package com.brilliancesoft.mushaf.ui.audioPlayer.helpers

import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

object ExoPlayerCacheHelper {

    private val context = MushafApplication.appContext
    private const val DOWNLOAD_AUDIO_DIRECTORY = "downloads"
    private val databaseProvider: ExoDatabaseProvider = ExoDatabaseProvider(context)
    val userAgent: String = Util.getUserAgent(context, context.getString(R.string.app_name))

    val downloadCache: SimpleCache by lazy {
        SimpleCache(
            getDownloadDirectory(),
            NoOpCacheEvictor(),
            databaseProvider
        )
    }

    val playerCache: SimpleCache by lazy {
        SimpleCache(
            context.cacheDir,
            LeastRecentlyUsedCacheEvictor(20 * 1024 * 1024),
            databaseProvider
        )
    }

    val downloadManager: DownloadManager = DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            DefaultHttpDataSourceFactory(userAgent)
        )

    private fun getDownloadDirectory(): File =
        File(context.getExternalFilesDir(null), DOWNLOAD_AUDIO_DIRECTORY)

}