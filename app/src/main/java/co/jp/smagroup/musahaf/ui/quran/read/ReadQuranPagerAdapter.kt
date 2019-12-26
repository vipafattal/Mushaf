package co.jp.smagroup.musahaf.ui.quran.read

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.plusAssign
import androidx.viewpager.widget.PagerAdapter
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.CustomToast
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.model.ReadData
import co.jp.smagroup.musahaf.ui.DownloadService
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.commen.dialog.ConformDialog
import co.jp.smagroup.musahaf.ui.commen.dialog.LoadingDialog
import co.jp.smagroup.musahaf.ui.commen.dialog.ProgressDialog
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.quran.read.helpers.*
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ReciterBottomSheet
import co.jp.smagroup.musahaf.ui.quran.read.reciter.ReciterViewModel
import co.jp.smagroup.musahaf.ui.quran.read.translation.TranslationBottomSheet
import co.jp.smagroup.musahaf.ui.quran.read.translation.TranslationViewModel
import co.jp.smagroup.musahaf.ui.quran.read.wordByWord.WordByWordBottomSheet
import co.jp.smagroup.musahaf.ui.quran.read.wordByWord.WordByWordLoader
import co.jp.smagroup.musahaf.ui.quran.read.wordByWord.WordByWordViewModel
import co.jp.smagroup.musahaf.utils.TextActionUtil
import co.jp.smagroup.musahaf.utils.TextSelectionCallback
import co.jp.smagroup.musahaf.utils.extensions.addIfNotNull
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.extrenalLib.TinyDB
import kotlinx.android.synthetic.main.item_read_quran.view.*
import kotlinx.android.synthetic.main.item_read_quran_full_page.view.*
import kotlinx.android.synthetic.main.layout_surah_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ReadQuranPagerAdapter(
    private val readQuranActivity: ReadQuranActivity,
    private val dataMap: Map<Int, List<Aya>>,
    private val coroutineScope: CoroutineScope
) : PagerAdapter(), TextSelectionCallback.OnActionItemClickListener,
    PopupActions.OnQuranPopupItemClickListener {

    init {
        if (dataMap.isEmpty()) readQuranActivity.finish()
    }

    private val pageFormatter = getPagerFormatter()
    private val tinyDb = TinyDB(readQuranActivity)
    private val wordByWordLoader =
        WordByWordLoader(readQuranActivity.repository)


    override fun isViewFromObject(view: View, `object`: Any): Boolean =
         view === `object`

    override fun getCount(): Int = MusahafConstants.SurahsNumber

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any): Unit =
        collection.removeView(view as View)

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val rawData = dataMap.getValue(position + 1)
        val readDataPage = mutableListOf<ReadData>()
        pageFormatter.format(rawData, outputTo = readDataPage)

        val mainView = collection.inflater(R.layout.item_read_quran_full_page)
        mainView.pageScroller.tag = "pageScroller$position"

        if (readDataPage.size > 1) {
            mainView.surah_name_view_root.gone()
            mainView.bismillah_page.gone()
            mainView.surahText_page.gone()

            for ((index, readData) in readDataPage.withIndex()) {
                val view =
                    LayoutInflater.from(readQuranActivity)
                        .inflate(R.layout.item_read_quran, mainView.surahText_rootView, false)
                mainView.surahText_rootView.addView(view)
                view.bindMultiSurah(readData)
                view.surahText.tag = "surahText$index$position"
            }
        } else {
            mainView.bindFullPage(readDataPage[0])
            mainView.surahText_page.tag = "surahPageText$position"
        }

        mainView.bindAyaInfo(readDataPage[0])

        collection += mainView
        return mainView
    }

    private fun View.bindFullPage(readData: ReadData) {
        if (MushafApplication.isDarkThemeEnabled) bismillah_page.setImageResource(R.drawable.ic_bismillah_light)
        else bismillah_page.setImageResource(R.drawable.ic_bismillah_dark)

        if (readData.aya.surah!!.englishName == MusahafConstants.Fatiha || readData.aya.surah!!.englishName == MusahafConstants.Tawba)
            bismillah_page.gone()
        if (!readData.isNewSurah) surah_name_view_root.gone()



        surahNameArabic_read_page.text = readData.aya.surah!!.name
        surahText_page.setText(readData.pagedText, TextView.BufferType.SPANNABLE)
        surahText_page.selectionTextCallBack(readData, this@ReadQuranPagerAdapter)
    }

    private fun View.bindMultiSurah(readData: ReadData) {
        if (!readData.isNewSurah)
            surah_name_view_root.gone()
        else {
            if (MushafApplication.isDarkThemeEnabled) bismillah_page.setImageResource(R.drawable.ic_bismillah_light)
            else bismillah_page.setImageResource(R.drawable.ic_bismillah_dark)
        }

        surahNameArabic_read_page.text = readData.aya.surah!!.name
        surahText.setText(readData.pagedText, TextView.BufferType.SPANNABLE)
        surahText.selectionTextCallBack(readData, this@ReadQuranPagerAdapter)
    }

    private fun View.bindAyaInfo(readData: ReadData) {
        //Page ayaNumber view
        val pageNumberView = surahText_rootView.inflater(R.layout.layout_page_number) as TextView
        pageNumberView.text = readData.aya.page.toString().toLocalizedNumber()
        surahText_rootView.addView(pageNumberView)

        //Juz ayaNumber & surah name in English or Arabic depending on local.
        juzNumber_read_quran_page.text =
            "${Stringify(R.string.juz, context)} ${readData.aya.juz.toString().toLocalizedNumber()}"

        surahName_read_quran_page.text = if (isRightToLeft == 1)
            readData.aya.surah!!.englishName
        else
            readData.aya.surah!!.name
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
                        TextActionUtil.shareText(readQuranActivity, outputFriendlyText)
                    else
                        clipboard.primaryClip =
                            ClipData.newPlainText("Quran text", outputFriendlyText)
                }
                return true
            }
        }
        return false
    }


    //Word By word translation.
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

        readQuranActivity.viewModelOf(WordByWordViewModel::class.java)
            .setWordByWordData(wordsByWords)

        WordByWordBottomSheet().show(
            readQuranActivity.supportFragmentManager,
            WordByWordBottomSheet.TAG
        )

    }

    private fun showDownloadConfirmationDialog(words: ArrayList<String>, aya: Aya) {
        val confirmationDialog =
            ConformDialog.getInstance(
                Stringify(
                    R.string.word_by_wrod_confirmation,
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
                    readQuranActivity.repository.getDownloadState(MusahafConstants.WordByWord)
                }

                val edition = Edition(
                    identifier = MusahafConstants.WordByWord,
                    name = "Word by word",
                    language = "En"
                )


                DownloadService.create(readQuranActivity, edition, downloadingState)


                val progressDialog = ProgressDialog()

                progressDialog.progressListener = object : ProgressDialog.ProgressListener {
                    override fun onSuccess(dialog: ProgressDialog) {
                        super.onSuccess(dialog)
                        wordsToTranslate(words, aya)
                    }
                }
                progressDialog.show(readQuranActivity.supportFragmentManager, ProgressDialog.TAG)

            }
        }
    }

    override fun popupItemClicked(aya: Aya, view: ImageView) {
        val numberInMusahaf = aya.number
        when (view.id) {
            R.id.bookmark_popup -> {
                //Aya.isBookmarked is true then it's must be removed.
                if (aya.isBookmarked) CustomToast.makeShort(
                    readQuranActivity,
                    R.string.bookmark_removed
                )
                else CustomToast.makeShort(readQuranActivity, R.string.bookmard_saved)

                readQuranActivity.updateBookmarkState(aya)
            }

            R.id.share_popup -> {
                val shareTextFormatted =
                    "{${aya.text}} \npage:${aya.page} \nsurah:${aya.surah!!.name}  \nvia @Musahaf for android"

                TextActionUtil.shareText(readQuranActivity, shareTextFormatted)
            }

            R.id.play_popup -> playReciter(aya)

            R.id.translate_popup -> getTranslation(numberInMusahaf)

            // R.id.wordByWord_popup -> prepareDataToWordByWord()
        }
    }


    private fun getTranslation(numberInMusahaf: Int) {
        coroutineScope.launch {
            val downloadedEditions =
                withContext(Dispatchers.IO) { readQuranActivity.repository.getDownloadedEditions() }.filter { it.format == MusahafConstants.Text }
                    .toMutableList()
            val selectedTranslation =
                tinyDb.getListString(PreferencesConstants.LastUsedTranslation)
            val selectedEditions: MutableList<Edition> = mutableListOf()
            val unSelectedEditions: MutableList<Edition>
            if (selectedTranslation.isNotEmpty()) {
                selectedTranslation.forEach { identifier ->
                    val selected =
                        downloadedEditions.firstOrNull { it.identifier == identifier }
                    selectedEditions.addIfNotNull(selected)
                }
                downloadedEditions.removeAll(selectedEditions)
                unSelectedEditions = downloadedEditions
            } else {
                unSelectedEditions = downloadedEditions.toMutableList()
            }
            readQuranActivity.viewModelOf(TranslationViewModel::class.java)
                .setTranslationData(selectedEditions, unSelectedEditions, numberInMusahaf)
            TranslationBottomSheet().show(
                readQuranActivity.supportFragmentManager,
                TranslationBottomSheet.TAG
            )
        }
    }

    private fun playReciter(aya: Aya) {
        val reciterViewModel = readQuranActivity.viewModelOf(ReciterViewModel::class.java)

        reciterViewModel.setSurah(
            aya.numberInSurah,
            aya.number - aya.numberInSurah,
            aya.surah!!
        )

        ReciterBottomSheet()
            .show(readQuranActivity.supportFragmentManager, ReciterBottomSheet.TAG)

    }

    private fun getPagerFormatter(): QuranPageInitializer {
        val popupActions = PopupActions(readQuranActivity, this)
        val textDecorator =
            FunctionalQuranText(readQuranActivity, popupActions)
        return QuranPageInitializer(textDecorator, readQuranActivity)
    }

}


