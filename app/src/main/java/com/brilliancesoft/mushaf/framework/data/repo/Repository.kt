package com.brilliancesoft.mushaf.framework.data.repo

import android.util.Log
import androidx.annotation.Keep
import com.brilliancesoft.mushaf.framework.api.QuranCloudAPI
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.local.LocalRepository
import com.brilliancesoft.mushaf.model.*
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.utils.extensions.onComplete
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */
@Keep
class Repository : RepositoryProviders {
    private val localRepository = LocalRepository()
    val localRepositoryMetadata = localRepository.metadataRepository
    var errorStream: BehaviorSubject<String> = BehaviorSubject.create()
        private set

    var loadingStream: BehaviorSubject<Int> = BehaviorSubject.create()
        private set

    @Inject
    lateinit var quranCloudAPI: QuranCloudAPI

    init {
        MushafApplication.appComponent.inject(this)
    }

    override suspend fun getSupportedLanguage(): List<String> {
        var languages = localRepository.getSupportedLanguage()
        if (languages.isEmpty()) {
            quranCloudAPI.getSupportedLanguage().awaitRequest {
                localRepository.addSupportedLanguages(it.languages)
                languages = it.languages
            }
        }
        return languages
    }

    override suspend fun getAyat(identifier: String): MutableList<Aya> {
        val downloadingState = localRepository.getDownloadingState(identifier)
        val startingPoint: Int
        when {
            !downloadingState.isDownloadCompleted -> startingPoint = downloadingState.stopPoint ?: 1
            else -> return localRepository.getAllAyatByIdentifier(identifier)
        }
        downloadAyat(identifier, startingPoint)

        val musahaf = localRepository.getAllAyatByIdentifier(identifier)
        return if (musahaf.size == MushafConstants.AyatNumber) musahaf else mutableListOf()
    }


