package com.brilliancesoft.mushaf.ui.quran.read.page

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.QuranFormattedPage
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.ui.quran.read.helpers.selectedTextToOutput
import com.brilliancesoft.mushaf.utils.TextActionUtil
import com.brilliancesoft.mushaf.utils.TextSelectionCallback
import com.brilliancesoft.mushaf.utils.extensions.setTextSizeFromType
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import kotlinx.android.synthetic.main.layout_page_number.view.*
import kotlinx.android.synthetic.main.layout_surah_header.view.*

class PageSurahsAdapter(
    private val quranFormattedPageList: List<QuranFormattedPage>,
    private val quranActions: QuranActions,
    @StringRes private val textSizeType:Int
) :
    RecyclerView.Adapter<PageSurahsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            parent,
            viewType,
            quranActions
        )
    }

    override fun getItemViewType(position: Int): Int =
        if (position == quranFormattedPageList.lastIndex)
            R.layout.item_quran_surah_with_footer
        else R.layout.item_quran_surah

    override fun getItemCount(): Int = quranFormattedPageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quranFormattedPage = quranFormattedPageList[position]
        holder.bind(quranFormattedPage)
    }

   inner class ViewHolder(
        itemView: ViewGroup, viewType: Int,
        private val quranActions: QuranActions
    ) :
        RecyclerView.ViewHolder(itemView.inflater(viewType)),
        TextSelectionCallback.OnActionItemClickListener {

        fun bind(quranFormattedPage: QuranFormattedPage) {
            val surah = quranFormattedPage.aya.surah!!

            itemView.apply {
                if (quranFormattedPage.isNewSurah) {
                    surah_name_view_root.visible()
                    if (!surah.isFatihaOrTawba) {
                        if (UserPreferences.isDarkThemeEnabled) bismillahSurah.setImageResource(R.drawable.ic_bismillah_light)
                        else bismillahSurah.setImageResource(R.drawable.ic_bismillah_dark)
                    } else
                        bismillahSurah.gone()
                } else
                    surah_name_view_root.gone()
                surahNameArabic.text = surah.name
                pageTextQuran.setTextSizeFromType(textSizeType)
                pageTextQuran.setText(quranFormattedPage.pagedText, TextView.BufferType.SPANNABLE)
                pageNumberQuran?.text = quranFormattedPage.aya.page.toLocalizedNumber()
                pageTextQuran.selectionTextCallBack(quranFormattedPage, this@ViewHolder)
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
            val readData = data as QuranFormattedPage
            when (item.itemId) {
                R.id.option_menu_translate -> {
                    quranActions.showWordsTranslation(readData.aya)
                    return true
                }
                TextSelectionCallback.Copy, TextSelectionCallback.Share -> {
                    val selectedText = data.pagedText.substring(selectedRange)

                    if (selectedText.isNotEmpty() && selectedText.isNotBlank()) {
                        val outputFriendlyText =
                            selectedTextToOutput(itemView.context, selectedText, readData.aya)

                        if (item.itemId == TextSelectionCallback.Share)
                            TextActionUtil.shareText(
                                itemView.context,
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
    }


}