package com.brilliancesoft.mushaf.framework.database.daos

import androidx.room.*
import com.brilliancesoft.mushaf.framework.database.helpers.*
import com.brilliancesoft.mushaf.model.*

@Dao
interface MushafDao {

    //Ayat dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAya(aya: Aya)

    @Query("update $AYAT_TABLE set isBookmarked = :bookmarkState where number_in_mushaf = :ayaNumber and edition_id = :editionId")
    suspend fun updateAyaBookmarkState(ayaNumber: Int, editionId: String, bookmarkState: Boolean)

    @Transaction
    @Query("select * from $AYAT_TABLE where edition_id = :mushafIdentifier order by number_in_mushaf ASC")
    suspend fun getAyatByEdition(mushafIdentifier: String): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where page = :page and edition_id = :musahafIdentifier order by number_in_mushaf ASC")
    suspend fun getAyatOfPage(musahafIdentifier: String, page: Int): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where surah_number = :surahNumber and edition_id = :musahafIdentifier order by number_in_mushaf ASC")
    suspend fun getAyatOfSurah(musahafIdentifier: String, surahNumber: Int): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where number_in_mushaf between :startNumber AND :endNumber and edition_id = :musahafIdentifier order by numberInSurah ASC")
    suspend fun getAyatByRange(
        musahafIdentifier: String,
        startNumber: Int,
        endNumber: Int
    ): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where number_in_mushaf = :ayaNumber and edition_id = :musahafIdentifier limit 1")
    suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, ayaNumber: Int): AyaWithInfo?

    @Transaction
    @Query("select * from $AYAT_TABLE where isBookmarked = :bookmarkStatus and edition_id = :musahafIdentifier")
    suspend fun getAyaByBookmarkStatus(
        musahafIdentifier: String,
        bookmarkStatus: Boolean
    ): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where isBookmarked = :bookmarkStatus")
    suspend fun getAllAyatByBookmark(bookmarkStatus: Boolean): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE inner join $EDITIONS_TABLE on identifier == edition_id where type = :type and text like :query")
    suspend fun searchTranslation(query: String, type: String): List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where edition_id = :editionId and text like :query")
    suspend fun searchQuran(query: String, editionId: String): List<AyaWithInfo>

    //Surahs dao
    @Query("select * from $SURAHS_TABLE")
    suspend fun getSurahs(): List<Surah>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSurah(surah: Surah)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSurahs(surahs: List<Surah>)

    //Editions dao
    @Query("select * from $EDITIONS_TABLE where format == :format and language == :lang")
    suspend fun getAllAvailableEditions(format: String, lang: String): List<Edition>

    @Query("select * from $EDITIONS_TABLE")
    suspend fun getAllEditions(): List<Edition>

    @Query("select * from $EDITIONS_TABLE where format = :format")
    suspend fun getAllEditionsByFormat(format: String): List<Edition>

    @Query("select * from $EDITIONS_TABLE where format == :type")
    suspend fun getEditionsByType(type: String): List<Edition>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEditions(editions: List<Edition>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEdition(edition: Edition)

    //DownloadState dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownloadState(downloadingState: DownloadingState)

    @Query("select * from $DOWNLOAD_STATE_TABLE where identifier == :id limit 1")
    suspend fun getDownloadingState(id: String): DownloadingState?


}