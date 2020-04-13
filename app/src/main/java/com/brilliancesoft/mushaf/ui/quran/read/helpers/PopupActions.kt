package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.view.View
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.utils.extensions.onClicks
import com.codebox.lib.android.animators.simple.alphaAnimator
import com.codebox.lib.android.animators.simple.scale.scaleAnimator
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.views.utils.invisible
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.popup_quran.view.*

/**
 * Created by ${User} on ${Date}
 */
class PopupActions(
    private val popupView: View,
    private val popupOnAyaClickListener: PopupOnAyaClickListener
) {
    private var lastClickedPopupItemId = 0
    private val scalingTime: Long = 250

    private val popupContainer: View = popupView.parent as View

    fun show(y: Int, ayaNumber: Int, doOnHide: (isBookmarkStatusChanged: Boolean) -> Unit) {

        val aya = QuranViewModel.MainQuranList.first { it.number == ayaNumber }
        popupContainer.visible()
        popupView.alpha = 1f
        popupView.scaleAnimator(0.5f, 1f, scalingTime, 0)

        updatePopupViewLocation(y)
        updateClickedAyaClick(aya, doOnHide)
        popupOnAyaClickListener.onShow()

    }

    private fun dismiss() {
        popupView.alphaAnimator(scalingTime, valueTo = 0f) { popupContainer.invisible() }
        lastClickedPopupItemId = 0
        popupOnAyaClickListener.onDismissed()
    }

    private fun updatePopupViewLocation(y: Int) {
        val context = popupView.context
        val popupHeight = context.resources.getDimension(R.dimen.popup_quran_height)
        val yCoordinate =
            if (y - popupHeight < 0) popupHeight + dp(100)
            else y - popupHeight

        popupView.y = yCoordinate
    }

    private inline fun setOutSideClickListener(crossinline doOnHide: (isBookmarkStatusChanged: Boolean) -> Unit) {
        popupContainer.setOnClickListener {
            doOnHide.invoke(lastClickedPopupItemId == R.id.bookmark_popup)
            dismiss()
        }
    }

    private fun updateBookmarkView(aya: Aya) {
        if (aya.isBookmarked) popupView.bookmarkPopupTextView.setText(R.string.bookmark_remove)
        else popupView.bookmarkPopupTextView.setText(R.string.bookmark_add)
    }

    private inline fun updateClickedAyaClick(
        aya: Aya,
        crossinline doOnHide: (isBookmarkStatusChanged: Boolean) -> Unit
    ) {
        updateBookmarkView(aya)

        popupView.apply {
            setOutSideClickListener(doOnHide)

            onClicks(
                play_popup,
                share_popup,
                translate_popup,
                wordByWord_popup,
                bookmark_popup
            ) {
                lastClickedPopupItemId = id
                popupOnAyaClickListener.popupItemClicked(aya, this)
                popupContainer.callOnClick()
            }
        }
    }


    interface PopupOnAyaClickListener {
        fun popupItemClicked(aya: Aya, view: View)
        fun onDismissed(){}
        fun onShow(){}
    }
}