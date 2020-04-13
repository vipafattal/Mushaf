package com.brilliancesoft.mushaf.ui.more.textSize

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.RecyclerViewItemClickedListener
import com.brilliancesoft.mushaf.ui.common.dialog.BaseBottomSheetDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.dialog_font_size.*
import kotlinx.android.synthetic.main.dialog_font_size.close_image

class TextSizeBottomSheet : BaseBottomSheetDialog(R.layout.dialog_font_size),
    RecyclerViewItemClickedListener<Int> {

    companion object {
        private val TAG = this::class.java.simpleName

        fun show(fragmentManager: FragmentManager) {
            TextSizeBottomSheet().apply {
                show(
                    fragmentManager,
                    TAG
                )
            }
        }
    }

    private lateinit var textSizeViewModel: TextSizeViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val selectedTextSize = UserPreferences.getFontSize()
        val fontSize = arrayOf(R.string.small_font_size, R.string.medium_font_size, R.string.large_font_size, R.string.x_large_font_size)
        fontSizeRecycler.adapter =
            FontSizeAdapter(
                fontSize,
                selectedTextSize,
                this
            )

        textSizeViewModel = viewModelOf<TextSizeViewModel>()

        val closeIcon = if (UserPreferences.isDarkThemeEnabled) R.drawable.ic_close_light
        else R.drawable.ic_close_dark

        close_image.setImageResource(closeIcon)
        close_image.onClick { dismiss() }
    }

    override fun onItemClicked(dataItem: Int, currentPosition: Int) {
        UserPreferences.saveFontSize(dataItem)
        textSizeViewModel.selectedTextSize.postValue(dataItem)
        dismiss()
    }

    override fun onDialogScrolled(slideOffset: Float) {
        if (dialogOffsetLimit < slideOffset) close_image?.visible()
        else close_image?.gone()
    }
}