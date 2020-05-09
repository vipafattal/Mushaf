package com.brilliancesoft.mushaf.framework.data.local

import com.brilliancesoft.mushaf.framework.api.ApiModels
import com.brilliancesoft.mushaf.framework.utils.EditionTypeOpt
import com.brilliancesoft.mushaf.model.*

/**
 * Created by ${User} on ${Date}
 */
interface MetadataSourceProviders {

    suspend fun getDownloadUri(editionId: String): List<DownloadedUri>
    suspend fun addDownloadedUri(downloadedUri: DownloadedUri)
}