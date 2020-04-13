package com.brilliancesoft.mushaf.ui.quran.read

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.ui.quran.read.reciter.Constants.PLAYBACK_CHANNEL_ID
import com.brilliancesoft.mushaf.ui.quran.read.reciter.Constants.PLAYBACK_NOTIFICATION_ID
import com.brilliancesoft.mushaf.ui.quran.read.reciter.DownloadingFragment
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ExoPlayerListener
import com.brilliancesoft.mushaf.ui.quran.read.reciter.PlayerNotificationAdapter
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterBottomSheet
import com.brilliancesoft.mushaf.utils.extensions.addOnPageSelectedListener
import com.brilliancesoft.mushaf.utils.extensions.callClickOnSpan
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.brilliancesoft.mushaf.utils.toCurrentLanguageNumber
import com.codebox.lib.android.animators.simple.alphaAnimator
import com.codebox.lib.android.os.wait
import com.codebox.lib.android.utils.AppPreferences
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
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import javax.inject.Inject


class ReadQuranActivity : BaseActivity(true), View.OnClickListener {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MushafApplication.appComponent.inject(this)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var exoPlayer: ExoPlayer? = null
    private var playerListener: ExoPlayerListener? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var playWhenReady = true
    private var currentPageKey = "current read page"
    private val appPreference = AppPreferences()

    private var disposable: Disposable? = null
    private val job: Job = SupervisorJob()
    private lateinit var quranPagerAdapter: QuranPagerAdapter
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val viewModel: QuranViewModel by lazy {
        viewModelOf(
            QuranViewModel::class.java,
            viewModelFactory
        )
    }

    private var startAtPage = 1
    private var startAtSurah = -1

    @Suppress("EXPERIMENTAL_API_USAGE")
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_quran)
        val bundle = intent.extras

        var startAtAya: Aya? = null
        if (bundle != null) {
            val selectedAyaJson = bundle.getString(START_AT_AYA) ?: ""

            if (selectedAyaJson.isNotEmpty()) {
                startAtAya = Json.parse(Aya.serializer(), selectedAyaJson)
                startAtPage = startAtAya.page
                startAtSurah = startAtAya.surah_number
            } else {
                startAtSurah = bundle.getInt(START_AT_SURAH_KEY, -1)
                startAtPage = bundle.getInt(START_AT_PAGE_KEY)
            }
        }


        if (savedInstanceState != null)
            startAtPage = savedInstanceState.getInt(currentPageKey, startAtPage)

        viewModel.getMainMushaf().observer(this) { quranRawList ->
            val quranFormattedByPage = quranRawList.groupBy { it.page }
            if (savedInstanceState == null) {
                val aya = quranFormattedByPage.getValue(startAtPage).last()
                //createHizbToast(aya)
            }

            initViewPager(startAtPage, startAtAya, quranFormattedByPage)
        }
        viewModel.getMainMushaf()
        activateClickListener()
    }

    private fun goToAya(aya: Aya) {
        coroutineScope.launch(Dispatchers.IO) {
            var textView: QuranTextView? = null
            while (true) {
                if (textView != null) {
                    withContext(Dispatchers.Main) { textView?.callClickOnSpan(aya) }
                    break
                } else {
                    delay(150)
                    textView = withContext(Dispatchers.Main) { getSurahTextView(aya) }
                }
            }
        }
    }


    private fun initViewPager(
        startAtPage: Int,
        startAtAya: Aya?,
        quranFormattedByPage: Map<Int, List<Aya>>
    ) {
        quranViewpager.orientation =
            if (appPreference.getBoolean(SettingsPreferencesConstant.VerticalQuranPageKey))
                ViewPager2.ORIENTATION_VERTICAL
            else ViewPager2.ORIENTATION_HORIZONTAL

        quranPagerAdapter =
            QuranPagerAdapter(
                coroutineScope,
                this,
                quranFormattedByPage,
                startAtSurah
            )
        quranViewpager.adapter = quranPagerAdapter
        quranViewpager.setCurrentItem(startAtPage - 1, false)
        startAtAya?.let { goToAya(it) }

        /*openArrow.onClick {
            if (quranPagerAdapter.zoomOut)
                quranViewpager.restZoom()
            else
                quranViewpager.zoomOutPages(
                    resources.getDimension(R.dimen.pageMargin).toInt(),
                    resources.getDimension(R.dimen.pagerOffset).toInt()
                )
        }*/

        quranViewpager.addOnPageSelectedListener {
            //val aya = quranFormattedByPage.getValue(it + 1).last()
            //createHizbToast(aya)
            hideSystemUI()
        }
        viewModel.bookmarkedAyaBookmarked.observer(this) {
            bookmarkedImage.apply {
                translationY = (-height).toFloat()
                animate().translationY(0f).alpha(1f).setDuration(800)
                    .setInterpolator(AnticipateInterpolator()).withEndAction {
                        handler.wait(500) {
                            this.alphaAnimator(time = 450, valueTo = 0f)
                        }
                    }.start()
            }
        }
    }

    fun getSurahTextView(aya: Aya): QuranTextView? {
        var quranTextView: QuranTextView? = null
        val ayaFormatted = aya.getFormattedAya()

        // if (quranViewpager.currentItem != page)
        // quranViewpager.currentItem = page

        val surahsContainer: RecyclerView? =
            quranViewpager.findViewWithTag(QuranPagerAdapter.PAGE_CONTAINER_VIEW_TAG + aya.page)

        if (surahsContainer != null) {
            for (childNumber in 0 until surahsContainer.adapter!!.itemCount) {
                quranTextView =
                    surahsContainer.findViewHolderForAdapterPosition(childNumber)?.itemView?.pageTextQuran

                if (quranTextView == null) continue

                val currentAyaIdx = quranTextView.text.indexOf(ayaFormatted)
                if (currentAyaIdx != -1) break
            }
        }
        return quranTextView
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
                DefaultBandwidthMeter.Builder(this).build()
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
            setUseStopAction(true)
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
        const val START_AT_AYA = "start-at-aya"
        const val START_AT_PAGE_KEY = "start-at-page"
        const val START_AT_SURAH_KEY = "start-at-surah"
        private var currentPlayedAyat: List<Aya>? = null
        private var currentWindow = 0
        private var playbackPosition: Long = 0
    }

}
