package com.brilliancesoft.mushaf.utils.extensions

/**
 * Created by ${User} on ${Date}
 */

fun <T> MutableCollection<T>.addIfNotNull(element:T?){
    if (element != null)
        add(element)
}