package com.brilliancesoft.mushaf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brilliancesoft.mushaf.framework.database.DOWNLOAD_STATE_TABLE
import kotlinx.serialization.Serializable

/**
 * Created by ${User} on ${Date}
 */
@Serializable
@Entity(tableName = DOWNLOAD_STATE_TABLE)
data class DownloadingState(
    @PrimaryKey
    val identifier: String,
    val isDownloadCompleted: Boolean,
    val stopPoint: Int?
)  {
    companion object {
        fun downloadQuranTextCompleted(identifier: String): DownloadingState = DownloadingState(identifier, true, 30)
    }
}