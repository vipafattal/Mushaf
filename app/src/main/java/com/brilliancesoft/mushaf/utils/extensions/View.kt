package com.brilliancesoft.mushaf.utils.extensions

import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.utils.OnPageSelectedListener
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.utils.CustomClickableSpan
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.os.wait
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.views.listeners.onClick
import com.google.android.material.chip.Chip


/**
 * Created by ${User} on ${Date}
 */


inline fun <T : View> T.onLongClick(crossinline block: T.() -> Unit) {
    setOnLongClickListener {
        block()
        false
    }
}

fun TextView.setStartDrawable(@DrawableRes drawableRes: Int) {
    if (isRightToLeft == 1) setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
    else setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

}

inline fun <T : View> onClicks(vararg views: T, crossinline block: T.() -> Unit) {
    for (view in views)
        view.onClick(block)
}

fun QuranTextView.callClickOnSpan(aya: Aya) {
    val start = text.indexOf(aya.numberInSurah.toLocalizedNumber()) - 1
    val end = start + 1

    val customClickableSpans: Array<out CustomClickableSpan> =
        text.toSpannable().getSpans(start, end, CustomClickableSpan::class.java)

    if (customClickableSpans.isEmpty())
        throw IllegalArgumentException("the given range(start..end) doesn't contain any clickable span")

    val clickableSpan = customClickableSpans[0]
    clickableSpan.onClick(this)
    (parent.parent.parent as NestedScrollView).smoothScrollTo(
        clickableSpan.clickX,
        clickableSpan.clickY
    )
    Handler().wait(200) {
        clickableSpan.onClick(this)
    }
}

fun scrollToView(
    scrollView: NestedScrollView,
    scrollableContent: ViewGroup,
    viewToScroll: View
) {
    val delay: Long = 100 //delay to let finish with possible modifications to ScrollView
    scrollView.postDelayed({
        val viewToScrollRect =
            Rect() //coordinates to scroll to
        viewToScroll.getHitRect(viewToScrollRect) //fills viewToScrollRect with coordinates of viewToScroll relative to its parent (LinearLayout)
        scrollView.requestChildRectangleOnScreen(
            scrollableContent,
            viewToScrollRect,
            false
        ) //ScrollView will make sure, the given viewToScrollRect is visible
    }, delay)
}

inline fun ViewPager.addOnPageSelectedListener(crossinline block: (position: Int) -> Unit) {
    addOnPageChangeListener(object : OnPageSelectedListener {

        override fun onPageSelected(position: Int) {
            block.invoke(position)
        }
    })

}

inline fun RecyclerView.onScroll(crossinline action: (dx: Int, dy: Int) -> Unit) {

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            action(dx, dy)
        }
    })
}

fun Chip.unChecked() {
    setChipBackgroundColorResource(R.color.colorPrimary)
    setTextColor(Color.WHITE)
    putElevation(0f)
}

fun Chip.checked(elevation: Float) {
    setChipBackgroundColorResource(R.color.white)
    setTextColor(Color.BLACK)
    putElevation(elevation)
}

fun View.putElevation(value: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = value
    }
}