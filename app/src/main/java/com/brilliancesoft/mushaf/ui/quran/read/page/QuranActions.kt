package com.brilliancesoft.mushaf.ui.quran.read.page

import androidx.appcompat.app.AppCompatActivity
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.framework.DownloadService
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.dialog.ConformDialog
import com.brilliancesoft.mushaf.ui.common.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.common.dialog.LoadingDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.quran.read.helpers.textToWords
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterViewModel
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationViewModel
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordLoader
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordViewModel
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuranActions(
    val activity: AppCompatActivity,
    val coroutineScope: CoroutineScope
) {


    private lateinit var wordByWordLoader: WordByWordLoader

    private val translationBottomSheet = TranslationBottomSheet()
    private val reciterBottomSheet = ReciterBottomSheet()
    private val wordByWordBottomSheet = WordByWordBottomSheet()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var repository: Repository

    private val reciterViewModel = activity.viewModelOf<ReciterViewModel>()
    private val wordByWordViewModel = activity.viewModelOf<WordByWordViewModel>()
    private val translationViewModel: TranslationViewModel by lazy {
        activity.viewModelOf(
            TranslationViewModel::class.java,
            viewModelFactory
        )
    }
    private val quranViewModel: QuranViewModel by lazy {
        activity.viewModelOf(
            QuranViewModel::class.java,
            viewModelFactory
        )
    }

    init {
        MushafApplication.appComponent.inject(this)
        wordByWordLoader = WordByWordLoader(repository)
    }

    fun showWordsTranslation(aya: Aya) {
        val words = textToWords(aya.text)
        if (words.isNotEmpty()) {

            val loadingDialog = LoadingDialog()
            loadingDialog.show(activity.supportFragmentManager, LoadingDialog.TAG)

            wordByWordLoader.getDataWordByWord(
                coroutineScope,
                words,
                aya,
                onSuccess = { wordsByWords ->
                    loadingDialog.dismiss()
                    showWordByWordDialog(wordsByWords)
                    //Log.d("Word by word", words.toString())
                },
                doOnEmptyData = {
                    loadingDialog.dismiss()
                    showDownloadConfirmationDialog(aya)
                })

        } else
            MushafToast.makeLong(activity, R.string.select_word_leaset)
    }

    fun showAyaTranslation(numberInMushaf: Int) {
        translationViewModel.setAyaNumber(numberInMushaf)
        translationBottomSheet.show(
            activity.supportFragmentManager,
            TranslationBottomSheet.TAG
        )
    }

    fun updateBookmarkState(aya: Aya) {
        val msg =
            if (aya.isBookmarked) R.string.bookmark_removed
            else R.string.bookmard_saved

        coroutineScope.launch(Dispatchers.IO) {
            repository.updateBookmarkStatus(aya.number, aya.edition!!.identifier, !aya.isBookmarked)
            withContext(Dispatchers.Main) {
                MushafToast.makeShort(activity, msg)
                quranViewModel.updateBookmarkStateInData(aya)
            }
        }
    }


    fun playReciter(aya: Aya) {
        reciterViewModel.setSurah(
            aya.numberInSurah,
            aya.number - aya.numberInSurah,
            aya.surah!!
        )
        reciterBottomSheet.show(
            activity.supportFragmentManager,
            ReciterBottomSheet.TAG
        )
    }

    private fun showWordByWordDialog(wordsByWords: List<Pair<String?, String>>) {
        wordByWordViewModel.setWordByWordData(wordsByWords)
        wordByWordBottomSheet.show(
            activity.supportFragmentManager,
            WordByWordBottomSheet.TAG
        )
    }

    private fun showDownloadConfirmationDialog(aya: Aya) {
        ConformDialog.show(
            R.string.word_by_word_confirmation,
            activity.supportFragmentManager
        ).onConfirm = { downloadWordByWord(aya) }
    }

    private fun downloadWordByWord(aya: Aya) {
        if (DownloadService.isDownloading)
            MushafToast.makeLong(activity, R.string.downloading_please_wait)
        else {
            coroutineScope.launch {

                val downloadingState = withContext(Dispatchers.IO) {
                    repository.getDownloadState(MushafConstants.WordByWord)
                }

                val edition = Edition(
                    identifier = MushafConstants.WordByWord,
                    name = "Word by word",
                    language = "En", format = "text", englishName = "Word by Word",
                    type = ""
                )


                DownloadService.create(activity, edition, downloadingState)
                val progressDialog = DownloadDialog()
                progressDialog.progressListener = object : DownloadDialog.ProgressListener {
                    override fun onSuccess(dialog: DownloadDialog) {
                        dialog.dismiss()
                        showWordsTranslation(aya)
                    }
                }
                progressDialog.show(
                    activity.supportFragmentManager,
                    DownloadDialog.TAG
                )
            }
        }
    }


    init {
        MushafApplication.appComponent.inject(this)
    }
}