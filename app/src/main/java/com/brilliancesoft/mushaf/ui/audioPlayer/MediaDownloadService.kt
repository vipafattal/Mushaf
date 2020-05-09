package com.brilliancesoft.mushaf.ui.audioPlayer

import android.app.Notification
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.DownloadedUri
import com.brilliancesoft.mushaf.ui.audioPlayer.helpers.ExoPlayerCacheHelper
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import javax.inject.Inject


class MediaDownloadService : DownloadService(
    DOWNLOAD_NOTIFICATION_ID, DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.exo_download_notification_channel_name, 0
) {

    @Inject
    lateinit var repository: Repository
    private val downloadsList: MutableList<Download> = mutableListOf()
    private val downloadNotificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(
            this,
            DOWNLOAD_NOTIFICATION_CHANNEL_ID
        )
    }

    init {
        MushafApplication.appComponent.inject(this)
    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override fun getDownloadManager(): DownloadManager {

        return ExoPlayerCacheHelper.downloadManager.apply {
            requirements = DownloadManager.DEFAULT_REQUIREMENTS
            maxParallelDownloads = 3
            addListener(
                TerminalStateNotificationHelper(
                    this@MediaDownloadService,
                    DOWNLOAD_NOTIFICATION_ID + 1
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancelChildren()
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        var info = getString(R.string.downloading) + ":\n"
        for (download in downloads) {
            val ayaNumber = getMediaFromBytes(download.request.data).ayaNumber.toString()
            info += getString(R.string.aya_number) + ":" + ayaNumber + "\n"
        }
        downloadsList.clear()
        downloadsList.addAll(downloads)
        downloadsLiveData.postValue(downloadsList)

        return downloadNotificationHelper.buildProgressNotification(
            R.drawable.ic_cloud_download,
            null,
            info,
            downloads
        )
    }

    override fun getScheduler(): Scheduler? =
        if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null

    private inner class TerminalStateNotificationHelper(
        private val context: Context,
        firstNotificationId: Int
    ) : DownloadManager.Listener {
        private var nextNotificationId = firstNotificationId

        override fun onDownloadChanged(
            manager: DownloadManager,
            download: Download
        ) {

            val downloadedUri = getMediaFromBytes(download.request.data)
            val notification: Notification?

            when (download.state) {
                Download.STATE_COMPLETED -> {
                    updateDownloads(download)
                    coroutineScope.launch {
                        repository.localRepositoryMetadata.addDownloadedUri(downloadedUri)
                    }

                    notification = null
                }
                Download.STATE_FAILED -> {
                    updateDownloads(download)

                    notification = downloadNotificationHelper.buildDownloadFailedNotification(
                        R.drawable.ic_error_outline,  /* contentIntent= */
                        null,
                        getString(R.string.exo_download_failed) + downloadedUri.title + "/" + downloadedUri.ayaNumber
                    )
                }

                else -> {
                    updateDownloads(download)
                    return
                }
            }

            NotificationUtil.setNotification(context, nextNotificationId++, notification)
        }

        private fun updateDownloads(download: Download) {
            val updateDownloadIndex =
                downloadsList.indexOfFirst { it.request.id == download.request.id }

            if (updateDownloadIndex != -1) {
                downloadsList[updateDownloadIndex] = download
                downloadsLiveData.postValue(downloadsList)
            }
        }
    }

    companion object {

        fun getMediaFromBytes(data: ByteArray): DownloadedUri {
            val jsonMedia = Util.fromUtf8Bytes(data)
            return DownloadedUri.fromJson(jsonMedia)
        }

        fun getCurrentDownloads(): LiveData<List<Download>> = downloadsLiveData

        private val downloadsLiveData: MutableLiveData<List<Download>> = MutableLiveData()

        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "xplayer_download_channel"
        private const val DOWNLOAD_NOTIFICATION_ID = 2
        private const val JOB_ID = 1
    }
}

