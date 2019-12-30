package co.jp.smagroup.musahaf.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.ui.bookmarks.BookmarksFragment
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.library.LibraryFragment
import co.jp.smagroup.musahaf.ui.more.SettingsFragment
import co.jp.smagroup.musahaf.ui.quran.QuranIndexFragment
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import co.jp.smagroup.musahaf.utils.notNull
import com.codebox.lib.android.fragments.replaceFragment
import com.codebox.lib.android.views.utils.invisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    @Inject
    lateinit var repository: Repository
    private var disposable: Disposable? = null
    private lateinit var navigationViewModel: NavigationViewModel


    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationViewModel = viewModelOf(NavigationViewModel::class.java)
        GlobalScope.launch(Dispatchers.IO) { repository.getAvailableReciters(true) }

        if (savedInstanceState != null) {
            val (navigationId, currentFragment) = navigationViewModel.getCurrentNavigation()

            if (navigationId == R.id.nav_home && !QuranViewModel.isQuranDataLoaded()) bottom_app_nav.invisible()
            bottom_app_nav.selectedItemId = navigationId
            currentNavId = navigationId
            updateCurrentFragment(currentFragment)
        }

        bottom_app_nav.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) bottom_app_nav.selectedItemId = R.id.nav_home
    }


    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

    private var currentNavId: Int = 0
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        if (currentNavId != item.itemId) {
            currentNavId = item.itemId
            when (item.itemId) {
                R.id.nav_home -> {
                    if (!QuranViewModel.isQuranDataLoaded()) bottom_app_nav.invisible()
                    fragment = QuranIndexFragment()
                }
                R.id.nav_library -> fragment = LibraryFragment()
                R.id.nav_bookmarks -> fragment = BookmarksFragment()
                R.id.nav_more -> fragment = SettingsFragment()
            }



            if (fragment != null) {
                updateCurrentFragment(fragment)
            }
        }
        return true
    }


    private fun updateCurrentFragment(fragment: Fragment) {
        navigationViewModel.updateNavigation(currentNavId, fragment)
        replaceFragment(fragment, R.id.main_nav_host)
    }

    fun updateToolbar(@StringRes title: Int, @DrawableRes startIcon: Int = 0, @DrawableRes endIcon: Int = 0) {
        if (toolbarTitle.notNull) {
            toolbarTitle.alpha = 0.5f
            toolbarTitle.animate().alpha(1f).withEndAction { toolbarTitle.setText(title) }.start()

            if (startIcon == 0) {
                beginToolbar_icon.setImageDrawable(null)
                beginToolbar_icon.setOnClickListener(null)
            } else
                beginToolbar_icon.setImageResource(startIcon)

            if (endIcon == 0) {
                endToolbar_icon.setImageDrawable(null)
                endToolbar_icon.setOnClickListener(null)
            } else
                endToolbar_icon.setImageResource(endIcon)
        }
    }
}

