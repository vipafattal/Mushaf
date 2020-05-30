package com.brilliancesoft.mushaf.ui.quran.read.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_navigete_to_page.*

/**
 * Created by ${User} on ${Date}
 */
class NavigateToPageDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "NavigateToPageDialog"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        page_number_edit_text.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO && !page_number_edit_text.text.isNullOrBlank()) {
                getToPage(page_number_edit_text.text.toString().toInt())
            } else
                activity?.let { MushafToast.makeShort(it,R.string.enter_number) }
            true
        }
    }


    private fun getToPage(pageNumber: Int) {
        if (pageNumber in 1..604) {
                val bundle = Bundle()
                bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, pageNumber)
                ReadQuranActivity.startNewActivity(requireContext(),bundle)
                dismiss()
        } else
            activity?.let { MushafToast.makeLong(it,R.string.enter_page_number) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_navigete_to_page, container, false)
}
