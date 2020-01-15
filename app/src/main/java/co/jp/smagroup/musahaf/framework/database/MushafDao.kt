package co.jp.smagroup.musahaf.framework.database

import androidx.room.*
import co.jp.smagroup.musahaf.model.*

@Dao
interface MushafDao {

/*    suspend fun insertAyaWithInfo(ayaWithInfo: AyaWithInfo) {
        addEdition(ayaWithInfo.edition)
        addSurah(ayaWithInfo.surah)
        addAya(ayaWithInfo.aya)
    }

    suspend fun insertAllAyaWithInfo(ayaWithInfoList: List<AyaWithInfo>) {
        for (ayaWithInfo in ayaWithInfoList) {
            addEdition(ayaWithInfo.edition)
            addSurah(ayaWithInfo.surah)
            addAya(ayaWithInfo.aya)
        }
    }*/

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
    suspend fun searchTranslation(query: String, type: String):List<AyaWithInfo>

    @Transaction
    @Query("select * from $AYAT_TABLE where edition_id = :editionId and text like :query")
    suspend fun searchQuran(query: String, editionId: String):List<AyaWithInfo>



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

    @Query("select * from $EDITIONS_TABLE where format == :type")
    suspend fun getEditionsByType(type: String): List<Edition>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEditions(editions: List<Edition>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEdition(edition: Edition)

    //Recipters dao
    @Transaction
    @Query("select * from $RECITERS_TABLE")
    suspend fun getReciters(): List<ReciterInfo>

    @Transaction
    @Query("select * from $RECITERS_TABLE where number_in_mushaf = :ayaNumber and name = :reciterName limit 1")
    suspend fun getReciterByAyaNumber(ayaNumber: Int, reciterName: String): ReciterInfo?

    @Transaction
    @Query("select * from $RECITERS_TABLE where number_in_mushaf between :startNumber AND :endNumber and edition_id = :reciterIdentifier and edition_id = :reciterIdentifier")
    suspend fun getReciterDownloadsByRang(startNumber: Int, endNumber: Int, reciterIdentifier: String): List<ReciterInfo>

    @Transaction
    @Query("select * from $RECITERS_TABLE where name = :reciterName")
    suspend fun getReciterDataByName(reciterName: String): List<ReciterInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReciter(reciter: Reciter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReciters(reciters: List<Reciter>)

   //DownloadState dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownloadState(downloadingState: DownloadingState)

    @Query("select * from $DOWNLOAD_STATE_TABLE where identifier == :id limit 1")
    suspend fun getDownloadingState(id: String): DownloadingState?


}