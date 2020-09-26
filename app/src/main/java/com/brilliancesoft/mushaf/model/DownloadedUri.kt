package com.brilliancesoft.mushaf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brilliancesoft.mushaf.framework.database.helpers.DOWNLOADED_URI_TABLE
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = DOWNLOADED_URI_TABLE)
data class DownloadedUri(
    @PrimaryKey
    val link: String,
    val title: String,
    val editionId: String,
    val ayaNumber: Int
) {

    fun toJson(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String): DownloadedUri {
            require(json.isNotEmpty())
            return Json.decodeFromString(serializer(), json)
        }
    }
}