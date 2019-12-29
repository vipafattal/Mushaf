@file:Suppress("MemberVisibilityCanBePrivate")

package co.jp.smagroup.musahaf.framework.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.jp.smagroup.musahaf.model.*

@Database(entities = [Aya::class, Edition::class, Surah::class, DownloadingState::class, Reciter::class], version = DATA_VERSION)
@TypeConverters(UriConverter::class)
abstract class MusahafDatabase : RoomDatabase() {
    abstract fun mushafDao(): MushafDao
}