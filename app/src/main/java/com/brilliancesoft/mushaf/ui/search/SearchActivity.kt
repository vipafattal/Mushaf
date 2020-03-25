package com.brilliancesoft.mushaf.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.framework.utils.TextTypeOpt
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.commen.ViewModelFactory
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.utils.extensions.*
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.*
import javax.inject.Inject


class SearchActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {


    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @TextTypeOpt
    private var searchType: String = Edition.Quran

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private var quranWithoutTashkil = mutableListOf<Aya>()
    private var isSearchableQuranReady = false

    private val searchableQuranViewModel: SearchableQuranViewModel by lazy {
        viewModelOf(SearchableQuranViewModel::class.java, viewModelFactory)
    }
    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenOrientation()
        setContentView(R.layout.activity_search)
        initGroupChipListener()
        initSearch()
        back_button_search.onClick { finish() }

        searchableQuranViewModel.getSearchableMushaf().observer(this) {
            quranWithoutTashkil = it as MutableList<Aya>
            isSearchableQuranReady = true
        }

    }


    private fun initSearch() {
        search_text_input.setOnEditorActionListener { _, actionId, _ ->
            empty_data_text.gone()

            if (actionId == EditorInfo.IME_ACTION_SEARCH && !search_text_input.text.isNullOrBlank() && !loading_search_result.isVisible) onSearch()
            else if (loading_search_result.isVisible) CustomToast.makeShort(this@SearchActivity, R.string.wait)
            else CustomToast.makeShort(this@SearchActivity, R.string.empty_search_query)

            true
        }
    }

    private fun onSearch() =
        runBlocking {
            coroutineScope.launch {
                val searchQuery = search_text_input.text.toString()
                when (searchType) {
                    Edition.Quran -> {
                            loading_search_result.visible()

                            val searchResult =
                                withContext(Dispatchers.IO) { repository.searchQuran(searchQuery,MushafConstants.SimpleQuran) }

                            dispatchSearchResult(searchResult, searchQuery)
                    }
                    else -> {
                        loading_search_result.visible()

                        val searchResult = withContext(Dispatchers.IO) {
                            repository.searchTranslation(
                                searchQuery,
                                searchType
                            )
                        }
                        dispatchSearchResult(searchResult, searchQuery)
                    }
                }

            }
        }

    @SuppressLint("SetTextI18n")
    private fun dispatchSearchResult(searchResult: List<Aya>, searchQuery: String) {
        loading_search_result.gone()
        number_of_result_search.visible()
        number_of_result_search.text =
            " ${searchResult.size} " + getString(R.string.result_for) + "  '$searchQuery'  "
        if (searchResult.isNotEmpty()) {
            empty_data_text.gone()

            recycler_search.adapter = SearchAdapter(searchResult, searchType)
        } else {
            empty_data_text.visible()
            recycler_search.adapter = null
        }
    }


    private fun initGroupChipListener() {
        search_quran_chip.setOnCheckedChangeListener(this)
        search_tafseer_chip.setOnCheckedChangeListener(this)
        search_translation_chip.setOnCheckedChangeListener(this)

        search_quran_chip.isChecked = true
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val elevation = resources!!.getDimension(R.dimen.item_elevation)
        val arrayOfChip = arrayOf(search_quran_chip, search_tafseer_chip, search_translation_chip)
        for (chip in arrayOfChip) {

            if (chip.id == buttonView.id) chip.checked(elevation)
            else chip.unChecked()

            searchType = when (buttonView.id) {
                R.id.search_quran_chip -> Edition.Quran
                R.id.search_tafseer_chip -> Edition.Tafsir
                else -> Edition.Translation
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancelChildren()
        unlockScreenOrientation()
    }

}
