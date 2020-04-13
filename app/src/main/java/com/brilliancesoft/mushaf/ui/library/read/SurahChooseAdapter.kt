package com.brilliancesoft.mushaf.ui.library.read

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Surah
import com.brilliancesoft.mushaf.ui.common.RecyclerViewItemClickedListener
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_library_choose_surah.view.*

/**
 * Created by ${User} on ${Date}
 */
class SurahChooseAdapter(
    private val dataList: List<Surah>,
    private val clickListener: RecyclerViewItemClickedListener<Surah>
) : RecyclerView.Adapter<SurahChooseAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_library_choose_surah)
        return ViewHolder(v)
    }
    
    override fun getItemCount(): Int {
        return dataList.size
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        //val mContext = holder.itemView.context
        
        holder.bindView(data,position)
        
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        fun bindView(data: Surah, position: Int) {
            itemView.surahName.text = if (isRightToLeft == 1) data.englishName else data.name
            itemView.surahNumber.text = data.number.toString()
            itemView.pageNumber_surah.text = data.numberOfAyahs.toString()
            
            itemView.onClick { clickListener.onItemClicked(data, position) }
        }
        
    }
    
    
}