    override suspend fun downloadAyat(identifier: String, startingPoint: Int) {
        var error: String? = null
        refreshForNewTask()
        loop@ for (juz in startingPoint..30) {
            quranCloudAPI.getQuran(juz, identifier).onComplete({
                localRepository.addAyat(it.data)
                loadingStream.onNext(juz)
                if (juz < 30) {
                    localRepository.addDownloadState(DownloadingState(identifier, false, juz + 1))
                    serverDelay(juz)
                } else {
                    localRepository.addDownloadState(
                        DownloadingState.downloadQuranTextCompleted(
                            identifier
                        )
                    )
                    delay(500)
                    refreshForNewTask()
                }
            }, onError = {
                error = it
                errorStream.onNext(it)
            })

            if (error != null)
                break@loop
        }
    }
    /*
    @NewAPI
    override suspend fun downloadFullDataReciter(
        reciterId: String,
        reciterName: String
    ) {
        val allReciterDownloads = localRepository.getDownloadedDataReciter(reciterName)
        var notDownloadedAyaRequest = mutableListOf<Request>()

        if (allReciterDownloads.isNotEmpty()) {
            for (ayaNumber in 1..MushafConstants.AyatNumber) if (allReciterDownloads.firstOrNull { it.number == ayaNumber } == null) {
                val aya = QuranViewModel.MainQuranList[ayaNumber - 1]
                val request =
                    ReciterRequestGenerator.createRequestFromFile(
                        reciterName,
                        reciterId,
                        aya.surah!!,
                        ayaNumber
                    )
                notDownloadedAyaRequest.add(request)

            }
        } else {
            notDownloadedAyaRequest = QuranViewModel.MainQuranList.map {
                ReciterRequestGenerator.createRequestFromFile(
                    reciterName,
                    reciterId,
                    it.surah!!,
                    it.number
                )
            }.toMutableList()
        }

        fetch.enqueue(notDownloadedAyaRequest)
        /*  val downloadListener = FetchDownloadListener(
              notDownloadedAyaRequest.size,
              reciterName,
              QuranViewModel.QuranDataList,
              this
          ) {}
          fetch.addListener(downloadListener)
          Log.d("FullReciterDownloader: ", "Downloading started")*/
    }*/


    override suspend fun updateBookmarkStatus(
        ayaNumber: Int,
        identifier: String,
        bookmarkStatus: Boolean
    ) {
        localRepository.updateBookmarkStatus(ayaNumber, identifier, bookmarkStatus)
    }


    override suspend fun searchTranslation(query: String, type: String): List<Aya> =
        localRepository.searchTranslation(query, type)
            .filter { isDownloaded(it.edition!!.identifier) }

    override suspend fun searchQuran(query: String, editionId: String): List<Aya> =
        localRepository.searchQuran(query, editionId)


    override suspend fun getMusahafAyat(identifier: String) = runBlocking<MutableList<Aya>> {
        val downloadingState = localRepository.getDownloadingState(identifier)
        val startingPoint: Int
        if (!downloadingState.isDownloadCompleted) {
            startingPoint = downloadingState.stopPoint ?: 1
            downloadAyat(identifier, startingPoint)
        }

        val musahaf = localRepository.getAllAyatByIdentifier(identifier)
        return@runBlocking if (musahaf.size == MushafConstants.AyatNumber) musahaf else mutableListOf()
    }

    override suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya> =
        localRepository.getAyatByRange(from, to)


    override suspend fun getQuranBySurah(
        musahafIdentifier: String,
        surahNumber: Int
    ): MutableList<Aya> =
        localRepository.getQuranBySurah(musahafIdentifier, surahNumber)


    override suspend fun getAvailableEditions(format: String, language: String): List<Edition> {
        var editions = localRepository.getAvailableEditions(format, language)
        if (editions.isEmpty())
            quranCloudAPI.getEditions(format, language).awaitRequest {
                localRepository.addEditions(it.editions)
                editions = it.editions
            }
        return editions
    }

    override suspend fun getAllEditions(): List<Edition> {
        var editions = listOf<Edition>()
        refreshForNewTask()
        quranCloudAPI.getAllEditions().awaitRequest {
            editions = it.editions
        }
        return editions
    }

    override suspend fun getSurahs(): List<Surah> =
        localRepository.getSurahs()

    override suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya =
        localRepository.getAyaByNumberInMusahaf(musahafIdentifier, number)

    override suspend fun getAllEditionsWithState(): List<Pair<Edition, DownloadingState>> {
        refreshForNewTask()
        var data = listOf<Pair<Edition, DownloadingState>>()
        quranCloudAPI.getAllEditions().awaitRequest {
            Log.d("getAllEditionsWithState", it.toString())
            data = it.editions.map { edition ->
                edition to localRepository.getDownloadingState(edition.identifier)
            }
        }
        return data
    }

    override suspend fun getEditionsByType(type: String, fromInternet: Boolean): List<Edition> {
        var data = listOf<Edition>()
        if (!fromInternet)
            data = localRepository.getEditionsByType(type)
        if (fromInternet || data.isEmpty())
            quranCloudAPI.getEditionsByType(type).awaitRequest {
                data = it.editions
                localRepository.addEditions(data)
            }
        return data
    }

    override suspend fun getAvailableReciters(fromInternet: Boolean): List<Edition> {
        var data = listOf<Edition>()
        if (!fromInternet)
            data = localRepository.getAvailableReciters()
        if (fromInternet || data.isEmpty())
            quranCloudAPI.getEditionsByType(MushafConstants.Audio).awaitRequest {
                data = it.editions.removeInGrantedReciters()
                localRepository.addEditions(data)
            }
        return data
    }

    override suspend fun getDownloadedEditions(): List<Edition> =
        localRepository.getAllEditions().distinctBy { it.identifier }
            .filter { isDownloaded(it.identifier) }
            .filter { it.identifier != MushafConstants.WordByWord && it.identifier != MushafConstants.MainMushaf }
            .sortedBy { it.language }

    override suspend fun getDownloadedEditions(type: String): List<Edition> =
        localRepository.getAllEditions(type).distinctBy { it.identifier }
            .filter { isDownloaded(it.identifier) }
            .filter { it.identifier != MushafConstants.WordByWord && it.identifier != MushafConstants.MainMushaf }
            .sortedBy { it.language }


    override suspend fun isDownloaded(identifier: String): Boolean {
        val downloadingState = localRepository.getDownloadingState(identifier)
        return downloadingState.isDownloadCompleted
    }

    override suspend fun getDownloadState(identifier: String): DownloadingState =
        localRepository.getDownloadingState(identifier)


    override suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya> =
        localRepository.getAllByBookmarkStatus(bookmarkStatus)

    override suspend fun getByAyaByBookmark(
        editionIdentifier: String,
        bookmarkStatus: Boolean
    ): MutableList<Aya> =
        localRepository.getAyaByBookmark(editionIdentifier, bookmarkStatus)

    override suspend fun getPage(musahafIdentifier: String, page: Int): List<Aya>? =
        if (isDownloaded(musahafIdentifier)) localRepository.getPage(musahafIdentifier, page)
        else null

    private suspend fun serverDelay(juz: Int) {
        if (juz % 5 == 0)
            delay(7000)
        else
            delay(4000)
    }


    private suspend inline fun <reified T> Deferred<Response<T>>.awaitRequest(onSuccess: (T) -> Unit) {
        onComplete({
            onSuccess(it)
        }, onError = {
            errorStream.onNext(it)
        })
    }

    private fun refreshForNewTask() {
        errorStream.onNext("")
        loadingStream.onNext(0)
    }

    //Removing some reciters due to incorrectness in reciting.
    private fun List<Edition>.removeInGrantedReciters(): List<Edition> {
        val data = this.toMutableList()
        return data.filter { it.identifier != "ar.ahmedajamy" || it.identifier != "ar.mahermuaiqly" }
    }
}