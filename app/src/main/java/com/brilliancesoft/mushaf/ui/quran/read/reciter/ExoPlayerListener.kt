package com.brilliancesoft.mushaf.ui.quran.read.reciter

import android.text.Spannable
import android.widget.TextView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.ui.quran.read.helpers.RecitingHighlighter
import com.brilliancesoft.mushaf.utils.extensions.toSpannable
import com.codebox.lib.android.resoures.Colour
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ExoPlayerListener(
    private val readQuranActivity: ReadQuranActivity,
    private val playlist: List<Aya>?,
    private val exoPlayer: ExoPlayer
) : Player.EventListener {

    private val highlightedColor = Colour(R.color.colorPlayHighlight, readQuranActivity)
    private var highlightedTextView: QuranTextView? = null
    private var currentPlayAya: Aya? = null

    override fun onPositionDiscontinuity(reason: Int) {
        val page = exoPlayer.currentWindowIndex
        val aya = playlist?.get(page)
        if (aya != null)
            highlightAya(aya)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val exoPlayerErrorTag = "exoPlayerErrorTag"
        CustomToast.makeShort(readQuranActivity, R.string.playing_error)

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
                val p = exoPlayer.currentWindowIndex
                val aya = playlist?.get(p)
                if (aya != null) highlightAya(aya)
            }
            //The player has finished playing the all passed media.
            Player.STATE_ENDED -> terminatePlayer()
        }
    }

    private fun highlightAya(aya: Aya) {
        if (highlightedTextView == null ||
            currentPlayAya != null &&
            (currentPlayAya!!.page != aya.page || currentPlayAya!!.surah_number != aya.surah_number)
        )
            highlightedTextView = readQuranActivity.getSurahTextView(aya)

        val ayaFormatted = aya.getFormattedAya()
        val currentAyaIdx = highlightedTextView?.text?.indexOf(ayaFormatted) ?: -1
        currentPlayAya = aya
        if (currentAyaIdx == -1) CustomToast.makeShort(readQuranActivity, "Error text not found")
        else highlightedTextView!!.highlight(currentAyaIdx, currentAyaIdx + ayaFormatted.length - 1)
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

    fun clearAllHighlighted() {
        for (textNumber in 0..4) {
            currentPlayAya?.let {
                highlightedTextView?.text?.toSpannable()?.clearHighlighted()
            }
        }
    }

    private fun terminatePlayer() {
        readQuranActivity.releasePlayer()

        clearAllHighlighted()
        exoPlayer.removeListener(this)
    }
}