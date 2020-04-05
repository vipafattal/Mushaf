package com.brilliancesoft.mushaf.ui.commen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.library.manage.LibraryViewModel
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationViewModel
import com.brilliancesoft.mushaf.ui.search.SearchableQuranViewModel
import com.codebox.lib.extrenalLib.TinyDB
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by ${User} on ${Date}
 */

@Singleton
class ViewModelFactory @Inject constructor(var repository: Repository, var tinyDB: TinyDB) :
    ViewModelProvider.Factory {

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

            modelClass.isAssignableFrom(TranslationViewModel::class.java) -> TranslationViewModel(
                repository, tinyDB
            ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }

}