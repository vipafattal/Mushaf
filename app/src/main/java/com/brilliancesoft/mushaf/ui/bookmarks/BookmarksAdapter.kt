package com.brilliancesoft.mushaf.ui.bookmarks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.library.read.ReadLibraryActivity
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.utils.toCurrentLanguageNumber
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_bookmark.view.*
import kotlinx.android.synthetic.main.item_bookmark_with_header.view.*
import kotlinx.serialization.json.Json

/**
 * Created by ${User} on ${Date}
 */
class BookmarksAdapter(
    private val dataList: List<Aya>,
    private val fragment: BookmarksFragment
) : RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(viewType, parent, fragment)

    override fun getItemViewType(position: Int): Int {
        val currentData = dataList[position]
        val prvData = if (position != 0) dataList[position - 1] else dataList[position]
        val prvType = prvData.edition!!.type

        return when {
            position == 0 -> itemWithHeader
            currentData.edition!!.type != prvType -> itemWithHeader
            else -> itemBookmark
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bindData(data)
    }


    class ViewHolder(
        @LayoutRes private val layoutId: Int, parent: ViewGroup,
        private val fragment: BookmarksFragment
    ) : RecyclerView.ViewHolder(parent.inflater(layoutId)) {

        private var preferences = AppPreferences()

        fun bindData(aya: Aya) {
            if (layoutId == itemWithHeader)
                itemView.bindItemWithHeader(aya)
            else itemView.bindItem(aya)
        }

        @SuppressLint("SetTextI18n")
        private fun View.bindItem(aya: Aya) {
            surah_name_bookmark.text = if (isRightToLeft == 1) aya.surah!!.englishName else aya.surah!!.name
            val page = context.getString(R.string.page) + " ${aya.page.toString().toCurrentLanguageNumber()}"
            val juz = context.getString(R.string.juz) + " ${aya.juz.toString().toCurrentLanguageNumber()}"
            val numberInSurah = context.getString(R.string.aya) + " ${aya.numberInSurah.toString().toCurrentLanguageNumber()}"
            val editionName = if (aya.edition!!.type != Edition.Quran) aya.edition!!.name else ""
            info_bookmark.text = "$page, $juz, $numberInSurah, $editionName"

            item_bookmark_root_view.onClick {
                val bundle = Bundle()
                val intent: Intent
                if (aya.edition!!.type == Edition.Quran) {
                    bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, aya.page)
                    bundle.putString(
                        ReadQuranActivity.START_AT_AYA,
                        Json.stringify(Aya.serializer(), aya)
                    )
                    ReadQuranActivity.startNewActivity(context,bundle)
                } else {
                    intent = context.newIntent<ReadLibraryActivity>()
                    val editionIdentifier = aya.edition!!.identifier
                    bundle.putString(ReadLibraryActivity.EditionIdKey, editionIdentifier)
                    intent.putExtras(bundle)

                    preferences.put(PreferencesConstants.LastScrollPosition + editionIdentifier, aya.numberInSurah - 1)
                    preferences.put(PreferencesConstants.LastSurahViewed + editionIdentifier, aya.surah!!.number)

                    context.startActivity(intent)
                }
            }
            remove_bookmark.onClick {
                fragment.removeBookmarked(aya)
            }
        }

        private fun View.bindItemWithHeader(aya: Aya) {
            type_header.text = when (aya.edition!!.type) {
                Edition.Quran -> context.getString(R.string.quran)
                Edition.Tafsir -> context.getString(R.string.tafseer)
                else -> context.getString(R.string.translation)
            }

            bindItem(aya)
        }

    }

    companion object {
        const val itemWithHeader = R.layout.item_bookmark_with_header
        const val itemBookmark = R.layout.item_bookmark
    }
}