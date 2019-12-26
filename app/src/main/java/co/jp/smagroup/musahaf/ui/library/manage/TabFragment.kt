package co.jp.smagroup.musahaf.ui.library.manage


import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.CustomToast
import co.jp.smagroup.musahaf.model.DownloadingState
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.ui.DownloadService
import co.jp.smagroup.musahaf.ui.commen.ViewModelFactory
import co.jp.smagroup.musahaf.ui.commen.dialog.ProgressDialog
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseFragment
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import com.codebox.lib.android.widgets.longToast
import com.codebox.lib.android.widgets.recyclerView.onScroll
import kotlinx.android.synthetic.main.fragment_tab.*
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
class TabFragment : BaseFragment(),ProgressDialog.ProgressListener {
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

        viewModel.allAvailableEditions.observer(viewLifecycleOwner) {
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
                    downloadMusahaf(edition, downloadingState)
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
            0 -> data.filter { it.first.type == Edition.Tafsir }
            1 -> data.filter { it.first.type == Edition.Translation && it.first.language != "ar" }
            // 2 -> data.filter { it.first.format == "audio" }
            else -> throw IllegalArgumentException("Position $tabPosition not found")
        }.onEach { it.first.language.capitalize() }.sortedBy { it.first.language }
    }

    private var recyclerViewPosition = 0
    private fun downloadMusahaf(edition: Edition, downloadingState: DownloadingState) {

        if (parentActivity.supportFragmentManager.findFragmentByTag(ProgressDialog.TAG) == null) {

            if (DownloadService.isDownloading)
                CustomToast.makeLong(parentActivity, R.string.downloading_please_wait)
            else {
                DownloadService.create(parentActivity, edition, downloadingState)

                val progressDialog = ProgressDialog()

                progressDialog.progressListener = object : ProgressDialog.ProgressListener {

                    override fun onSuccess(dialog: ProgressDialog) {
                        super.onSuccess(dialog)
                        viewModel.updateDataDownloadState(edition.identifier)
                    }

                    override fun onError() = viewModel.updateDataDownloadState(edition.identifier)

                    override fun onBackground() {
                        parentActivity.finish()
                    }
                }
                progressDialog.show(parentActivity.supportFragmentManager, ProgressDialog.TAG)
            }
        }
    }

    override fun loadData() {
        super.loadData()
        viewModel.getEditions()
    }

    companion object {
        fun newInstance(tabPosition: Int): TabFragment {
            val tabFragment = TabFragment()
            tabFragment.tabPosition = tabPosition
            return tabFragment
        }
    }
}

