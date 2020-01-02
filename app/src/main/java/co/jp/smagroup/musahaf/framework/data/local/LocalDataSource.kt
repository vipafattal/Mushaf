package co.jp.smagroup.musahaf.framework.data.local

import co.jp.smagroup.musahaf.framework.api.ApiModels
import co.jp.smagroup.musahaf.framework.commen.MushafConstants
import co.jp.smagroup.musahaf.framework.database.MushafDao
import co.jp.smagroup.musahaf.model.*
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import com.codebox.lib.extrenalLib.TinyDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */

class LocalDataSource : LocalDataSourceProviders {
    private val sajdaList: List<Int> =
        listOf(
            1160,
            1722,
            1951,
            2138,
            2308,
            2613,
            2672,
            2915,
            3185,
            3518,
            3994,
            4256,
            4846,
            5905,
            6125
        )
    @Inject
    lateinit var tinyDB: TinyDB
    @Inject
    lateinit var dao: MushafDao

    init {
        MushafApplication.appComponent.inject(this)
    }


    override suspend fun addSupportedLanguages(languages: List<String>) {
        withContext(Dispatchers.Main) {
            tinyDB.putListString(PreferencesConstants.SupportedLanguage, languages)
        }
    }

    override suspend fun getSupportedLanguage(): List<String> =
        withContext(Dispatchers.Main) {
            tinyDB.getListString(PreferencesConstants.SupportedLanguage)
        }


    override suspend fun addAyat(
        quran: ApiModels.QuranDataApi
    ) {
        for (aya in quran.ayahs) {
            dao.addAya(aya.copy(edition_id = quran.edition.identifier, surah_number = aya.surah!!.number))
            dao.addSurah(aya.surah!!)
            dao.addEdition(quran.edition)
        }
    }

    override suspend fun getSurahs(): List<Surah> =
        dao.getSurahs()

    override suspend fun addEditions(editions: List<Edition>) =
        dao.addEditions(editions)


    override suspend fun addEdition(edition: Edition) =
        dao.addEdition(edition)


    override suspend fun addDownloadState(downloadState: DownloadingState) =
        dao.addDownloadState(downloadState)

    override suspend fun getDownloadingState(identifier: String): DownloadingState =
        dao.getDownloadingState(identifier)
            ?: DownloadingState(identifier, false, null)

    override suspend fun getAvailableEditions(format: String, language: String): List<Edition> =
        dao.getAllAvailableEditions(format, language)

    override suspend fun getAllEditions(): List<Edition> =
        dao.getAllEditions()

    override suspend fun getEditionsByType(type: String): List<Edition> =
        dao.getEditionsByType(type)

    override suspend fun getAvailableReciters(): List<Edition> =
        dao.getEditionsByType(MushafConstants.Audio)


    override suspend fun addDownloadReciter(reciters: List<Reciter>) =
        dao.addReciters(reciters)


    override suspend fun addDownloadReciter(reciter: Reciter) =
        dao.addReciter(reciter)


    override suspend fun updateBookmarkStatus(
        ayaNumber: Int,
        identifier: String,
        bookmarkStatus: Boolean
    ) {
        dao.updateAyaBookmarkState(ayaNumber, identifier, bookmarkStatus)
    }

    override suspend fun searchTranslation(query: String, type: String): MutableList<Aya> {
        val ayatWithInfo = dao.searchTranslation("%$query%", type)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }

    override suspend fun searchQuran(query: String, editionId: String): List<Aya> {
        val ayatWithInfo = dao.searchQuran("%$query%", editionId)
        return ayatWithInfo.map { Aya(it) }.toMutableList()    }

    override suspend fun getAllAyatByIdentifier(musahafIdentifier: String): MutableList<Aya> {
        val ayatWithInfo = dao.getAyatByEdition(musahafIdentifier)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }


    override suspend fun getPage(musahafIdentifier: String, page: Int): MutableList<Aya> {
        val ayatWithInfo = dao.getAyatOfPage(musahafIdentifier, page)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }

    override suspend fun getQuranBySurah(
        musahafIdentifier: String,
        surahNumber: Int
    ): MutableList<Aya> {
        val ayatWithInfo = dao.getAyatOfSurah(musahafIdentifier, surahNumber)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }

    override suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya {
        val ayaWithInfo = dao.getAyaByNumberInMusahaf(musahafIdentifier, number)
            ?: throw IllegalStateException("$musahafIdentifier at aya $number is not downloaded")
        return Aya(ayaWithInfo)
    }

    override suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya> {
        val ayaWithInfo = dao.getAllAyatByBookmark(bookmarkStatus)
        return ayaWithInfo.map { Aya(it) }.toMutableList()
    }


    override suspend fun getAyaByBookmark(
        musahafIdentifier: String,
        bookmarkStatus: Boolean
    ): MutableList<Aya> {
        val ayaWithInfo = dao.getAyaByBookmarkStatus(musahafIdentifier, bookmarkStatus)
        return ayaWithInfo.map { Aya(it) }.toMutableList()
    }


    override suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya> {
        val ayaWithInfo = dao.getAyatByRange(MushafConstants.MainMushaf, from, to)
        return ayaWithInfo.map { Aya(it) }.toMutableList()
    }


    override suspend fun getReciterDownload(ayaNumber: Int, reciterName: String): Reciter? {
        val reciterWithInfo = dao.getReciterByAyaNumber(ayaNumber, reciterName)
        return if (reciterWithInfo != null) Reciter(reciterWithInfo) else null
    }

    override suspend fun getReciterDownloads(
        from: Int,
        to: Int,
        reciterIdentifier: String
    ): List<Reciter> {
        val reciterWithInfo = dao.getReciterDownloadsByRang(from, to, reciterIdentifier)
        return reciterWithInfo.map { Reciter(it) }
    }

    override suspend fun getDownloadedDataReciter(reciterName: String): List<Reciter> {
        val reciterWithInfo = dao.getReciterDataByName(reciterName)
        return reciterWithInfo.map { Reciter(it) }
    }
}