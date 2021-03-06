package com.brilliancesoft.mushaf.framework.data.local

import com.brilliancesoft.mushaf.framework.utils.EditionTypeOpt
import com.brilliancesoft.mushaf.model.*


interface LocalDataSourceProviders {

    suspend fun addAyat(ayat:List<Aya>, edition: Edition)
    suspend fun addSupportedLanguages(languages: List<String>)
    suspend fun addEditions(editions: List<Edition>)
    suspend fun addEdition(edition: Edition)
    suspend fun addDownloadState(downloadState: DownloadingState)

    suspend fun updateBookmarkStatus(ayaNumber: Int,identifier: String, isAddNew: Boolean)

    suspend fun getSupportedLanguage(): List<String>
    suspend fun getAvailableEditions(format: String, language: String): List<Edition>
    suspend fun getAllEditions(): List<Edition>
    suspend fun getEditionsByType(@EditionTypeOpt type: String): List<Edition>
    suspend fun getAvailableReciters(): List<Edition>
    suspend fun getDownloadingState(identifier: String): DownloadingState?

    suspend fun getAllAyatByIdentifier(musahafIdentifier: String): MutableList<Aya>
    suspend fun getPage(musahafIdentifier: String, page: Int): MutableList<Aya>
    suspend fun getQuranBySurah(musahafIdentifier: String, surahNumber: Int): MutableList<Aya>
    suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya
    suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya>
    suspend fun getAllByBookmarkStatus(): MutableList<Aya>

    suspend fun searchTranslation(query: String,  type: String): MutableList<Aya>
    suspend fun searchQuran(query: String, editionId: String): List<Aya>
    suspend fun getSurahs(): List<Surah>
    suspend fun getAllEditionsByType(type: String): List<Edition>
}