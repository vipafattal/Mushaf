package co.jp.smagroup.musahaf.framework.commen

import android.net.Uri
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.more.SettingsPreferencesConstant
import com.codebox.lib.android.resoures.Stringer
import com.codebox.lib.android.utils.AppPreferences
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Util

object MediaSourceBuilder {
    private val preferences = AppPreferences()

    private val qualityValues = arrayOf("low", "", "high")

    @Suppress("DEPRECATION")
    fun onlineSource(
        mediaList: Array<String>,
        eachVerse: Int,
        wholeSet: Int
    ): MediaSource {
        val userAgent = Util.getUserAgent(MushafApplication.appContext, Stringer(R.string.app_name))
        val dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        val mediaSourceArray = arrayOfNulls<MediaSource>(mediaList.size)
        for (i in mediaList.indices) {
            val uri = mediaList[i]
            val audioUri = Uri.parse(uri)
            val audioSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)

            /*  if (i == 0)
                  mediaSourceArray[i] = LoopingMediaSource(audioSource, 2)
              else*/
            mediaSourceArray[i] = if (eachVerse > 1) LoopingMediaSource(audioSource, eachVerse) else audioSource
        }
        return if (wholeSet > 1) LoopingMediaSource(
            ConcatenatingMediaSource(*mediaSourceArray),
            wholeSet
        ) else ConcatenatingMediaSource(*mediaSourceArray)
    }

    fun offlineSource(
        uriArray: Array<Uri>,
        eachVerse: Int,
        wholeSet: Int
    ): MediaSource {
        val mediaSourceArray = arrayOfNulls<MediaSource>(uriArray.size)
        for ((index, uri) in uriArray.withIndex()) {
            val dataSpec = DataSpec(uri)
            val fileDataSource = FileDataSource()
            try {
                fileDataSource.open(dataSpec)
            } catch (e: FileDataSource.FileDataSourceException) {
                e.printStackTrace()
            }
            val factoryDataSource = DataSource.Factory { fileDataSource }
            val audioSource =
                ProgressiveMediaSource.Factory(factoryDataSource).createMediaSource(uri)
            mediaSourceArray[index] = if (eachVerse > 1) LoopingMediaSource(audioSource, eachVerse) else audioSource
        }
        return if (wholeSet > 1) LoopingMediaSource(
            ConcatenatingMediaSource(*mediaSourceArray),
            wholeSet
        ) else ConcatenatingMediaSource(*mediaSourceArray)
    }

    fun linksGenerator(
        selectedAyaRange: IntRange,
        reciterIdentifier: String): Array<String> {
        val urlArray = mutableListOf<String>()
        val quality = getAudioQuality()
        if (selectedAyaRange.start != selectedAyaRange.last)
            for (aya in selectedAyaRange) {
                val url = "http://cdn.alquran.cloud/media/audio/ayah/$reciterIdentifier/$aya$quality"
                urlArray.add(url)
            }
        else
            urlArray.add("http://cdn.alquran.cloud/media/audio/ayah/$reciterIdentifier/${selectedAyaRange.start}$quality")
        return urlArray.toTypedArray()
    }

    private fun getAudioQuality(): String {
        val audioQuality = preferences.getInt(SettingsPreferencesConstant.AudioQualityKey, 1)
        return if (audioQuality == 1) qualityValues[audioQuality] else "/ ${qualityValues[audioQuality]}"
    }

    fun linkGenerator(ayaNumber: Int, reciterIdentifier: String): String =
        "http://cdn.alquran.cloud/media/audio/ayah/$reciterIdentifier/$ayaNumber"

}