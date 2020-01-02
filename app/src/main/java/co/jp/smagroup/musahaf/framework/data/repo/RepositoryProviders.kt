package co.jp.smagroup.musahaf.framework.data.repo

import co.jp.smagroup.musahaf.framework.utils.EditionTypeOpt
import co.jp.smagroup.musahaf.model.*
import com.tonyodev.fetch2.Fetch

/**
 * Created by ${User} on ${Date}
 */
interface RepositoryProviders {
    suspend fun getSupportedLanguage(): List<String>
    suspend fun getAyat(identifier: String): MutableList<Aya>
    suspend fun downloadAyat(identifier: String, startingPoint: Int = 1)
    suspend fun getMusahafAyat(identifier: String): MutableList<Aya>
    suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya>
    suspend fun getAvailableEditions(format: String, language: String): List<Edition>
    suspend fun getQuranBySurah(musahafIdentifier: String, surahNumber: Int): MutableList<Aya>
    suspend fun getPage(musahafIdentifier: String, page: Int): List<Aya>?
    suspend fun getSurahs(): List<Surah>
    suspend fun getDownloadState(identifier: String): DownloadingState
    suspend fun getByAyaByBookmark(editionIdentifier: String, bookmarkStatus: Boolean): MutableList<Aya>
    suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya>
    suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya

    suspend fun getAllEditions(): List<Edition>
    suspend fun getEditionsByType(@EditionTypeOpt type: String, fromInternet: Boolean = false): List<Edition>
    suspend fun getAvailableReciters(fromInternet: Boolean = false): List<Edition>
    suspend fun getAllEditionsWithState(): List<Pair<Edition, DownloadingState>>
    suspend fun getDownloadedEditions(): List<Edition>
    suspend fun downloadFullDataReciter(fetch: Fetch, reciterId: String, reciterName: String)
    suspend fun searchTranslation(query: String, type: String): List<Aya>
    suspend fun searchQuran(query: String, editionId: String): List<Aya>

    suspend fun updateBookmarkStatus(ayaNumber: Int, identifier: String, bookmarkStatus: Boolean)

    suspend fun addDownloadedReciter(reciter: Reciter)
    suspend fun addDownloadReciter(data: List<Reciter>)
    suspend fun getReciterDownload(ayaNumber: Int, reciterName: String): Reciter?
    suspend fun getReciterDownloads(from: Int, to: Int, reciterIdentifier: String): List<Reciter>

    suspend fun isDownloaded(identifier: String): Boolean
}