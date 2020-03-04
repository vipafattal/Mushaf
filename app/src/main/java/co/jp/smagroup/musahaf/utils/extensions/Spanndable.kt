package co.jp.smagroup.musahaf.utils.extensions

import android.text.Spannable
import co.jp.smagroup.musahaf.utils.ClickableImageSpan

fun whiteSpaceMagnifier(text: String): String {
    //text.replace(" ", " ")
    return text
}


fun Spannable.isClickableSpan(start: Int, end: Int): Boolean {
    val styleSpans: Array<out ClickableImageSpan> =
        getSpans(start, end, ClickableImageSpan::class.java)
    var isClickableSpan = false
    for (style in styleSpans) {
        isClickableSpan = true
        break
    }
    return isClickableSpan
}