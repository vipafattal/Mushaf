package com.brilliancesoft.mushaf.framework.data.repo

import androidx.annotation.Keep
import com.brilliancesoft.mushaf.framework.utils.EditionTypeOpt
import com.brilliancesoft.mushaf.model.*

/**
 * Created by ${User} on ${Date}
 */
@Keep
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
    suspend fun getAllByBookmarked(): MutableList<Aya>
    suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya

    suspend fun getAllEditions(): List<Edition>
    suspend fun getEditionsByType(@EditionTypeOpt type: String, fromInternet: Boolean = false): List<Edition>
    suspend fun getAvailableReciters(fromInternet: Boolean = false): List<Edition>
    suspend fun getAllEditionsWithState(): List<Pair<Edition, DownloadingState>>
    suspend fun getDownloadedTafseer(): List<Edition>
    suspend fun searchTranslation(query: String, type: String): List<Aya>
    suspend fun searchQuran(query: String, editionId: String): List<Aya>

    suspend fun updateBookmarkStatus(ayaNumber: Int, identifier: String, bookmarkStatus: Boolean)

    suspend fun isDownloaded(identifier: String): Boolean
    suspend fun getDownloadedTafseer(type: String): List<Edition>
}