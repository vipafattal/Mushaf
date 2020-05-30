package com.brilliancesoft.mushaf.ui.library.manage

import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.utils.TabStyle
import com.brilliancesoft.mushaf.utils.extensions.observeOnMainThread
import com.brilliancesoft.mushaf.utils.extensions.setTabStyle
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import kotlinx.android.synthetic.main.activity_manage_library.*
import javax.inject.Inject

class ManageLibraryActivity : BaseActivity() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    init {
        MushafApplication.appComponent.inject(this)
    }

    private val libraryViewModel: LibraryViewModel by lazy {
        viewModelOf(LibraryViewModel::class.java, viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenOrientation()
        setContentView(R.layout.activity_manage_library)

        setSupportActionBar(toolbar_manage_library)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        intent.extras?.apply {
            val editionId = getString(DOWNLOAD_EDITION_KEY, "")

            if (editionId.isNotEmpty() && supportFragmentManager.findFragmentByTag(DownloadDialog.TAG) == null)
                showDownloadDialog(editionId)
        }


        repository.errorStream.filter { it.isNotEmpty() }
            .observeOnMainThread { MushafToast.makeLong(this, it) }

        val tabPagerAdapter = TabPagerAdapter(this)
        libraryManagerPager.adapter = tabPagerAdapter
        libraryManagerPager.setTabStyle(tabs_manage_library) { position ->
            val tabTitle: String
            val tabIcon: Int
            when (position) {
                0 -> {
                    tabTitle = getString(R.string.tafseer)
                    tabIcon = R.drawable.ic_defining
                }
                else -> {
                    tabTitle = getString(R.string.translate)
                    tabIcon = R.drawable.ic_translate
                }
            }
            TabStyle(tabTitle, tabIcon)
        }
    }

    fun showDownloadDialog(editionId: String) {

        val downloadDialog = DownloadDialog()

        downloadDialog.progressListener = object : DownloadDialog.ProgressListener {

            override fun onSuccess(dialog: DownloadDialog) {
                dialog.dismiss()
                libraryViewModel.updateDataDownloadState(editionId)
            }

            override fun onError() = libraryViewModel.updateDataDownloadState(editionId)

            override fun onBackground() = finish()
        }

        downloadDialog.show(supportFragmentManager, DownloadDialog.TAG)
    }

    override fun onResume() {
        super.onResume()
        currentSystemVisibility = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unlockScreenOrientation()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val DOWNLOAD_EDITION_KEY = "current download edition"
    }

}
