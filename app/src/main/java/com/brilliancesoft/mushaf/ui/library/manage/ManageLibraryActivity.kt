package com.brilliancesoft.mushaf.ui.library.manage

import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.commen.ViewModelFactory
import com.brilliancesoft.mushaf.ui.commen.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.utils.extensions.observeOnMainThread
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
            val editionId = getString(DOWNLOAD_EDITION_KEY,"")

            if (editionId.isNotEmpty() && supportFragmentManager.findFragmentByTag(DownloadDialog.TAG) == null)
                showDownloadDialog(editionId)
        }


        repository.errorStream.filter { it.isNotEmpty() }
            .observeOnMainThread { CustomToast.makeLong(this, it) }

        val tabPagerAdapter = TabPagerAdapter(this, supportFragmentManager)
        viewpager_manage_library.adapter = tabPagerAdapter
        tabs_manage_library.setupWithViewPager(viewpager_manage_library)
        // Iterate over all tabs and set the custom view
        for (i in 0 until tabs_manage_library.tabCount) {
            val tab = tabs_manage_library.getTabAt(i)
            tab?.customView = tabPagerAdapter.bindView(i)
        }
    }

    fun showDownloadDialog(editionId:String){

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
