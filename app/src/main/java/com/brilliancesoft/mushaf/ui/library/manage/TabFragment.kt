package com.brilliancesoft.mushaf.ui.library.manage


import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.framework.DownloadService
import com.brilliancesoft.mushaf.model.DownloadingState
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseFragment
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.widgets.recyclerView.onScroll
import kotlinx.android.synthetic.main.fragment_tab.*
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
class TabFragment : BaseFragment(), DownloadDialog.ProgressListener {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    init {
        MushafApplication.appComponent.inject(this)
    }

    override val layoutId: Int = R.layout.fragment_tab
    private lateinit var viewModel: LibraryViewModel

    private lateinit var parentActivity: ManageLibraryActivity
    private lateinit var adapter: ManageLibraryAdapter
    private var tabPosition = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        parentActivity = activity as ManageLibraryActivity
        viewModel = viewModelOf(LibraryViewModel::class.java, viewModelFactory)

        viewModel.getEditions().observer(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val data = filterDataByTabPosition(it)
                recycler_library_manage.adapter = getRecyclerAdapter(data)
                recycler_library_manage.scrollToPosition(recyclerViewPosition)
                loadingCompleted(false)
            } else loadingCompleted(true)
        }

        loadData()
    }

    private fun getRecyclerAdapter(data: List<Pair<Edition, DownloadingState>>): ManageLibraryAdapter {
        adapter =
            ManageLibraryAdapter(data, doOnItemClicked = { edition, downloadingState ->
                if (!downloadingState.isDownloadCompleted)
                    downloadMushaf(edition, downloadingState)
                //else Do nothing
            })

        recycler_library_manage.onScroll { _, _ ->
            recyclerViewPosition =
                (recycler_library_manage.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }

        return adapter
    }

    private fun filterDataByTabPosition(data: List<Pair<Edition, DownloadingState>>): List<Pair<Edition, DownloadingState>> {
        return when (tabPosition) {
            0 -> data.filter { it.first.type == Edition.TYPE_TAFSEER }
            1 -> data.filter { it.first.type == Edition.TYPE_TRANSLATION && it.first.language != "ar" }
            // 2 -> data.filter { it.first.format == "audio" }
            else -> throw IllegalArgumentException("Position $tabPosition not found")
        }.onEach { it.first.language.capitalize() }.sortedBy { it.first.language }
    }

    private var recyclerViewPosition = 0
    private fun downloadMushaf(edition: Edition, downloadingState: DownloadingState) {

        if (parentActivity.supportFragmentManager.findFragmentByTag(DownloadDialog.TAG) == null) {
            if (DownloadService.isDownloading) MushafToast.makeLong(parentActivity, R.string.downloading_please_wait)
            else DownloadService.create(parentActivity, edition, downloadingState)
        }

        parentActivity.showDownloadDialog(edition.identifier)

    }

    companion object {
        fun newInstance(tabPosition: Int): TabFragment {
            val tabFragment = TabFragment()
            tabFragment.tabPosition = tabPosition
            return tabFragment
        }
    }
}

