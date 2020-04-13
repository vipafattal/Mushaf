package com.brilliancesoft.mushaf.ui.common

/**
 * Created by ${User} on ${Date}
 */
interface RecyclerViewItemClickedListener<T : Any> {
    fun onItemClicked(dataItem: T, currentPosition: Int)
}