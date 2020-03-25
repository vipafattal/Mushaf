package com.brilliancesoft.mushaf.ui.library.manage

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.brilliancesoft.mushaf.R
import com.codebox.lib.android.resoures.Stringify
import kotlinx.android.synthetic.main.item_tab_layout.view.*

class TabPagerAdapter(private val context: Context, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {

    private val tabsTitle = arrayOf(Stringify(R.string.tafseer, context), Stringify(R.string.translate, context))
    private val tabIcons = intArrayOf(R.drawable.ic_defining, R.drawable.ic_translate/*, R.drawable.ic_reciter*/)

    override fun getPageTitle(position: Int): CharSequence = tabsTitle[position]
    override fun getCount(): Int = tabsTitle.size
    override fun getItem(position: Int): Fragment =
        TabFragment.newInstance(tabPosition = position)

    fun bindView(position: Int): View {
        val tabView = View.inflate(context, R.layout.item_tab_layout, null)
        tabView.icon_tab.setImageResource(tabIcons[position])
        tabView.title_tab.text = tabsTitle[position]
        return tabView
    }

}