package co.jp.smagroup.musahaf.ui.commen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.ui.library.manage.LibraryViewModel
import co.jp.smagroup.musahaf.ui.quran.QuranViewModel
import co.jp.smagroup.musahaf.ui.search.SearchableQuranViewModel
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