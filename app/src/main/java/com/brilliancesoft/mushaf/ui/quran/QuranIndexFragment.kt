package com.brilliancesoft.mushaf.ui.quran

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.commen.PreferencesConstants
import com.brilliancesoft.mushaf.ui.commen.ViewModelFactory
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.NavigateToPageDialog
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.ui.quran.sharedComponent.BaseFragment
import com.brilliancesoft.mushaf.ui.search.SearchActivity
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.onScroll
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_quran.*
import kotlinx.android.synthetic.main.toolbar_main.*
import javax.inject.Inject


class QuranIndexFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    init {
        MushafApplication.appComponent.inject(this)
    }

    private lateinit var viewModel: QuranViewModel
    private lateinit var parentActivity: MainActivity
    private val preferences = AppPreferences()

    override val layoutId: Int = R.layout.fragment_quran

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parentActivity = activity as MainActivity
        parentActivity.updateToolbar(R.string.quran, R.drawable.ic_search, R.drawable.ic_read)
        viewModel = viewModelOf(QuranViewModel::class.java, viewModelFactory)

        initToolbar()

        viewModel.getMainMushaf().observer(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                dispatchAyatData(it)
            } else {
                loadingCompleted(true)
                errorViewVisible()
            }
        }
        loadData()
    }

    override fun loadData() {
        super.loadData()
        viewModel.prepareData()
    }


    private fun dispatchAyatData(ayat: List<Aya>){
        fast_page_transition.visible()
        parentActivity.bottom_app_nav.visible()

        loadingCompleted(false)
        Log.d("DBFLOW BY QURAN", "size ${ayat.size} $ayat")
        val data = convertToQuranList(ayat)

        initRecyclerView(data)
        showToolbarActions()
    }


    private fun initRecyclerView(data: List<Aya>) {
        quranlist_recyclerView.adapter = QuranIndexAdapter(data, false)

        fast_page_transition.onClick {
            fragmentManager?.let {
                val pageNavDialog = NavigateToPageDialog()
                pageNavDialog.show(it,NavigateToPageDialog.TAG)
            }
        }

        val layoutManager = quranlist_recyclerView.layoutManager as LinearLayoutManager
        quranlist_recyclerView.onScroll { _, dy ->
            if (dy > 0) fast_page_transition.shrink()
            else if (layoutManager.findFirstVisibleItemPosition() < 1) fast_page_transition.extend()

        }

    }

    private fun initToolbar() {
        parentActivity.endToolbar_icon.onClick {
            val intent = parentActivity.newIntent<ReadQuranActivity>()
            val bundle = Bundle()
            val lastPage = preferences.getInt(PreferencesConstants.LastSurahViewed, 0)
            bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, lastPage + 1)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        parentActivity.beginToolbar_icon.onClick {
            parentActivity.launchActivity<SearchActivity>()
        }
    }

    private fun convertToQuranList(ayat: List<Aya>): MutableList<Aya> {
        val adapterList = mutableListOf<Aya>()
        var oldJuz = -1
        var oldSurahNumber = -1
        for (aya in ayat) {
            if (aya.surah_number != oldSurahNumber || aya.juz != oldJuz)
                adapterList.add(aya)
            oldJuz = aya.juz
            oldSurahNumber = aya.surah_number
        }
        return adapterList
    }


    private fun showToolbarActions() {

        val lastPageRead = parentActivity.endToolbar_icon
        val searchButton = parentActivity.beginToolbar_icon

        lastPageRead.visible()
        searchButton.visible()

        lastPageRead.animate().alpha(1f)
        searchButton.animate().alpha(1f)
    }
}
