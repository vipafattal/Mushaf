package co.jp.smagroup.musahaf.framework.di

import co.jp.smagroup.musahaf.framework.data.local.LocalDataSource
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.framework.DownloadService
import co.jp.smagroup.musahaf.ui.MainActivity
import co.jp.smagroup.musahaf.ui.bookmarks.BookmarksFragment
import co.jp.smagroup.musahaf.ui.commen.dialog.DownloadDialog
import co.jp.smagroup.musahaf.ui.library.LibraryFragment
import co.jp.smagroup.musahaf.ui.library.manage.ManageLibraryActivity
import co.jp.smagroup.musahaf.ui.library.manage.TabFragment
import co.jp.smagroup.musahaf.ui.library.read.ReadLibraryActivity
import co.jp.smagroup.musahaf.ui.quran.read.ReadQuranActivity
import co.jp.smagroup.musahaf.ui.quran.QuranIndexFragment
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ReciterBottomSheet
import co.jp.smagroup.musahaf.ui.quran.read.translation.TranslationBottomSheet
import co.jp.smagroup.musahaf.ui.search.SearchActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Created by ${User} on ${Date}
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    //Framework
    fun inject(repository: Repository)

    fun inject(localDataSource: LocalDataSource)

    //Activities
    fun inject(mainActivity: MainActivity)
    fun inject(searchActivity: SearchActivity)
    fun inject(mainActivity: ReadQuranActivity)
    fun inject(manageLibraryActivity: ManageLibraryActivity)
    fun inject(readLibraryActivity: ReadLibraryActivity)

    //Fragments and dialogs
    fun inject(downloadDialog: DownloadDialog)

    fun inject(libraryFragment: LibraryFragment)
    fun inject(quranListFragment: QuranIndexFragment)
    fun inject(tabFragment: TabFragment)
    fun inject(translationBottomSheet: TranslationBottomSheet)
    fun inject(reciterBottomSheet: ReciterBottomSheet)
    fun inject(bookmarksFragment: BookmarksFragment)
    fun inject(downloadService: DownloadService)

}