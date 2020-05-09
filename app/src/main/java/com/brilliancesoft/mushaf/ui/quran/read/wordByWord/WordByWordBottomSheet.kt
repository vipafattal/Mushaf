package com.brilliancesoft.mushaf.ui.quran.read.wordByWord


import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.dialog.BaseBottomSheetDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.dialog_word_by_word.*

class WordByWordBottomSheet : BaseBottomSheetDialog(R.layout.dialog_word_by_word) {

    companion object {
        const val TAG = "WordByWordBottomSheet"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as BaseActivity).systemUiVisibility(false)

        val wordByWordViewModel = viewModelOf<WordByWordViewModel>()

        wordByWordViewModel.getWordByWord().observer(viewLifecycleOwner) {
            recycler_wordByWord.adapter = WordByWordAdapter(it)
        }

        close_image.onClick { dismiss() }
    }

    override fun onDialogScrolled(slideOffset: Float) {
        if (dialogOffsetLimit <  slideOffset) close_image?.visible()
        else close_image?.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as BaseActivity).systemUiVisibility(true)
    }
}
