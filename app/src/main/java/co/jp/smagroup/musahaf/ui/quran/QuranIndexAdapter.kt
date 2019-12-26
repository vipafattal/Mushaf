package co.jp.smagroup.musahaf.ui.quran;

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.ui.quran.read.ReadQuranActivity
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.utils.getAyaWord
import co.jp.smagroup.musahaf.utils.toCurrentLanguageNumber
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import co.jp.smagroup.musahaf.utils.toLocalizedRevelation
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
class QuranIndexAdapter(private val dataList: List<Aya>,
                        private val isReadingFromLibrary:Boolean) : RecyclerView.Adapter<QuranIndexAdapter.ViewHolder>() {

    val headerView = R.layout.item_juz
    val surahView = R.layout.item_surah
    private val fullView = R.layout.item_surah_and_juz


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(viewType)
        return ViewHolder(v, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val currentData = dataList[position]
        val prvData = if (position != 0) dataList[position - 1] else dataList[position]

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
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
       // val mContext = holder.itemView.context
        holder.bindData(data)

    }

    inner class ViewHolder(itemView: View, private val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        fun bindData(data: Aya) {
            when (viewType) {
                headerView -> bindHeader(data)
                surahView -> bindSurah(data)
                else -> bindHeaderAndSurah(data)
            }
        }

        private fun bindHeader(data: Aya) {
            itemView.juzNumber.text = "${Stringify(R.string.juz,itemView.context)} ${data.juz.toString().toCurrentLanguageNumber()}"
            itemView.pageNumber.text = data.page.toString().toCurrentLanguageNumber()
            itemView.juz_view_root.activeClick(data)
        }

        private fun bindSurah(data: Aya) {
            itemView.apply {
                pageNumber_surah.text = data.page.toString().toCurrentLanguageNumber()

                if (isRightToLeft == 1) surahName.text = data.surah!!.englishName
                else surahName.text = data.surah!!.name

                surahNumber.text = data.surah!!.number.toString().toLocalizedNumber()
                surahInfo.text = "${data.surah!!.revelationType.toLocalizedRevelation()} - ${data.surah!!.numberOfAyahs} ${data.surah!!.numberOfAyahs.getAyaWord()}"
                surah_view_root.activeClick(data)
            }
        }

        private fun bindHeaderAndSurah(data: Aya) {
            bindHeader(data)
            bindSurah(data)
        }

        private fun View.activeClick(data: Aya){
            onClick {
                val intent = context.newIntent<ReadQuranActivity>()
                val bundle = Bundle()
                bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, data.page)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
        }
    }


}