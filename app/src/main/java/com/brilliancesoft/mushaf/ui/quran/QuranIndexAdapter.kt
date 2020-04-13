package com.brilliancesoft.mushaf.ui.quran

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.utils.extensions.bundleOf
import com.brilliancesoft.mushaf.utils.getAyaWord
import com.brilliancesoft.mushaf.utils.toCurrentLanguageNumber
import com.brilliancesoft.mushaf.utils.toLocalizedRevelation
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_juz.view.*
import kotlinx.android.synthetic.main.item_surah.view.*


/**
 * Created by ${User} on ${Date}
 */
class QuranIndexAdapter(
    private val ayaList: List<Aya>,
    private val isReadingFromLibrary: Boolean
) : RecyclerView.Adapter<QuranIndexAdapter.ViewHolder>() {

    val headerView = R.layout.item_juz
    val surahView = R.layout.item_surah
    private val fullView = R.layout.item_surah_and_juz


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(viewType)
        return ViewHolder(v, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val currentData = ayaList[position]
        val prvData = if (position != 0) ayaList[position - 1] else ayaList[position]

        val prvJuz = prvData.juz
        val prvSurah = prvData.surah!!.number

        return when {
            isReadingFromLibrary -> surahView
            position == 0 -> fullView
            currentData.juz != prvJuz && currentData.surah!!.number == prvSurah -> headerView
            currentData.juz == prvJuz && currentData.surah!!.number != prvSurah -> surahView
            currentData.juz != prvJuz && currentData.surah!!.number != prvSurah -> fullView
            else -> headerView
        }
    }

    override fun getItemCount(): Int {
        return ayaList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val aya = ayaList[position]
        // val mContext = holder.itemView.context
        holder.bindData(aya)

    }

    inner class ViewHolder(itemView: View, private val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(aya: Aya) {
            when (viewType) {
                headerView -> bindHeader(aya)
                surahView -> bindSurah(aya)
                else -> bindHeaderAndSurah(aya)
            }
        }

        private fun bindHeader(aya: Aya) {
            itemView.juzNumber.text =
                "${Stringify(R.string.juz, itemView.context)} ${aya.juz.toCurrentLanguageNumber()}"
            itemView.pageNumberQuran.text = aya.page.toString().toCurrentLanguageNumber()
            itemView.juz_view_root.enableOnClick(aya.page,-1)
        }

        private fun bindSurah(aya: Aya) {
            itemView.apply {
                pageNumber_surah.text = aya.page.toString().toCurrentLanguageNumber()

                if (isRightToLeft == 1) surahName.text = aya.surah!!.englishName
                else surahName.text = aya.surah!!.name

                surahNumber.text = aya.surah!!.number.toString().toCurrentLanguageNumber()
                surahInfo.text =
                    "${aya.surah!!.revelationType.toLocalizedRevelation()} - ${aya.surah!!.numberOfAyahs} ${aya.surah!!.numberOfAyahs.getAyaWord()}"
                surah_view_root.enableOnClick(aya.page, aya.surah_number)
            }
        }

        private fun bindHeaderAndSurah(aya: Aya) {
            bindHeader(aya)
            bindSurah(aya)
        }

        private fun View.enableOnClick(startAtPage: Int, startAtSurah: Int) {
            onClick {
                val intent = context.newIntent<ReadQuranActivity>()
                val bundle = bundleOf(
                    ReadQuranActivity.START_AT_PAGE_KEY to startAtPage,
                    ReadQuranActivity.START_AT_SURAH_KEY to startAtSurah
                )
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
        }
    }


}