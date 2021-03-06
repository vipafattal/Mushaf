package com.brilliancesoft.mushaf.framework

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.DownloadingState
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.library.manage.ManageLibraryActivity
import com.brilliancesoft.mushaf.utils.extensions.bundleOf
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import javax.inject.Inject

@Keep
class DownloadService : Service() {

    @Inject
    lateinit var repository: Repository

    private var loadingDisposable: Disposable? = null
    private var errorDisposable: Disposable? = null
    private lateinit var notifyBuilder: NotificationCompat.Builder
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var edition: Edition
    private lateinit var downloadingState: DownloadingState

    init {
        MushafApplication.appComponent.inject(this)
    }

    private var currentProgress = 0

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.extras?.run {
            downloadingState = Json.decodeFromString(
                DownloadingState.serializer(),
                getString(DOWNLOADING_STATE_KEY) ?: throw  IllegalAccessException()
            )

            edition = Json.decodeFromString(
                Edition.serializer(),
                getString(EDITION_KEY) ?: throw  IllegalAccessException()
            )
        }
        createNotification()
        startDownload()
        return Service.START_STICKY
    }


    private fun startDownload() {

        isDownloading = true
        coroutineScope.launch {
            currentProgress = downloadingState.stopPoint ?: 1
            repository.downloadAyat(edition.identifier, currentProgress)
        }

        errorDisposable = repository.errorStream.filter { it != "" }.subscribe {
            finish(it)
        }
    }


    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotification() {

        val pendingIntent = Intent(applicationContext, ManageLibraryActivity::class.java).run {

            putExtras(bundleOf(ManageLibraryActivity.DOWNLOAD_EDITION_KEY to edition.identifier))

            PendingIntent.getActivity(
                applicationContext,
                0,
                this,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        notifyBuilder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentIntent(pendingIntent)
            .setContentTitle(edition.name)
            .setContentText(edition.language)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(edition.identifier)
            ).setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notifyBuilder.setSmallIcon(R.drawable.ic_downloading_notification)

        // Register the channel with the system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, importance
            )
            notificationManager.createNotificationChannel(channel)
        }

        notifyBuilder.setProgress(PROGRESS_MAX, currentProgress, false)

        // notificationId is a unique int for each notification that you must define
        startForeground(notificationId, notifyBuilder.build())
        activeDownloadingProgress(notifyBuilder)
    }


    @SuppressLint("CheckResult")
    private fun activeDownloadingProgress(
        builder: NotificationCompat.Builder
    ) {
        loadingDisposable = repository.loadingStream.subscribe {
            if (currentProgress < PROGRESS_MAX) {
                currentProgress = it
                builder.setProgress(PROGRESS_MAX, currentProgress, false)
                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(notificationId, builder.build())
            } else
                finish(getString(R.string.download_completed))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseProcess()
    }

    private fun finish(message: String) {
        isDownloading = false
        if (message != getString(R.string.cancelled)) showToast(message)
        notificationManager.cancel(notificationId)
        stopSelf()
    }


    private fun releaseProcess() {
        loadingDisposable?.dispose()
        job.cancelChildren()
        currentProgress = 0
    }


    private fun showToast(msg: String) {
        coroutineScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        var isDownloading = false
            private set

        private const val CHANNEL_ID = "123"
        private const val CHANNEL_NAME = "DOWNLOAD_SERVICE"

        private const val notificationId = R.string.app_name + R.string.downloading
        const val PROGRESS_MAX = 30

        const val DOWNLOADING_STATE_KEY = "downloadingState"
        const val EDITION_KEY = "edition_key"

        fun create(context: Context, edition: Edition, downloadingState: DownloadingState) {
            val jsonDownloadState = Json.encodeToString(DownloadingState.serializer(), downloadingState)
            val jsonEdition = Json.encodeToString(Edition.serializer(), edition)

            val intent = Intent(context, DownloadService::class.java)
            intent.putExtras(
                bundleOf(
                    DOWNLOADING_STATE_KEY to jsonDownloadState,
                    EDITION_KEY to jsonEdition
                )
            )
            context.startService(intent)
        }
    }
}
