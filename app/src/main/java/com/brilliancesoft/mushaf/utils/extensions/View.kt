package com.brilliancesoft.mushaf.utils.extensions

import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.utils.TabStyle
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranTextView
import com.brilliancesoft.mushaf.utils.CustomClickableSpan
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.views.listeners.onClick
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


/**
 * Created by ${User} on ${Date}
 */

/*
fun ViewPager2.zoomOutPages(pageMarginPx: Int, offsetPx: Int) {

 clipToPadding = false
    clipChildren = false
    offscreenPageLimit = 3
    (adapter as? QuranPagerAdapter)?.updateZoom(this,true,currentItem)

    // Save state
    setPageTransformer { page, position ->

        val offset = position * -(2 * offsetPx + pageMarginPx)
        if (this.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                page.translationX = -offset
            } else {
                page.translationX = offset
            }
        } else {
            page.translationY = -offset
        }
    }
}

fun ViewPager2.restZoom() {

   offscreenPageLimit = 1
    (adapter as? QuranPagerAdapter)?.updateZoom(this,false,currentItem)
    setPageTransformer { page, _ ->

        if (this.orientation == ViewPager2.ORIENTATION_HORIZONTAL)
            page.translationX = 0f
         else
            page.translationY = 0f

    }
}*/

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

    (parent.parent.parent as RecyclerView).scrollTo(0, clickableSpan.clickY)
}

fun TextView.setTextSizeFromType(@StringRes type: Int) {
    val textDimenRes = when (type) {
        R.string.small_font_size -> R.dimen._16ssp
        R.string.medium_font_size -> R.dimen._19ssp
        R.string.large_font_size -> R.dimen._25ssp
        R.string.x_large_font_size -> R.dimen._29ssp
        else -> throw IllegalArgumentException("Unknown text size type")
    }

    setTextSize(
        TypedValue.COMPLEX_UNIT_PX,
        MushafApplication.appContext.resources.getDimension(textDimenRes)
    )
}

inline fun ViewPager2.addOnPageSelectedListener(crossinline block: (position: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            block.invoke(position)
        }
    })
}

fun ViewPager2.setTabStyle(tabs: TabLayout, tabPosition: (position: Int) -> TabStyle) {
    TabLayoutMediator(tabs,
        this,
        TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            val tabStyle = tabPosition(position)
            tab.text = tabStyle.title
            tab.setIcon(tabStyle.icon)
        }).attach()

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