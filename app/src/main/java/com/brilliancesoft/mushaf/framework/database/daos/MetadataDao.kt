package com.brilliancesoft.mushaf.framework.database.daos

import androidx.room.*
import com.brilliancesoft.mushaf.framework.database.helpers.DOWNLOADED_URI_TABLE
import com.brilliancesoft.mushaf.model.*

@Dao
interface MetadataDao {
    @Query("select * from $DOWNLOADED_URI_TABLE where editionId = :editionId")
    suspend fun getDownloadUri(editionId: String): List<DownloadedUri>
    @Insert
    suspend fun addDownloadUri(downloadedUri: DownloadedUri)
}