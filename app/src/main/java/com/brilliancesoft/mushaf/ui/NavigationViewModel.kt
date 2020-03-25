package com.brilliancesoft.mushaf.ui

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.quran.QuranIndexFragment

/**
 * Created by ${User} on ${Date}
 */
class NavigationViewModel : ViewModel() {
    private lateinit var currentNavigation: MutableLiveData<Pair<Int, Fragment>>

    fun updateNavigation(@IdRes navigationId: Int, fragment: Fragment) {
        if (!::currentNavigation.isInitialized)
            currentNavigation = MutableLiveData()
        currentNavigation.value = navigationId to fragment
    }

    fun getCurrentNavigation(): Pair<Int, Fragment> {
        if (!::currentNavigation.isInitialized)
            currentNavigation = MutableLiveData()
        return currentNavigation.value ?: R.id.nav_home to QuranIndexFragment()
    }

}