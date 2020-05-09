package com.brilliancesoft.mushaf.ui.quran.read.reciter

import com.brilliancesoft.mushaf.framework.data.local.MetadataRepository
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.audioPlayer.helpers.DownloadUtils
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import kotlinx.coroutines.*

class ReciterPlayer(
    private val playList: List<Aya>,
    private val readQuranActivity: ReadQuranActivity,
    private val repository: MetadataRepository,
    private val reciterEdition: Edition,
    private val eachVerse: Int,
    private val wholeSet: Int
) {
    private val job = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    fun play(
        isStreamingOnline: Boolean,
        playRange: IntRange
    ) {
        if (isStreamingOnline)
            playOnline(eachVerse, wholeSet)
        else {
            readQuranActivity.executeWithPendingPermission(ReadQuranActivity.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
                coroutineScope.launch {

                    val downloadedReciter =
                        withContext(Dispatchers.IO) { repository.getDownloadUri(reciterEdition.identifier) }
                    val ayatToDownload = mutableListOf<Int>()
                    //Noting is Downloaded for the following reciter edition.
                    if (downloadedReciter.isEmpty()) {
                        //Download whole Range.
                        DownloadUtils.download(playRange, reciterEdition)
                    } else {
                        val _playRange =
                            if (playRange.start != playRange.last) playRange else playRange.start until playRange.start
                        for (ayaNumber in _playRange)
                        //downloadedReciter.firstOrNull { it.number == number } == null means this aya is not downloaded.
                            if (downloadedReciter.firstOrNull { it.ayaNumber == ayaNumber } == null)
                                ayatToDownload.add(ayaNumber)

                    }

                    for (ayaNumber in ayatToDownload)
                        DownloadUtils.download(ayaNumber, reciterEdition)

                    playOffline(eachVerse, wholeSet)
                }
            }
        }
    }

    private fun playOnline(
        eachVerse: Int,
        wholeSet: Int
    ) {
        readQuranActivity.playAyat(playList, reciterEdition, eachVerse, wholeSet, false)
    }

    private fun playOffline(
        eachVerse: Int,
        wholeSet: Int
    ) {
        readQuranActivity.playAyat(playList, reciterEdition, eachVerse, wholeSet, true)
    }
}