package co.jp.smagroup.musahaf.ui.quran.read

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.widget.NestedScrollView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.CustomToast
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.commen.ViewModelFactory
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.ui.quran.read.reciter.Constants.PLAYBACK_CHANNEL_ID
import co.jp.smagroup.musahaf.ui.quran.read.reciter.Constants.PLAYBACK_NOTIFICATION_ID
import co.jp.smagroup.musahaf.ui.quran.read.reciter.DownloadingFragment
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ExoPlayerListener
import co.jp.smagroup.musahaf.ui.quran.read.reciter.PlayerNotificationAdapter
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ReciterBottomSheet
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.utils.extensions.addOnPageSelectedListener
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.updatePadding
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import co.jp.smagroup.musahaf.utils.toCurrentLanguageNumber
import com.codebox.lib.android.utils.screenHelpers.dp
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read_quran.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.coroutines.*
import javax.inject.Inject


class ReadQuranActivity : BaseActivity(true), View.OnClickListener {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MushafApplication.appComponent.inject(this)
    }

    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var exoPlayer: ExoPlayer? = null
    private var playerListener: ExoPlayerListener? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var playWhenReady = true
    private var currentPageKey = "current read page"

    private var disposable: Disposable? = null
    private val job: Job = SupervisorJob()
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val viewModel: QuranViewModel by lazy {
        viewModelOf(
            QuranViewModel::class.java,
            viewModelFactory
        )
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_quran)
        val bundle = intent.extras
        //Extract the data.
        var startAtPage = bundle?.getInt(START_AT_PAGE_KEY) ?: 1

        if (savedInstanceState != null) {
            startAtPage = savedInstanceState.getInt(currentPageKey, startAtPage)
        }


        viewModel.getMainMushaf().observer(this) {

            val data = it.groupBy { it.page }

            if (savedInstanceState == null) {
                val aya = data[startAtPage]!!.last()
                createHizbToast(aya)
            }

            initViewPager(startAtPage, data)
        }

        activateClickListener()
    }


    fun updateBookmarkState(aya: Aya) {
        coroutineScope.launch(Dispatchers.IO) {
            repository.updateBookmarkStatus(aya.number, aya.edition!!.identifier, !aya.isBookmarked)
        }
        viewModel.updateBookmarkStateInData(aya)
    }

    private fun initViewPager(
        startAtPage: Int,
        data: Map<Int, List<Aya>>
    ) {
        quranViewpager.adapter = ReadQuranPagerAdapter(this, data, coroutineScope)

        //quranViewpager.adapter = VerticalAdapter(this, data)

        quranViewpager.setCurrentItem(startAtPage - 1, false)

        quranViewpager.addOnPageSelectedListener {
            //updateSystemNavState(true)
            val aya = data[it + 1]!!.last()
            createHizbToast(aya)
            if (playerView.isShown) updatePagerPadding(dp(80))
            else updatePagerPadding(0)
            updateSystemNavState(true)
        }

    }

    private fun createHizbToast(aya: Aya) {

        val hizbQuarters = aya.hizbQuarter
        val integerNumberOfHizb = hizbQuarters / 4
        val floatingNumberOfHizb = hizbQuarters % 4

        val floatingText =
            if (integerNumberOfHizb != 0) "$integerNumberOfHizb $floatingNumberOfHizb/4"
            else "${floatingNumberOfHizb}/" + 4

        val toastText =
            "${getString(R.string.hizb)} " + if (floatingNumberOfHizb == 0) integerNumberOfHizb else floatingText

        CustomToast.make(this, toastText.toCurrentLanguageNumber(), Toast.LENGTH_LONG)
    }

    private fun initializePlayer() {
        if (exoPlayer == null && currentPlayedAyat != null) {
            // a factory to create an AdaptiveVideoTrackSelection
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()

            exoPlayer = ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(adaptiveTrackSelectionFactory), DefaultLoadControl(), null,
                BANDWIDTH_METER
            )
            playerView.player = exoPlayer
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.seekTo(
                currentWindow,
                playbackPosition
            )
            playerListener = ExoPlayerListener(
                this,
                currentPlayedAyat,
                exoPlayer!!
            )
            exoPlayer!!.addListener(playerListener)
            if (exoMediaSource != null) {
                //if exoMediaSource not null then resume player with previous media source. This happens when activity configuration changed or resumed.
                resumePlayer()
                initNotificationManger(currentPlayedAyat!!)
            } else {
                //resting saved position for the new media source.
                currentWindow = 0
                playbackPosition = 0
            }
            playerNotificationManager!!.setPlayer(exoPlayer)
        }
    }

    private fun resumePlayer() {
        playerView.show()
        updatePagerPadding(dp(80))
        playPauseButton.setImageResource(R.drawable.ic_pause)
        exoPlayer!!.prepare(exoMediaSource, false, false)
    }

    fun releasePlayer() {
        exoPlayer?.let {
            playerView.hide()
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            playerNotificationManager?.setPlayer(null)
            it.removeListener(playerListener)
            exoPlayer = null
        }
    }

    fun setExoPLayerMediaSource(
        newMediaSource: MediaSource,
        selectedAyat: List<Aya>
    ) {
        currentPlayedAyat = selectedAyat
        initNotificationManger(selectedAyat)

        //Resetting the exoMediaSource and re-instantiate exo-player.
        exoMediaSource = null
        releasePlayer()
        initializePlayer()

        updatePagerPadding(dp(80))
        exoPlayer?.prepare(newMediaSource, true, true)
        playPauseButton.setImageResource(R.drawable.ic_pause)
        playerView.show()
        exoMediaSource = newMediaSource
    }

    private fun initNotificationManger(playList: List<Aya>) {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this, PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name,
            PLAYBACK_NOTIFICATION_ID, PlayerNotificationAdapter(playList, this)
        )
        playerNotificationManager!!.apply {
            setColor(Color.BLACK)
            setColorized(true)
            setUseChronometer(false)
            setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.playerSettings -> {
                val reciterBottomSheet = ReciterBottomSheet()
                reciterBottomSheet.isComingFromMediaPlayer = true
                reciterBottomSheet.show(supportFragmentManager, ReciterBottomSheet.TAG)
            }

            R.id.playPauseButton -> {
                exoPlayer?.let {
                    it.playWhenReady = !it.playWhenReady
                    if (it.playWhenReady) playPauseButton.setImageResource(R.drawable.ic_pause)
                    else playPauseButton.setImageResource(R.drawable.ic_play)
                }
            }

            R.id.stopPlayer -> {
                playerListener?.clearAllHighlighted()
                playerView.hide()
                releasePlayer()
                updatePagerPadding(0)
            }
        }
    }

    private fun activateClickListener() {
        stopPlayer.setOnClickListener(this)
        playPauseButton.setOnClickListener(this)
        playerSettings.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        systemUiVisibility(true)
        if (Util.SDK_INT <= 23)
            initializePlayer()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        preferences.put(PreferencesConstants.LastSurahViewed, quranViewpager.currentItem)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        job.cancelChildren()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exoMediaSource = null
        currentPlayedAyat = null
        releasePlayer()
        DownloadingFragment.playerDownloadingCancelled.onNext(true)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (exoPlayer != null) {
            releasePlayer()
            initializePlayer()
        }
    }

    fun updatePagerPadding(pad: Int) {
        val scrollView =
            quranViewpager.findViewWithTag<NestedScrollView>("pageScroller${quranViewpager.currentItem}")
        scrollView?.updatePadding(bottom = pad)
    }

    fun updateSystemNavState(ifVisibleOnly: Boolean) {

        val systemUiVisibility = window.decorView.systemUiVisibility

        if (ifVisibleOnly) {
            if ((systemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN) != systemUiVisibility)
                hideSystemUI()
        } else {
            if ((systemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN) != systemUiVisibility)
                hideSystemUI()
            else
                showSystemUI()
        }

    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(currentPageKey, quranViewpager.currentItem + 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onPermissionGiven?.invoke()
            else CustomToast.makeShort(
                this,
                "Cannot save audio files without the requested permission"
            )
        }
    }

    companion object {
        private var exoMediaSource: MediaSource? = null
        const val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 101
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
        const val START_AT_PAGE_KEY = "start-at-page"
        private var currentPlayedAyat: List<Aya>? = null
        private var currentWindow = 0
        private var playbackPosition: Long = 0
    }

}
