package com.brilliancesoft.mushaf.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brilliancesoft.mushaf.framework.database.helpers.BOOKMARKS_TABLE

@Entity(tableName = BOOKMARKS_TABLE,primaryKeys = ["bookmark_editionId","bookmark_ayaNumber"])
data class Bookmark(
    @ColumnInfo(name = "bookmark_editionId")
    val editionId: String,
    @ColumnInfo(name = "bookmark_ayaNumber")
    val ayaNumber:Int
)

{
    companion object {
        const val QURAN_BOOKMARK = 0
        const val LIBRARY_BOOKMARK = 0
    }
}

