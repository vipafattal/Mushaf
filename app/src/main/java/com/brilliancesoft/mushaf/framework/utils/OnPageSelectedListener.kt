package com.brilliancesoft.mushaf.framework.utils

import androidx.viewpager.widget.ViewPager

/**
 * Created by ${User} on ${Date}
 */
@FunctionalInterface
interface OnPageSelectedListener: ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
}