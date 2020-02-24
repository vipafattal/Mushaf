package co.jp.smagroup.musahaf.ui.library


import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.ui.MainActivity
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.library.manage.ManageLibraryActivity
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseFragment
import co.jp.smagroup.musahaf.ui.search.SearchActivity
import co.jp.smagroup.musahaf.utils.extensions.onScroll
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.coroutines.*
import javax.inject.Inject


class LibraryFragment : BaseFragment() {
    @Inject
    lateinit var repository: Repository

    init {
        MushafApplication.appComponent.inject(this)
    }
    private val job = SupervisorJob()
    protected val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override val layoutId: Int = R.layout.fragment_library
    private lateinit var parentActivity: MainActivity
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parentActivity = activity as MainActivity

        parentActivity.updateToolbar(R.string.library, R.drawable.ic_search)


        add_item_fab.onClick {
            context?.launchActivity<ManageLibraryActivity>()
        }
        val layoutManager = recycler_items_library.layoutManager as LinearLayoutManager
        recycler_items_library.onScroll { _, dy ->
            if (dy > 0) add_item_fab.shrink()
            else if (layoutManager.findFirstVisibleItemPosition() < 1) add_item_fab.extend()
        }

        parentActivity.beginToolbar_icon.onClick {
            parentActivity.launchActivity<SearchActivity>()
        }


    }

    override fun onResume() {
        super.onResume()
        initRecyclerView()
    }

    override fun onPause() {
        super.onPause()
        job.cancelChildren()
    }

    private fun initRecyclerView() {
        coroutineScope.launch {
            empty_data_text.gone()
            val data = withContext(Dispatchers.IO) { repository.getDownloadedEditions() }
            loadingCompleted(false)
            if (data.isNotEmpty()) {
                recycler_items_library?.adapter = LibraryAdapter(data)
            } else
                empty_data_text?.visible()
        }
    }
}
