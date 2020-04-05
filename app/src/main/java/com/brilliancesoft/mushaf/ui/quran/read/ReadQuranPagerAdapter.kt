package com.brilliancesoft.mushaf.ui.quran.read

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.PagerAdapter
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.DownloadService
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.ReadData
import com.brilliancesoft.mushaf.ui.commen.ViewModelFactory
import com.brilliancesoft.mushaf.ui.commen.dialog.ConformDialog
import com.brilliancesoft.mushaf.ui.commen.dialog.DownloadDialog
import com.brilliancesoft.mushaf.ui.commen.dialog.LoadingDialog
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.helpers.*
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.reciter.ReciterViewModel
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.translation.TranslationViewModel
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordBottomSheet
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordLoader
import com.brilliancesoft.mushaf.ui.quran.read.wordByWord.WordByWordViewModel
import com.brilliancesoft.mushaf.utils.TextActionUtil
import com.brilliancesoft.mushaf.utils.TextSelectionCallback
import com.brilliancesoft.mushaf.utils.extensions.plusAssign
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.utils.gone
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import kotlinx.android.synthetic.main.layout_page_number.view.*
import kotlinx.android.synthetic.main.layout_surah_header.view.*
import kotlinx.android.synthetic.main.pager_quran.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReadQuranPagerAdapter(
    private val readQuranActivity: ReadQuranActivity,
    private val quranFormattedBySurah: Map<Int, List<Aya>>,
    private val coroutineScope: CoroutineScope
) : PagerAdapter(), TextSelectionCallback.OnActionItemClickListener {

    init {
        MushafApplication.appComponent.inject(this)
        if (quranFormattedBySurah.isEmpty()) readQuranActivity.finish()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val translationViewModel by lazy {
        readQuranActivity.viewModelOf(
            TranslationViewModel::class.java,
            viewModelFactory
        )
    }
    private val reciterViewModel = readQuranActivity.viewModelOf(ReciterViewModel::class.java)
    private val wordByWordViewModel = readQuranActivity.viewModelOf(WordByWordViewModel::class.java)

    private val translationBottomSheet = TranslationBottomSheet()
    private val reciterBottomSheet = ReciterBottomSheet()
    private val wordByWordBottomSheet = WordByWordBottomSheet()

    private val pageFormatter: QuranPageTextFormatter by lazy(LazyThreadSafetyMode.NONE) {
        val popupActions = PopupActions(readQuranActivity, popupActions)
        val textDecorator = FunctionalQuranText(readQuranActivity, popupActions)

        return@lazy QuranPageTextFormatter(textDecorator)
    }

    private val wordByWordLoader = WordByWordLoader(readQuranActivity.repository)

    override fun isViewFromObject(view: View, `object`: Any): Boolean =
        view === `object`

    override fun getCount(): Int = MushafConstants.SurahsNumber

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any): Unit =
        collection.removeView(view as View)

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val rawData = quranFormattedBySurah.getValue(position + 1)
        val readDataPage = mutableListOf<ReadData>()
        pageFormatter.format(rawData, outputTo = readDataPage)

        val pageView = collection.inflater(R.layout.pager_quran)
        val pageScroller = pageView.pageScroller

        pageScroller.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (oldScrollY < scrollY) readQuranActivity.updateSystemNavState(true)
            else readQuranActivity.updateSystemNavState(false)
        }

        for (readData in readDataPage) {
            val view = pageView.quranContainer.inflater(R.layout.item_quran_surah)
            pageView.quranContainer.addView(view)
            view.bindSurahText(readData)
        }
        pageView.quranContainer.tag = PAGE_CONTAINER_VIEW_TAG + position
        pageView.bindAyaInfo(readDataPage.first())
        collection += pageView
        return pageView
    }

    private fun View.bindSurahText(readData: ReadData) {
        val surah = readData.aya.surah!!
        if (readData.isNewSurah) {
            if (!surah.isFatihaOrTawba) {
                if (MushafApplication.isDarkThemeEnabled) bismillahSurah.setImageResource(R.drawable.ic_bismillah_light)
                else bismillahSurah.setImageResource(R.drawable.ic_bismillah_dark)
            } else
                bismillahSurah.gone()
        } else
            surah_name_view_root.gone()

        surahNameArabic.text = surah.name
        pageTextQuran.setText(readData.pagedText, TextView.BufferType.SPANNABLE)
        pageTextQuran.selectionTextCallBack(readData, this@ReadQuranPagerAdapter)
    }

    @SuppressLint("SetTextI18n")
    private fun View.bindAyaInfo(readData: ReadData) {
        //Page ayaNumber view
        val pageNumberView = quranContainer.inflater(R.layout.layout_page_number)
        val aya = readData.aya

        pageNumberView.pageNumber.text = aya.page.toString().toLocalizedNumber()
        quranContainer.addView(pageNumberView)
        //Juz ayaNumber & surah name in English or Arabic depending on local.
        juzNumberHeader.text =
            "${Stringify(R.string.juz, context)} ${aya.juz.toString().toLocalizedNumber()}"

        surahNameHeader.text = if (isRightToLeft == 1) aya.surah!!.englishName
        else aya.surah!!.name
    }

    private val popupActions = object : PopupActions.PopupOnAyaClickListener {
        override fun popupItemClicked(aya: Aya, view: View) {
            when (view.id) {
                R.id.bookmark_popup -> {
                    //Aya.isBookmarked is true then it's must be removed.
                    val msg =
                        if (aya.isBookmarked) R.string.bookmark_removed else R.string.bookmard_saved
                    CustomToast.makeShort(readQuranActivity, msg)
                    readQuranActivity.updateBookmarkState(aya)
                }
                R.id.share_popup -> TextActionUtil.shareAya(readQuranActivity, aya)
                R.id.play_popup -> playReciter(aya)
                R.id.translate_popup -> showAyaTranslation(aya.number)
                R.id.wordByWord_popup -> wordsToTranslate(textToWords(aya.text), aya)
            }
        }
    }


    //on text selection this will respond to user click on menu created.
    @SuppressLint("NewApi")
    override fun onActionItemClick(
        data: Any,
        item: MenuItem,
        selectedRange: IntRange,
        clipboard: ClipboardManager
    ): Boolean {
        val readData = data as ReadData
        when (item.itemId) {
            R.id.option_menu_translate -> {
                val words = textToWords(readData.pagedText.subSequence(selectedRange))
                wordsToTranslate(words, readData.aya)
                return true
            }
            TextSelectionCallback.Copy, TextSelectionCallback.Share -> {
                val selectedText = data.pagedText.substring(selectedRange)

                if (selectedText.isNotEmpty() && selectedText.isNotBlank()) {

                    val outputFriendlyText =
                        selectedTextToOutput(readQuranActivity, selectedText, readData.aya)

                    if (item.itemId == TextSelectionCallback.Share)
                        TextActionUtil.shareText(
                            readQuranActivity,
                            outputFriendlyText,
                            R.string.share_ayah_text
                        )
                    else
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Quran text", outputFriendlyText)
                        )

                }
                return true
            }
        }
        return false
    }

    private fun wordsToTranslate(words: ArrayList<String>, aya: Aya) {
        if (words.isNotEmpty()) {

            val loadingDialog = LoadingDialog()
            loadingDialog.show(readQuranActivity.supportFragmentManager, LoadingDialog.TAG)

            wordByWordLoader.getDataWordByWord(
                coroutineScope,
                words,
                aya,
                onSuccess = { wordsByWords ->
                    loadingDialog.dismiss()
                    showWordByWordDialog(wordsByWords)
                    Log.d("Word by word", words.toString())

                },
                doOnEmptyData = {
                    loadingDialog.dismiss()
                    showDownloadConfirmationDialog(words, aya)
                })

        } else
            CustomToast.makeLong(readQuranActivity, R.string.select_word_leaset)
    }

    private fun showWordByWordDialog(wordsByWords: List<Pair<String?, String>>) {
        wordByWordViewModel.setWordByWordData(wordsByWords)
        wordByWordBottomSheet.show(
            readQuranActivity.supportFragmentManager,
            WordByWordBottomSheet.TAG
        )
    }

    private fun showDownloadConfirmationDialog(words: ArrayList<String>, aya: Aya) {
        val confirmationDialog =
            ConformDialog.getInstance(
                Stringify(
                    R.string.word_by_word_confirmation,
                    readQuranActivity
                )
            )
        confirmationDialog.show(readQuranActivity.supportFragmentManager, ConformDialog.TAG)
        confirmationDialog.onConfirm = { downloadWordByWord(words, aya) }
    }

    private fun downloadWordByWord(words: ArrayList<String>, aya: Aya) {
        if (DownloadService.isDownloading)
            CustomToast.makeLong(readQuranActivity, R.string.downloading_please_wait)
        else {
            coroutineScope.launch {

                val downloadingState = withContext(Dispatchers.IO) {
                    readQuranActivity.repository.getDownloadState(MushafConstants.WordByWord)
                }

                val edition = Edition(
                    identifier = MushafConstants.WordByWord,
                    name = "Word by word",
                    language = "En", format = "text", englishName = "Word by Word",
                    type = ""
                )


                DownloadService.create(readQuranActivity, edition, downloadingState)
                val progressDialog = DownloadDialog()
                progressDialog.progressListener = object : DownloadDialog.ProgressListener {
                    override fun onSuccess(dialog: DownloadDialog) {
                        dialog.dismiss()
                        wordsToTranslate(words, aya)
                    }
                }
                progressDialog.show(readQuranActivity.supportFragmentManager, DownloadDialog.TAG)
            }
        }
    }

    private fun showAyaTranslation(numberInMusahaf: Int) {
        translationViewModel.setAyaNumber(numberInMusahaf)
        translationBottomSheet.show(
            readQuranActivity.supportFragmentManager,
            TranslationBottomSheet.TAG
        )
    }

    private fun playReciter(aya: Aya) {
        reciterViewModel.setSurah(
            aya.numberInSurah,
            aya.number - aya.numberInSurah,
            aya.surah!!
        )
        reciterBottomSheet.show(readQuranActivity.supportFragmentManager, ReciterBottomSheet.TAG)
    }

    companion object {
        const val PAGE_CONTAINER_VIEW_TAG = "pageScroller:"
    }
}


