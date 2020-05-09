package com.brilliancesoft.mushaf.framework.data.local

import com.brilliancesoft.mushaf.framework.database.daos.MetadataDao
import com.brilliancesoft.mushaf.framework.database.MushafDatabase
import com.brilliancesoft.mushaf.model.DownloadedUri
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.codebox.lib.extrenalLib.TinyDB
import javax.inject.Inject

class MetadataRepository : MetadataSourceProviders {

    @Inject
    lateinit var tinyDB: TinyDB
    @Inject
    lateinit var database: MushafDatabase
    private val dao: MetadataDao

    init {
        MushafApplication.appComponent.inject(this)
        dao = database.metadataDao()
    }

    override suspend fun getDownloadUri(editionId: String): List<DownloadedUri> =
        dao.getDownloadUri(editionId)

    override suspend fun addDownloadedUri(downloadedUri: DownloadedUri) {
        dao.addDownloadUri(downloadedUri)
    }
}