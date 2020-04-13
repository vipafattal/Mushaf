package com.brilliancesoft.mushaf.ui.quran.read.wordByWord

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.extensions.setTextSizeFromType
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_word_by_word.view.*

/**
 * Created by ${User} on ${Date}
 */
class WordByWordAdapter(private val dataList: List<Pair<String?, String>>) :
    RecyclerView.Adapter<WordByWordAdapter.ViewHolder>() {

    private val textSizeType = UserPreferences.getFontSize()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_word_by_word)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        holder.itemView.apply {
            englishWord.text = data.first ?: "_______"
            englishWord.setTextSizeFromType(textSizeType)

            arabicWord.text = data.second
            arabicWord.setTextSizeFromType(textSizeType)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}