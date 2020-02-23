package co.jp.smagroup.musahaf.utils.extensions

import android.text.Spannable
import android.text.style.BackgroundColorSpan
import co.jp.smagroup.musahaf.utils.ClickableImageSpan

fun whiteSpaceMagnifier(text: String): String =
    text.replace(" ", "     ")


fun Spannable.clearHighlighted(start: Int = 0, end: Int = length) {
    val styleSpans: Array<out BackgroundColorSpan> =
        getSpans(start, end, BackgroundColorSpan::class.java)
    for (style in styleSpans)
        removeSpan(style)
}

fun Spannable.highlightText(color: Int, start: Int, end: Int) {
    setSpan(BackgroundColorSpan(color), start, end, 0)
}

fun Spannable.isClickableSpan(start: Int, end: Int): Boolean {
    val styleSpans: Array<out ClickableImageSpan> = getSpans(start, end, ClickableImageSpan::class.java)
    var isClickableSpan = false
    for (style in styleSpans) {
        isClickableSpan = true
        break
    }
    return isClickableSpan

}