package com.brilliancesoft.mushaf.ui.quran.read

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.Media
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.audioPlayer.MediaPlayerService
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.ui.quran.read.reciter.QuranPlayerListener
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterBottomSheet
import com.brilliancesoft.mushaf.utils.extensions.addOnPageSelectedListener
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.animators.simple.alphaAnimator
import com.codebox.lib.android.os.wait
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.viewGroup.get
import com.codebox.lib.android.views.utils.visible
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read_quran.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.android.synthetic.main.item_quran_page.view.*
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ReadQuranActivity : BaseActivity(true) {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MushafApplication.appComponent.inject(this)
    }

    private val playerServiceIntent by lazy { Intent(this, MediaPlayerService::class.java) }
    private val quranPlayerListener by lazy {
        QuranPlayerListener(this@ReadQuranActivity, playerService)
    }
    private lateinit var quranPagerAdapter: QuranPagerAdapter

    lateinit var playerService: MediaPlayerService

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var currentPageKey = "current read page"

    private val appPreference = AppPreferences()
    private var disposable: Disposable? = null
    private val job: Job = SupervisorJob()
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
            initViewPager(startAtPage, startAtAya, quranFormattedByPage)
        }

        viewModel.getMainMushaf()
        initPlayerView()
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

        quranPagerAdapter = QuranPagerAdapter(
            coroutineScope,
            this,
            quranFormattedByPage,
            startAtSurah
        )

        quranViewpager.adapter = quranPagerAdapter
        quranViewpager.setCurrentItem(startAtPage - 1, false)
        startAtAya?.let { goToAya(it) }
        var previousPageIndex = 0
        quranViewpager.addOnPageSelectedListener { pageIndex ->
            if (pageIndex != previousPageIndex) {
                val pageLayoutManager = getCurrentPageContainer(pageIndex)?.layoutManager
                (pageLayoutManager as? LinearLayoutManager)?.scrollToPosition(0)
                previousPageIndex = pageIndex
            }
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


    private fun initPlayerView() {
        stopPlayer.setOnClickListener(clickListener)
        playPauseButton.setOnClickListener(clickListener)
        playerSettings.setOnClickListener(clickListener)
    }

    fun playAyat(
        ayatList: List<Aya>,
        reciterEdition: Edition,
        eachVerse: Int,
        wholeSet: Int,
        playFromDownloadSource: Boolean
    ) {
        val playlist = ayatList.map { Media.create(it, reciterEdition) }
        playerService.playMedia(playlist, playFromDownloadSource, eachVerse, wholeSet)
        playerView.visible()
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
                    withContext(Dispatchers.Main) {
                        getSurahTextView(aya) { textView = it }
                    }
                }
            }
        }
    }

    fun getCurrentPageContainer(pageIndex: Int): RecyclerView? {
        val rootRecycler = quranViewpager[0] as RecyclerView
        return rootRecycler.layoutManager!!.findViewByPosition(pageIndex)?.pageSurahsRecycler

    }

    inline fun getSurahTextView(aya: Aya, crossinline onTextViewReady: (QuranTextView) -> Unit) {

        val pageIndex = aya.page - 1
        quranViewpager.setCurrentItem(pageIndex, false)

        var quranTextView: QuranTextView?
        val ayaFormatted = aya.getFormattedAya()

        quranViewpager.post {
            val surahsContainer: RecyclerView? = getCurrentPageContainer(pageIndex)

            if (surahsContainer != null) {
                for (childNumber in 0 until surahsContainer.adapter!!.itemCount) {
                    quranTextView =
                        surahsContainer.findViewHolderForAdapterPosition(childNumber)?.itemView?.pageTextQuran

                    if (quranTextView == null) continue
                    val currentAyaIdx = quranTextView!!.text.indexOf(ayaFormatted)
                    if (currentAyaIdx != -1) {
                        onTextViewReady(quranTextView!!)
                        break
                    }
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        systemUiVisibility(true)
    }

    override fun onPause() {
        super.onPause()
        preferences.put(PreferencesConstants.LastSurahViewed, quranViewpager.currentItem)
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            launchActivity<MainActivity>()
            finish()
        } else
            super.onBackPressed()

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

    override fun onStart() {
        super.onStart()
        bindService(playerServiceIntent, connection, Context.BIND_AUTO_CREATE)
    }


    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        job.cancelChildren()
        try {
            unbindService(connection)
            if (::playerService.isInitialized && !playerService.isPlaying)
                playerService.stopService(playerServiceIntent)
        } catch (ex: IllegalArgumentException) {
            print(ex.stackTrace)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            if (service is MediaPlayerService.ServiceBinder) {

                playerService = service.getService()
                playerService.setPlayerListener(quranPlayerListener)
                playerService.setPlayerView(playerView)

                (playerService.getCurrentPlayMedia()?.tag as? Aya)?.let {
                    playerView.visible()
                    getSurahTextView(it) {}
                }
            }
        }
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.playPauseButton -> {
                if (playerService.isPlaying) playPauseButton.setImageResource(R.drawable.ic_play)
                else playPauseButton.setImageResource(R.drawable.ic_pause)
                playerService.isPlaying = !playerService.isPlaying
            }
            R.id.stopPlayer -> {
                quranPlayerListener.terminatePlayer()
                playerService.releasePlayer()
            }
            R.id.playerSettings -> {
                val reciterBottomSheet = ReciterBottomSheet()
                reciterBottomSheet.isComingFromMediaPlayer = true
                reciterBottomSheet.show(supportFragmentManager, ReciterBottomSheet.TAG)
            }
        }
    }


    companion object {
        fun startNewActivity(context: Context, bundle: Bundle) {
            val intent = context.newIntent<ReadQuranActivity>().apply {
                putExtras(bundle)
            }

            context.stopService(Intent(context, MediaPlayerService::class.java))
            context.startActivity(intent)
        }

        const val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 101
        const val START_AT_AYA = "start-at-aya"
        const val START_AT_PAGE_KEY = "start-at-page"
        const val START_AT_SURAH_KEY = "start-at-surah"
    }

}
