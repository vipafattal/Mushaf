package com.brilliancesoft.mushaf.framework.di

import com.brilliancesoft.mushaf.framework.DownloadService
import com.brilliancesoft.mushaf.framework.data.local.LocalRepository
import com.brilliancesoft.mushaf.framework.data.local.MetadataRepository
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.SplashActivity
import com.brilliancesoft.mushaf.ui.audioPlayer.MediaDownloadService
import com.brilliancesoft.mushaf.ui.bookmarks.BookmarksFragment
import com.brilliancesoft.mushaf.ui.common.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.library.LibraryFragment
import com.brilliancesoft.mushaf.ui.library.manage.ManageLibraryActivity
import com.brilliancesoft.mushaf.ui.library.manage.TabFragment
import com.brilliancesoft.mushaf.ui.library.read.ReadLibraryActivity
import com.brilliancesoft.mushaf.ui.quran.QuranIndexFragment
import com.brilliancesoft.mushaf.ui.quran.read.page.QuranActions
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationBottomSheet
import com.brilliancesoft.mushaf.ui.search.SearchActivity
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

    fun inject(localRepository: LocalRepository)

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
    fun inject(quranActions: QuranActions)
    fun inject(splashActivity: SplashActivity)
    fun inject(metadataRepository: MetadataRepository)
    fun inject(mediaDownloadService: MediaDownloadService)

}