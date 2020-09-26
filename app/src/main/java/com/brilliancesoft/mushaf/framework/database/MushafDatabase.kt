package com.brilliancesoft.mushaf.framework.database

import android.database.DatabaseUtils
import android.database.sqlite.SQLiteException
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brilliancesoft.mushaf.framework.commen.MushafConstants.MainMushaf
import com.brilliancesoft.mushaf.framework.commen.MushafConstants.MainMushaf_OLD
import com.brilliancesoft.mushaf.framework.database.daos.MetadataDao
import com.brilliancesoft.mushaf.framework.database.daos.MushafDao
import com.brilliancesoft.mushaf.framework.database.helpers.*
import com.brilliancesoft.mushaf.model.*

@Database(
    entities = [Aya::class, Edition::class, Surah::class, Bookmark::class, DownloadingState::class, DownloadedUri::class],
    version = DATA_VERSION
)
abstract class MushafDatabase : RoomDatabase() {

    abstract fun mushafDao(): MushafDao
    abstract fun metadataDao(): MetadataDao

    companion object {

        @JvmField
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                createTable(
                    db = database,
                    tableName = DOWNLOADED_URI_TABLE,
                    columns = mapOf(
                        "link TEXT NOT NULL".toExisting(),
                        "title TEXT NOT NULL".toExisting(),
                        "editionId TEXT NOT NULL".toExisting(),
                        "ayaNumber INTEGER NOT NULL".toExisting()
                    ),
                    primaryKeys = listOf("link")
                )

                deleteTable(
                    db = database,
                    tableName = RECITERS_TABLE
                )
            }
        }

        @JvmField
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                createTable(
                    db = database,
                    tableName = BOOKMARKS_TABLE,
                    columns = mapOf(
                        "bookmark_editionId TEXT NOT NULL".toExisting(),
                        "bookmark_ayaNumber INTEGER NOT NULL".toExisting()
                    ),
                    primaryKeys = listOf("bookmark_editionId", "bookmark_ayaNumber")
                )

                database.execSQL(
                    "INSERT INTO $BOOKMARKS_TABLE (bookmark_editionId, bookmark_ayaNumber) SELECT edition_id, number_in_mushaf FROM $AYAT_TABLE where isBookmarked = 1"
                )

                database.execSQL(
                    "UPDATE $BOOKMARKS_TABLE SET bookmark_editionId = '$MainMushaf' where bookmark_editionId like '$MainMushaf_OLD' "
                )


                //updateTable(database,BOOKMARKS_TABLE, mapOf("bookmark_editionId" to MainMushaf), primaryKeys = [])
                /*database.update(
                    BOOKMARKS_TABLE,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    ContentValues().apply { put("bookmark_editionId", MainMushaf_OLD) },
                    " bookmark_editionId = $MainMushaf_OLD ",
                    arrayOf(MainMushaf_OLD))
*/
                updateTable(
                    db = database,
                    tableName = AYAT_TABLE,
                    columns = mapOf(
                        "number_in_mushaf INTEGER NOT NULL".toExisting(),
                        "surah_number INTEGER NOT NULL".toExisting(),
                        "text TEXT NOT NULL".toExisting(),
                        "numberInSurah INTEGER NOT NULL".toExisting(),
                        "juz INTEGER NOT NULL".toExisting(),
                        "page INTEGER NOT NULL".toExisting(),
                        "hizbQuarter INTEGER NOT NULL".toExisting(),
                        "edition_id TEXT NOT NULL".toExisting()
                    ),
                    primaryKeys = listOf("number_in_mushaf", "edition_id")
                )
                try {
                    database.execSQL("DELETE FROM $AYAT_TABLE WHERE edition_id = $MainMushaf_OLD")
                } catch (sq: SQLiteException){
                    sq.printStackTrace()
                }
            }
        }
    }
}