package com.brilliancesoft.mushaf.framework.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brilliancesoft.mushaf.framework.database.daos.MetadataDao
import com.brilliancesoft.mushaf.framework.database.daos.MushafDao
import com.brilliancesoft.mushaf.framework.database.helpers.*
import com.brilliancesoft.mushaf.model.*

@Database(
    entities = [Aya::class, Edition::class, Surah::class, DownloadingState::class, DownloadedUri::class],
    version = DATA_VERSION
)
abstract class MushafDatabase : RoomDatabase() {

    abstract fun mushafDao(): MushafDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        @JvmField
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                alterTableUsage(database)
            }
        }

        private fun alterTableUsage(database: SupportSQLiteDatabase) {

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
}