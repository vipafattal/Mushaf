package com.brilliancesoft.mushaf.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.utils.TextTypeOpt
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.library.read.ReadLibraryActivity
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.invisible
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.item_search.view.*
import kotlinx.serialization.json.Json

/**
 * Created by ${User} on ${Date}
 */
class SearchAdapter(
    private val dataList: List<Aya>,
    @TextTypeOpt
    private val searchType: String
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_search)
        return ViewHolder(v, searchType)
    }

    override fun getItemCount(): Int = dataList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val isLastPosition = dataList.size - 1 == position

        holder.bindData(data, isLastPosition)
    }

    class ViewHolder(itemView: View, private val searchType: String) :
        RecyclerView.ViewHolder(itemView) {

        private var preferences = AppPreferences()

        @SuppressLint("SetTextI18n")
        fun bindData(aya: Aya, isLastPosition: Boolean) {
            itemView.apply {
                aya_number_search.text = aya.numberInSurah.toString()
                if (searchType == Edition.TYPE_QURAN) {
                    val typeface = ResourcesCompat.getFont(context, R.font.kitab_bold)
                    aya_text_search.setLineSpacing(dp(1).toFloat(), 1.2f)
                    aya_text_search.typeface = typeface
                    aya_text_search.text = aya.text

                } else
                    aya_text_search.text = aya.text

                var searchInfo =
                    if (isRightToLeft == 1) aya.surah!!.englishName else aya.surah!!.name
                searchInfo += if (searchType != Edition.TYPE_QURAN) " ${context.getString(R.string.In)} ${aya.edition!!.name} " else ""

                found_in_search.text = " ${context.getString(R.string.found)} $searchInfo"
                if (isLastPosition)
                    divider_item_search.invisible()
                else
                    divider_item_search.visible()
            }

            activeItemClick(aya)

        }


        private fun activeItemClick(aya: Aya) {
            itemView.search_item_root_view.onClick {
                val bundle = Bundle()
                val intent: Intent
                if (searchType == Edition.TYPE_QURAN) {
                    bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, aya.page)
                    bundle.putString(
                        ReadQuranActivity.START_AT_AYA,
                        Json.encodeToString(Aya.serializer(), QuranViewModel.MainQuranList[aya.number-1])
                    )
                    ReadQuranActivity.startNewActivity(context, bundle)
                } else {
                    intent = context.newIntent<ReadLibraryActivity>()
                    val editionIdentifier = aya.edition!!.identifier

                    bundle.putString(ReadLibraryActivity.EditionIdKey, editionIdentifier)
                    intent.putExtras(bundle)

                    preferences.put(
                        PreferencesConstants.LastScrollPosition + editionIdentifier,
                        aya.numberInSurah - 1
                    )
                    preferences.put(
                        PreferencesConstants.LastSurahViewed + editionIdentifier,
                        aya.surah!!.number
                    )
                    context.startActivity(intent)
                }
            }
        }

    }


}