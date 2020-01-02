package co.jp.smagroup.musahaf.framework.api

import android.content.Context
import android.net.Uri
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.CustomToast
import co.jp.smagroup.musahaf.framework.commen.Priority
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Reciter
import co.jp.smagroup.musahaf.ui.quran.read.reciter.DownloadingFragment
import co.jp.smagroup.musahaf.utils.extensions.toFile
import com.crashlytics.android.Crashlytics
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        Crashlytics.log(Priority.Medium, "FetchError ", error.name)
        DownloadingFragment.playerDownloadingCancelled.onNext(true)
    }

    companion object {
        val progressListener: BehaviorSubject<Float> = BehaviorSubject.create()
    }

}