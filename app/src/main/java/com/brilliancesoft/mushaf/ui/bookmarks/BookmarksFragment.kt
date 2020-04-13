package com.brilliancesoft.mushaf.ui.bookmarks

import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseFragment
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import com.codebox.lib.android.widgets.snackbar.material
import com.codebox.lib.android.widgets.snackbar.showAction
import com.codebox.lib.android.widgets.snackbar.snackbar
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import kotlinx.coroutines.*
import javax.inject.Inject

class BookmarksFragment : BaseFragment() {
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var quranViewModel: QuranViewModel
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    override val layoutId: Int = R.layout.fragment_bookmarks
    private var deletedAyaIndex = -1
    private var deletedAya: Aya? = null
    private var dataList = mutableListOf<Aya>()
    private lateinit var bookmarksAdapter: BookmarksAdapter
    private val toDelete = mutableListOf<Aya>()

    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).updateToolbar(R.string.bookmarks)
        quranViewModel = viewModelOf(QuranViewModel::class.java, viewModelFactory)
    }

    private fun dispatchBookmarkData() {
        if (dataList.isNotEmpty()) {
            empty_data_text.gone()
            bookmarksAdapter = BookmarksAdapter(dataList, this@BookmarksFragment)
            recycler_bookmarks.adapter = bookmarksAdapter
        } else empty_data_text.visible()
    }


    private fun refreshBookmarksData() = runBlocking {
        coroutineScope.launch {
            val newData = withContext(Dispatchers.IO) { repository.getAllByBookmarkStatus(true) }
            dataList = newData
            dataList.sortBy { it.edition!!.type }
            dispatchBookmarkData()
        }
    }

    fun removeBookmarked(aya: Aya) {
        deletedAya = aya
        addToDelete(aya)
        deletedAyaIndex = dataList.indexOf(aya)
        dataList.remove(aya)
        bookmarksAdapter.notifyItemRemoved(deletedAyaIndex)
        bookmarksAdapter.notifyItemRangeChanged(deletedAyaIndex, dataList.size)
        activeDeleteAyaAction()
    }

    private fun activeDeleteAyaAction() {
        val snackbar =
            (activity as BaseActivity).snackbar(getString(R.string.remove_bookmark)).material(true)
        snackbar.setAnchorView(R.id.bottom_app_nav)

        snackbar.showAction(
            getString(R.string.undo),
            actionTextColor = Colour(R.color.colorSecondary)
        ) { restoreDeletedAya() }
    }

    private fun addToDelete(aya: Aya) {
        toDelete.add(aya)
        if (dataList.isEmpty()) empty_data_text.visible()
    }

    private fun restoreDeletedAya() {
        toDelete.remove(deletedAya)
        dataList.add(deletedAyaIndex, deletedAya!!)
        bookmarksAdapter.notifyItemInserted(deletedAyaIndex)
        bookmarksAdapter.notifyItemRangeChanged(deletedAyaIndex, dataList.size)
    }

    private fun deletePermanently() {
        if (toDelete.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                for (aya in toDelete) {
                    quranViewModel.updateBookmarkStateInData(aya)


                    repository.updateBookmarkStatus(
                        aya.number,
                        aya.edition!!.identifier,
                        false
                    )
                }
                toDelete.clear()

                job.cancelChildren()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        refreshBookmarksData()
    }

    override fun onPause() {
        super.onPause()
        deletePermanently()
    }


}
