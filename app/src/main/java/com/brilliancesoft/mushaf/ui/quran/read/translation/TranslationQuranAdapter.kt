package com.brilliancesoft.mushaf.ui.quran.read.translation

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.common.Fonts
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.extensions.setTextSizeFromType
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_translation.view.*

/**
 * Created by ${User} on ${Date}
 */
class TranslationQuranAdapter(private val translationData: List<Aya>) :
    RecyclerView.Adapter<TranslationQuranAdapter.ViewHolder>() {

    private val textSizeType = UserPreferences.getFontSize()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            parent,
            R.layout.item_translation
        )


    override fun getItemCount(): Int = translationData.size
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val aya = translationData[position]
        holder.itemView.apply {
           translationName.text = aya.edition!!.name
           translation_quran.text = aya.text
           translation_quran.setTextSizeFromType(textSizeType)
           translation_quran.typeface = Fonts.getTranslationFont(context,aya.edition!!.language)
        }
    }
    
    class ViewHolder(parent: ViewGroup, @LayoutRes layout: Int) : RecyclerView.ViewHolder(parent.inflater(layout))

}