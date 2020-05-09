package com.brilliancesoft.mushaf.ui.audioPlayer.helpers

import android.content.Context
import android.net.Uri
import com.brilliancesoft.mushaf.framework.commen.MediaLinkBuilder
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.DownloadedUri
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.Media
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.audioPlayer.MediaDownloadService
import com.google.android.exoplayer2.offline.*
import java.io.IOException
import java.util.*

/**
 * Created by Abed on 2019/9/24
 */

object DownloadUtils {

    private val context: Context = MushafApplication.appContext
    //private val downloadManager: DownloadManager
      //  get() = ExoPlayerCacheHelper.downloadManager

    /**
     * Download [Aya] by [Aya.number] and link to save it to cache so to check if download not completed yet.
     * It gets removed from cache when download completed
     * @see CheckDownloadComplete
     */
    fun download(ayaNumber: Int, reciterEdition: Edition): Boolean {
        var downloadStarted = true
        val mediaLink = MediaLinkBuilder.linkGenerator(ayaNumber, reciterEdition.identifier)
        if (!isDownloading(mediaLink)) {
            val uri = Uri.parse(mediaLink)

            val downloadHelper = DownloadHelper.forProgressive(uri)
            downloadHelper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(helper: DownloadHelper?) {}
                override fun onPrepareError(helper: DownloadHelper?, e: IOException?) {}
            })

            val downloadId = UUID.randomUUID().toString()
            val downloadUri =
                DownloadedUri(mediaLink, reciterEdition.name, reciterEdition.identifier, ayaNumber)

            val downloadRequest = DownloadRequest(
                downloadId,
                DownloadRequest.TYPE_PROGRESSIVE,
                uri,  /* streamKeys= */
                Collections.emptyList(),  /* customCacheKey= */
                null,
                downloadUri.toJson().toByteArray()
            )

            DownloadService.sendAddDownload(
                context,
                MediaDownloadService::class.java,
                downloadRequest,
                true
            )

        } else
            downloadStarted = false


        return downloadStarted
    }


    fun download(request: DownloadRequest) {
        DownloadService.sendAddDownload(context, MediaDownloadService::class.java, request, true)
    }

    fun download(ayatDownloadRange: IntRange, reciterEdition: Edition) {
        for (ayaNumber in ayatDownloadRange)
            download(ayaNumber, reciterEdition)
    }
/*
    fun removeDownload(download: Download) {
        DownloadService.sendRemoveDownload(
            context,
            MediaDownloadService::class.java,
            download.request.id,
            true
        )
    }

    fun removeDownload(media: Media) {
        val uri = Uri.parse(media.link)
        val download =
            ExoPlayerCacheHelper.downloadManager.currentDownloads.first { it.request.uri == uri }

        DownloadService.sendRemoveDownload(
            context,
            MediaDownloadService::class.java,
            download.request.id,
            true
        )
    }
*/
    /*fun resumeDownloads() {
        if (downloadManager.currentDownloads.isNotEmpty() && downloadManager.downloadsPaused && !downloadManager.isWaitingForRequirements)
            DownloadService.sendResumeDownloads(context, MediaDownloadService::class.java, true)
    }*/

    fun isDownloading(link: String): Boolean {
        val uri = Uri.parse(link)

        val download =
            ExoPlayerCacheHelper.downloadManager.currentDownloads.firstOrNull { it.request.uri == uri }

        if (download != null && download.state == Download.STATE_FAILED)
            DownloadService.sendResumeDownloads(context, MediaDownloadService::class.java, true)
        return download != null
    }

}