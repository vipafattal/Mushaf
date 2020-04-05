package com.brilliancesoft.mushaf.utils.extensions

import android.text.Spannable
import com.brilliancesoft.mushaf.utils.CustomClickableSpan

/*fun whiteSpaceMagnifier(text: String): String {
    //text.replace(" ", " ")
    return text
}*/


fun Spannable.isClickableSpan(start: Int, end: Int): Boolean {
    val styleSpanCustoms: Array<out CustomClickableSpan> =
        getSpans(start, end, CustomClickableSpan::class.java)
    var isClickableSpan = false
    for (style in styleSpanCustoms) {
        isClickableSpan = true
        break
    }
    return isClickableSpan
}

