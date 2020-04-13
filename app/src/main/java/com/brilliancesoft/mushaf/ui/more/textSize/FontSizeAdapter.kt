package com.brilliancesoft.mushaf.ui.more.textSize

import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.Fonts
import com.brilliancesoft.mushaf.ui.common.RecyclerViewItemClickedListener
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranicSpanText
import com.brilliancesoft.mushaf.utils.extensions.onClicks
import com.brilliancesoft.mushaf.utils.extensions.setTextSizeFromType
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_font_size.view.*
import java.util.*

class FontSizeAdapter(
    private val sizeNamesRes: Array<Int>,
    private val selectedTextType: Int,
    private val onItemClickRecycler: RecyclerViewItemClickedListener<Int>
) : RecyclerView.Adapter<FontSizeAdapter.ViewHolder>() {

    private val appLanguage = Locale.getDefault().language
    private val quranicSpanText = QuranicSpanText(MushafApplication.appContext, null)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflater(R.layout.item_font_size))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sizeNamesRes[position])
    }

    override fun getItemCount(): Int = sizeNamesRes.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(sizeName: Int) {
            itemView.apply {
                textSizeName.text = context.getString(sizeName)

                selectedTextSizeRadio.isChecked = sizeName == selectedTextType
                quranTextViewSize.text =
                    quranicSpanText.applyPreviewSpans(context.getString(R.string.preview_quran))

                quranTextViewSize.setTextSizeFromType(sizeName)
                translationTextViewSize.setTextSizeFromType(sizeName)

                translationTextViewSize.typeface = Fonts.getTranslationFont(context, appLanguage)

                onClicks(itemView,textSizeRow, selectedTextSizeRadio) {
                    itemView.selectedTextSizeRadio.isChecked = true
                    onItemClickRecycler.onItemClicked(sizeName, absoluteAdapterPosition)
                }
            }
        }
    }
}