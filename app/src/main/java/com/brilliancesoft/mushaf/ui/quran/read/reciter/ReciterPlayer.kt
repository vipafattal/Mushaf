package com.brilliancesoft.mushaf.ui.quran.read.reciter

import android.util.Log
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.api.FetchDownloadListener
import com.brilliancesoft.mushaf.framework.commen.MediaSourceBuilder
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.framework.utils.ReciterRequestGenerator
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Reciter
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.utils.extensions.observeOnMainThread
import com.codebox.lib.android.fragments.replaceFragment
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import kotlinx.coroutines.*

class ReciterPlayer(
    private val readQuranActivity: ReadQuranActivity,
    private val repository: Repository,
    private val playList: List<Aya>,
    private val eachVerse: Int,
    private val wholeSet: Int
) {
    private val playListWithoutLoopedAyat = playList.distinctBy { it.number }

    private val job = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val fetch: Fetch? =
        Fetch.Impl.getInstance(MushafApplication.appContext.fetchConfiguration())
    private lateinit var downloadListener: FetchDownloadListener

    fun play(
        isStreamingOnline: Boolean,
        playRange: IntRange,
        selectedReciterId: String,
        selectedReciterName: String
    ) {
        if (isStreamingOnline)
            playOnline(playRange, selectedReciterId, eachVerse, wholeSet)
        else {
            readQuranActivity.executeWithPendingPermission(ReadQuranActivity.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
                coroutineScope.launch {
                    val downloadedReciter = getDownloadedReciters(playRange, selectedReciterId)
                    var notDownloadedAyaRequest = mutableListOf<Request>()
                    if (downloadedReciter.isNotEmpty()) {
                        val _playRange =
                            if (playRange.start != playRange.last) playRange else playRange.start until playRange.start
                        for (ayaNumber in _playRange)
                        //downloadedReciter.firstOrNull { it.number == number } == null means this aya is not downloaded.
                            if (downloadedReciter.firstOrNull { it.number == ayaNumber } == null) {
                                val request = ReciterRequestGenerator.createRequestFromFile(
                                    selectedReciterName,
                                    selectedReciterId,
                                    playListWithoutLoopedAyat[0].surah!!,
                                    ayaNumber
                                )
                                notDownloadedAyaRequest.add(request)
                            }
                    } else {
                        notDownloadedAyaRequest = playListWithoutLoopedAyat.map {
                            ReciterRequestGenerator.createRequestFromFile(
                                selectedReciterName,
                                selectedReciterId,
                                it.surah!!,
                                it.number
                            )
                        }.toMutableList()
                    }

                    if (notDownloadedAyaRequest.isNotEmpty()) {
                        readQuranActivity.replaceFragment(
                            DownloadingFragment(),
                            R.id.down_reciter_fragment_holder
                        )

                        fetch!!.enqueue(notDownloadedAyaRequest)
                        addDownloadListener(
                            notDownloadedAyaRequest.size,
                            selectedReciterId,
                            selectedReciterName,
                            playRange
                        )
                    } else playOffline(downloadedReciter, eachVerse, wholeSet)

                }
            }
        }
    }

    private suspend fun getDownloadedReciters(playRange: IntRange, selectedReciterName: String) =
        withContext(Dispatchers.IO) {
            repository.getReciterDownloads(
                playRange.first,
                playRange.last,
                selectedReciterName
            )
        }


    private fun addDownloadListener(
        numberOfRequest: Int,
        selectedReciterId: String,
        selectedReciterName: String,
        playRange: IntRange
    ) {
        downloadListener = FetchDownloadListener(
            numberOfRequest,
            selectedReciterName,
            selectedReciterId,
            playListWithoutLoopedAyat,
            repository,
            readQuranActivity, coroutineScope
        ) {
            if (fetch != null && !fetch.isClosed) {
                fetch.removeListener(it)
                fetch.close()
            }
            onDownloadingCompleted(playRange, selectedReciterId)
        }
        fetch!!.addListener(downloadListener)
        downloadingCancelledListener()
    }


    private fun onDownloadingCompleted(playRange: IntRange, selectedReciterId: String) {
        coroutineScope.launch {
            val newDownloadedReciter =
                withContext(Dispatchers.IO) {
                    repository.getReciterDownloads(
                        playRange.first,
                        playRange.last,
                        selectedReciterId
                    )
                }


            var isError = false
            for (number in playRange) {
                //Checking if not contains this number, if contains
                if (newDownloadedReciter.firstOrNull { it.number == number } == null) {
                    CustomToast.makeShort(readQuranActivity, R.string.error_downloading)
                    isError = true
                    break
                }
            }
            if (!isError)
                playOffline(newDownloadedReciter, eachVerse, wholeSet)
        }
    }

    private fun playOnline(
        playRange: IntRange,
        selectedReciterId: String,
        eachVerse: Int,
        wholeSet: Int
    ) {
        val links = MediaSourceBuilder.linksGenerator(playRange, selectedReciterId)
        val mediaSource = MediaSourceBuilder.onlineSource(links, eachVerse, wholeSet)
        readQuranActivity.setExoPLayerMediaSource(mediaSource, playList)
    }

    private fun playOffline(downloadedReciter: List<Reciter>, eachVerse: Int, wholeSet: Int) {
        val uriArray = downloadedReciter.map { it.uri!! }.toTypedArray()
        val mediaSource = MediaSourceBuilder.offlineSource(uriArray, eachVerse, wholeSet)
        readQuranActivity.setExoPLayerMediaSource(mediaSource, playList)
    }

    private fun downloadingCancelledListener() {
        DownloadingFragment.playerDownloadingCancelled.observeOnMainThread {
            Log.d("Downloading cancelled", it.toString())
            if (it) {
                if (fetch != null && !fetch.isClosed) {
                    fetch.removeListener(downloadListener)
                    fetch.cancelAll()
                    fetch.close()
                }
                job.cancelChildren()
            }
        }
    }
}