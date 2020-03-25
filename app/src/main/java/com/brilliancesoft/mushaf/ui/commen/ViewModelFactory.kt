package com.brilliancesoft.mushaf.ui.commen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.library.manage.LibraryViewModel
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.search.SearchableQuranViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by ${User} on ${Date}
 */

@Singleton
class ViewModelFactory @Inject constructor(var repository: Repository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(QuranViewModel::class.java) -> QuranViewModel(
                repository
            ) as T
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> LibraryViewModel(
                repository
            ) as T

            modelClass.isAssignableFrom(SearchableQuranViewModel::class.java) -> SearchableQuranViewModel(
                repository
            ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }

}