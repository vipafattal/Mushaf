package com.brilliancesoft.mushaf.framework.api

import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Reciter
import com.brilliancesoft.mushaf.ui.quran.read.reciter.DownloadingFragment
import com.brilliancesoft.mushaf.utils.extensions.toFile
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Keep
class FetchDownloadListener(
    private val numberOfRequests: Int,
    private val reciterName: String,
    private val reciterIdentifier: String,
    private val selectedAyat: List<Aya>,
    private val repository: Repository,
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val doOnCompleted: (FetchListener) -> Unit
) : AbstractFetchListener() {
    init {
        progressListener.onNext(0f)
    }

    private var completedUriArray = arrayOfNulls<Uri>(numberOfRequests)
    private var completedDownloads = 0

    override fun onCompleted(download: Download) {
        completedDownloads++
        progressListener.onNext(completedDownloads.toFloat() / numberOfRequests * 100f)
        if (numberOfRequests >= completedDownloads) {
            val fileUri = download.fileUri
            completedUriArray[completedDownloads - 1] = fileUri

            val fileName = download.fileUri.toFile().nameWithoutExtension.toInt()

            val aya = selectedAyat.first { it.number == fileName }

            val reciter = Reciter(aya, reciterIdentifier, reciterName, download.fileUri)
            coroutineScope.launch(Dispatchers.IO) {
                repository.addDownloadedReciter(reciter)
            }
        }
        if (numberOfRequests == completedDownloads) {
            doOnCompleted.invoke(this)
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        CustomToast.makeShort(context, R.string.error_downloading)

        FirebaseCrashlytics.getInstance().log("FetchError ")
        DownloadingFragment.playerDownloadingCancelled.onNext(true)
    }

    companion object {
        val progressListener: BehaviorSubject<Float> = BehaviorSubject.create()
    }

}