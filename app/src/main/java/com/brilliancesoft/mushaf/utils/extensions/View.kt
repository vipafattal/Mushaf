package com.brilliancesoft.mushaf.utils.extensions

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.utils.OnPageSelectedListener
import com.codebox.lib.android.views.listeners.onClick
import com.google.android.material.chip.Chip

/**
 * Created by ${User} on ${Date}
 */


inline fun <T:View> T.onLongClick(crossinline block: T.() -> Unit) {
    setOnLongClickListener {
        block()
        false
    }
}

inline fun <T:View> onClicks(vararg views: T, crossinline block: T.() -> Unit) {
    for (view in views)
        view.onClick(block)
}


inline fun ViewPager.addOnPageSelectedListener(crossinline block: (position:Int) -> Unit){
    addOnPageChangeListener(object : OnPageSelectedListener {

        override fun onPageSelected(position: Int) { block.invoke(position) }
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