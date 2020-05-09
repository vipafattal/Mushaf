package com.brilliancesoft.mushaf.ui.audioPlayer.helpers

import android.net.Uri
import com.brilliancesoft.mushaf.model.Media
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory

object MediaSourceBuilder {

    @Suppress("RedundantSuspendModifier")
    fun create(
        mediaList: List<Media>,
        eachTruck: Int,
        wholeSet: Int,
        playOffline: Boolean
    ): MediaSource {

        val concatenatingMediaSource =
            ConcatenatingMediaSource(*mediaList.map { create(it, eachTruck, playOffline) }.toTypedArray())
        return LoopingMediaSource(concatenatingMediaSource, wholeSet)
    }

    @Suppress("RedundantSuspendModifier")
    fun create(
        media: Media,
        eachTruck: Int,
        playOffline: Boolean
    ): ConcatenatingMediaSource {
        return if (playOffline)
            intermediateSource(media, eachTruck)
        else
            onlineSource(media, eachTruck)
    }

    /*  fun create(media: Media, eachTruck: Int = 1): MediaSource {
          return when {
              media.isDownloaded -> offlineSource(media, eachTruck)
              DownloadUtils.isDownloading(media) -> intermediateSource(media)
              else -> onlineSource(media, eachTruck)
          }
      }*/

    private fun onlineSource(
        media: Media,
        eachTrack: Int = 1
    ): ConcatenatingMediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory(ExoPlayerCacheHelper.userAgent)
        val audioUri = Uri.parse(media.link)

        val cacheDataSource =
            CacheDataSourceFactory(ExoPlayerCacheHelper.playerCache, dataSourceFactory)

        val audioSource =
            ProgressiveMediaSource.Factory(cacheDataSource, Mp3ExtractorsFactory()).setTag(media)
                .createMediaSource(audioUri)

        return ConcatenatingMediaSource(
            if (eachTrack == 0) audioSource
            else LoopingMediaSource(audioSource, eachTrack)
        )
    }

    private fun intermediateSource(media: Media,eachTrack: Int): ConcatenatingMediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory(ExoPlayerCacheHelper.userAgent)
        val audioUri = Uri.parse(media.link)

        val downloadDataSource = CacheDataSourceFactory(
            ExoPlayerCacheHelper.downloadCache,
            dataSourceFactory,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
        val audioSource =
            ProgressiveMediaSource.Factory(downloadDataSource, Mp3ExtractorsFactory()).setTag(media)
                .createMediaSource(audioUri)

        return ConcatenatingMediaSource(
            if (eachTrack == 0) audioSource
            else LoopingMediaSource(audioSource, eachTrack)
        )
    }

    private fun offlineSource(
        media: Media,
        eachTrack: Int = 1
    ): ConcatenatingMediaSource {


        val dataSource = CacheDataSourceFactory(
            ExoPlayerCacheHelper.downloadCache,
            DataSource.Factory { FileDataSource() },
            DataSource.Factory { FileDataSource() },
            null,
            0,
            null
        )

        val audioSource =
            ProgressiveMediaSource.Factory(dataSource, Mp3ExtractorsFactory()).setTag(media)
                .createMediaSource(Uri.parse(media.link))

        return ConcatenatingMediaSource(
            if (eachTrack == 0) audioSource
            else LoopingMediaSource(audioSource, eachTrack)
        )
    }

/*    private  fun offlineSource(
        mediaList: List<Media>,
        eachTruck: Int,
        wholeSet: Int
    ): ConcatenatingMediaSource {
        val mediaSourceArray = arrayOfNulls<MediaSource>(mediaList.size)

        for ((index, media) in mediaList.withIndex()) {
            val uri = DataDirectories.buildSurahFile(media).toUri()
            val dataSpec = DataSpec(uri)
            val fileDataSource = FileDataSource()
            try {
                fileDataSource.open(dataSpec)
            } catch (e: FileDataSource.FileDataSourceException) {
                e.printStackTrace()
            }
            val factoryDataSource = DataSource.Factory { fileDataSource }
            val audioSource = ProgressiveMediaSource.Factory(factoryDataSource).setTag(media)
                .createMediaSource(uri)
            mediaSourceArray[index] =
                if (eachTruck > 1) LoopingMediaSource(audioSource, eachTruck) else audioSource
        }
        return ConcatenatingMediaSource(*mediaSourceArray)
    }*/

/*    fun onlineSource(
        mediaList: List<Media>,
        eachTruck: Int = 1,
        wholeSet: Int = 1
    ): ConcatenatingMediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory(XplayerApplication.userAgent)
        val mediaSourceArray = arrayOfNulls<MediaSource>(mediaList.size)
        for (i in mediaList.indices) {
            val media = mediaList[i]
            val audioUri = Uri.parse(media.link)
            val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).setTag(media)
                .createMediaSource(audioUri)

            mediaSourceArray[i] =
                if (eachTruck > 1) LoopingMediaSource(audioSource, eachTruck) else audioSource
        }
        return ConcatenatingMediaSource(*mediaSourceArray)
    }*/


}