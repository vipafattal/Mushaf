package co.jp.smagroup.musahaf.ui.library.manage

import android.os.Bundle
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.utils.extensions.observeOnMainThread
import co.jp.smagroup.musahaf.framework.CustomToast
import kotlinx.android.synthetic.main.activity_manage_library.*
import javax.inject.Inject

class ManageLibraryActivity : BaseActivity() {

    @Inject
    lateinit var repository: Repository

    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenOrientation()
        setContentView(R.layout.activity_manage_library)

        setSupportActionBar(toolbar_manage_library)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        repository.errorStream.filter { it.isNotEmpty() }.observeOnMainThread { CustomToast.makeLong(this, it) }

        val tabPagerAdapter = TabPagerAdapter(this, supportFragmentManager)
        viewpager_manage_library.adapter = tabPagerAdapter
        tabs_manage_library.setupWithViewPager(viewpager_manage_library)
        // Iterate over all tabs and set the custom view
        for (i in 0 until tabs_manage_library.tabCount) {
            val tab = tabs_manage_library.getTabAt(i)
            tab?.customView = tabPagerAdapter.bindView(i)
        }
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
}
