package com.brilliancesoft.mushaf.framework.data.local

import com.brilliancesoft.mushaf.framework.api.ApiModels
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.database.daos.MushafDao
import com.brilliancesoft.mushaf.framework.database.MushafDatabase
import com.brilliancesoft.mushaf.model.*
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.codebox.lib.extrenalLib.TinyDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */

class LocalRepository : LocalDataSourceProviders {
    @Inject
    lateinit var tinyDB: TinyDB
    @Inject
    lateinit var database: MushafDatabase
    private  val dao: MushafDao
    @Inject
    lateinit var metadataRepository: MetadataRepository


    init {
        MushafApplication.appComponent.inject(this)
        dao = database.mushafDao()
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


    override suspend fun addAyat(quran: ApiModels.QuranDataApi) {

        for (index in quran.ayahs.indices) {
            val aya = quran.ayahs[index]
            aya.edition_id = quran.edition.identifier
            aya.surah_number = aya.surah!!.number
            dao.addAya(aya)
            dao.addSurah(aya.surah!!)
        }

        dao.addEdition(quran.edition)
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

    override suspend fun getAllEditions(type: String): List<Edition> =
        dao.getAllEditions()

    override suspend fun getEditionsByType(type: String): List<Edition> =
        dao.getEditionsByType(type)

    override suspend fun getAvailableReciters(): List<Edition> =
        dao.getEditionsByType(MushafConstants.Audio)


    override suspend fun updateBookmarkStatus(
        ayaNumber: Int,
        identifier: String,
        bookmarkStatus: Boolean
    ) {
        dao.updateAyaBookmarkState(ayaNumber, identifier, bookmarkStatus)
    }

    override suspend fun searchTranslation(query: String, type: String): MutableList<Aya> {
        val ayatWithInfo: List<AyaWithInfo> = dao.searchTranslation("%$query%", type)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }

    override suspend fun searchQuran(query: String, editionId: String): List<Aya> {
        val ayatWithInfo = dao.searchQuran("%$query%", editionId)
        return ayatWithInfo.map { Aya(it) }.toMutableList()
    }

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
        editionIdentifier: String,
        bookmarkStatus: Boolean
    ): MutableList<Aya> {
        val ayaWithInfo = dao.getAyaByBookmarkStatus(editionIdentifier, bookmarkStatus)
        return ayaWithInfo.map { Aya(it) }.toMutableList()
    }


    override suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya> {
        val ayaWithInfo = dao.getAyatByRange(MushafConstants.MainMushaf, from, to)
        return ayaWithInfo.map { Aya(it) }.toMutableList()
    }



}