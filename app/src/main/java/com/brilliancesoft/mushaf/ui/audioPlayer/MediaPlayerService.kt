package com.brilliancesoft.mushaf.ui.audioPlayer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Media
import com.brilliancesoft.mushaf.ui.audioPlayer.helpers.AudioOnlyRenderersFactory
import com.brilliancesoft.mushaf.ui.audioPlayer.helpers.MediaSourceBuilder

import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter

class MediaPlayerService : Service() {

    lateinit var audioPlayer: ExoPlayer
    private val serviceBinder = ServiceBinder()
    private var isRadio: Boolean = false

    private var exoMediaSource: MediaSource? = null
        private set
    private val bandwidthMeter =
        DefaultBandwidthMeter.Builder(MushafApplication.appContext).build()
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var playerListener: Player.EventListener? = null
    private var isReleased = false
    private var playerView: PlayerControlView? = null

    var isPlaying: Boolean = false
        get() {
            field = audioPlayer.playWhenReady
            return field
        }
        set(value) {
            audioPlayer.playWhenReady = value
            field = value
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bindNotificationManger()
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return serviceBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (playerListener != null) audioPlayer.removeListener(playerListener)
        if (audioPlayer.playbackState == (Player.STATE_IDLE)) stopSelf()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        if (!this::audioPlayer.isInitialized) initializePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun initializePlayer() {
        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()

        audioPlayer = ExoPlayerFactory.newSimpleInstance(
            applicationContext,
            AudioOnlyRenderersFactory(applicationContext),
            DefaultTrackSelector(adaptiveTrackSelectionFactory),
            DefaultLoadControl(),
            null,
            bandwidthMeter
        )

        audioPlayer.setForegroundMode(true)
        isReleased = false
    }

    fun setPlayerView(view: PlayerControlView?) {
        if (view != null) {
            playerView = view
            view.player = audioPlayer
        }

    }

    private fun ensurePlayerIsReady() {
        if (isReleased) {
            initializePlayer()
            setPlayerView(playerView)
            setPlayerListener(playerListener)
        }
    }

    fun playMedia(
        list: List<Media>,
        playOffline: Boolean,
        playEachTrack: Int,
        playWholeSet: Int
    ) {
        ensurePlayerIsReady()

        exoMediaSource = MediaSourceBuilder.create(list, playEachTrack, playWholeSet, playOffline)

        currentPlayList = list
        //resting saved position for the new media create.
        currentWindow = 0
        playbackPosition = 0
        audioPlayer.prepare(exoMediaSource, true, true)
        audioPlayer.seekTo(currentWindow, C.TIME_UNSET)
        isRadio = false
        audioPlayer.playWhenReady = true
    }

    fun resumePlayer() {
        ensurePlayerIsReady()
        if (exoMediaSource != null) {
            if (audioPlayer.playbackState == Player.STATE_ENDED) {
                audioPlayer.prepare(exoMediaSource, true, true)
            }

            audioPlayer.seekTo(currentWindow, playbackPosition)
            audioPlayer.playWhenReady = true
        }
    }

    fun pausePlayer() {
        audioPlayer.playWhenReady = false
        playbackPosition = audioPlayer.currentPosition
        currentWindow = audioPlayer.currentWindowIndex
    }

    fun releasePlayer() {
        audioPlayer.release()
        playerNotificationManager?.setPlayer(null)
        currentWindow = 0
        playbackPosition = 0
        stopForeground(true)
        isReleased = true
        _currentPlayMedia.postValue(null)
        audioPlayer.removeListener(playerListener)
    }

    fun setPlayerListener(exoPlayerListener: Player.EventListener?) {
        playerListener = exoPlayerListener
        audioPlayer.addListener(exoPlayerListener)
    }

/*    private fun restartPlayer() {
        releasePlayer()
        ensurePlayerIsReady()
        exoMediaSource = MediaLinkBuilder.create(currentPlayList)
    }*/

    fun updateCurrentPlayMedia() {
        _currentPlayMedia.postValue(if (isPlaying) getCurrentPlayMedia() else null)
    }

    fun getCurrentPlayMedia(): Media? {
        return audioPlayer.currentTag as? Media
    }

    fun clearNotification() {
        playerNotificationManager?.setPlayer(null)
    }

    private fun bindNotificationManger() {
        playerNotificationManager =
            PlayerNotificationManager.createWithNotificationChannel(
                MushafApplication.appContext,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                R.string.playback_channel_description,
                PLAYBACK_NOTIFICATION_ID,
                NotificationDescriptionAdapter(currentPlayList),
                object : PlayerNotificationManager.NotificationListener {

                    override fun onNotificationCancelled(
                        notificationId: Int,
                        dismissedByUser: Boolean
                    ) = releasePlayer()

                    override fun onNotificationStarted(
                        notificationId: Int,
                        notification: Notification?
                    ) = startForeground(notificationId, notification)

                }
            )
        playerNotificationManager!!.apply {
            setColor(Color.BLACK)
            setColorized(true)
            setUseChronometer(false)
            setUseStopAction(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            setPlayer(audioPlayer)
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    companion object {
        private var currentPlayList: List<Media> = mutableListOf()
        private val _currentPlayMedia = MutableLiveData<Media?>()
        fun currentPlayMedia(): LiveData<Media?> = _currentPlayMedia
        private const val PLAYBACK_CHANNEL_ID = "mushaf_playback_channel"
        private const val PLAYBACK_NOTIFICATION_ID = 11678
    }

}
