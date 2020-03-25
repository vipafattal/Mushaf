package com.brilliancesoft.mushaf.utils

/**
 * Created by ${User} on ${Date}
 */
interface RecyclerViewItemClickedListener<T : Any> {
    fun onItemClicked(dataItem: T, currentPosition: Int)
}