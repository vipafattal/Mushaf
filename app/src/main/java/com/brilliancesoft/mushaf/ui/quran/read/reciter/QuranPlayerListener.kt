package com.brilliancesoft.mushaf.ui.quran.read.reciter

import android.content.Intent
import android.text.Spannable
import android.widget.TextView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.audioPlayer.MediaPlayerService
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.ui.quran.read.helpers.RecitingHighlighter
import com.brilliancesoft.mushaf.utils.extensions.toSpannable
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_read_quran.*

class QuranPlayerListener(
    private val readQuranActivity: ReadQuranActivity,
    private val playerService: MediaPlayerService
) : Player.EventListener {

    private val highlightedColor = Colour(R.color.colorPlayHighlight, readQuranActivity)
    private var highlightedTextView: QuranTextView? = null
    private var currentPlayAya: Aya? = null

    private fun getCurrentMediaAya(): Aya? {
        return if (playerService.isPlaying)
            playerService.getCurrentPlayMedia()?.tag as? Aya
        else
            null
    }

    override fun onPositionDiscontinuity(reason: Int) {
        getCurrentMediaAya()?.let { highlightAya(it) }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val exoPlayerErrorTag = "exoPlayerErrorTag"
        MushafToast.makeShort(readQuranActivity, R.string.playing_error)

        val crashlytics = FirebaseCrashlytics.getInstance()

        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> crashlytics.log(
                "TYPE_SOURCE: $exoPlayerErrorTag" + ", Message " +
                        error.message
            )
            ExoPlaybackException.TYPE_RENDERER -> crashlytics.log(
                "TYPE_RENDERER: $exoPlayerErrorTag" + ", Message " +
                        error.message
            )
            ExoPlaybackException.TYPE_UNEXPECTED -> crashlytics.log(
                "TYPE_UNEXPECTED: $exoPlayerErrorTag" + ", Message " + error.message
            )
            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                "TYPE_OUT_OF_MEMORY: $exoPlayerErrorTag" + ", Message " + error.message
            }
            ExoPlaybackException.TYPE_REMOTE -> {
                "TYPE_REMOTE: $exoPlayerErrorTag" + ", Message " + error.message
            }
        }
        terminatePlayer()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            //The player is able to immediately play from the current position. This means the player does actually play media when playWhenReady is true. If it is false the player is paused.
            Player.STATE_READY -> {
                playerService.setPlayerView(readQuranActivity.playerView)
                readQuranActivity.playerView?.visible()
                val intent = Intent(readQuranActivity, MediaPlayerService::class.java)
                Util.startForegroundService(MushafApplication.appContext, intent)
                getCurrentMediaAya()?.let { highlightAya(it) }
            }
            //The player has finished playing the all passed media.
            Player.STATE_ENDED -> terminatePlayer()
        }
    }

    private fun highlightAya(aya: Aya) {
        highlightedTextView?.run { text.toSpannable().clearHighlighted() }
        readQuranActivity.getSurahTextView(aya) {
            highlightedTextView = it
            val ayaFormatted = aya.getFormattedAya()
            val currentAyaIdx = highlightedTextView?.text?.indexOf(ayaFormatted) ?: -1
            currentPlayAya = aya
            if (currentAyaIdx != -1) highlightedTextView!!.highlight(
                currentAyaIdx,
                currentAyaIdx + ayaFormatted.length - 1
            )
            else Log.d("aya not highlighted", "Error text not found")
        }
    }

    private fun TextView.highlight(start: Int, end: Int) {
        text.toSpannable().run {
            clearHighlighted()
            setSpan(RecitingHighlighter(highlightedColor), start, end, 0)
        }
    }

    private fun Spannable.clearHighlighted(start: Int = 0, end: Int = length) {
        val styleSpans: Array<out RecitingHighlighter> =
            getSpans(start, end, RecitingHighlighter::class.java)
        for (style in styleSpans)
            removeSpan(style)
    }

    private fun clearAllHighlighted() {
        for (textNumber in 0..4)
            currentPlayAya?.let {
                highlightedTextView?.text?.toSpannable()?.clearHighlighted()

            }
    }

    fun terminatePlayer() {
        clearAllHighlighted()
        playerService.isPlaying = false
        playerService.clearNotification()
        readQuranActivity.playerView?.gone()
    }
}