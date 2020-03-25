package com.brilliancesoft.mushaf.ui.quran.read.translation

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_translation.view.*

/**
 * Created by ${User} on ${Date}
 */
class TranslationQuranAdapter(private val dataList: List<String>) :
    RecyclerView.Adapter<TranslationQuranAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            parent,
            R.layout.item_translation
        )
    
    
    override fun getItemCount(): Int = dataList.size
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bindData(data)
    }
    
    class ViewHolder(parent: ViewGroup, @LayoutRes layout: Int) : RecyclerView.ViewHolder(parent.inflater(layout)) {
        fun bindData(text: String) {
            itemView.translation_quran.text = text
        }
    }
    
    
}