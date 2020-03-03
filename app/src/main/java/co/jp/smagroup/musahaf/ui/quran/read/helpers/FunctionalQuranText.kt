package co.jp.smagroup.musahaf.ui.quran.read.helpers

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.utils.ClickableImageSpan
import co.jp.smagroup.musahaf.utils.TextDrawable
import co.jp.smagroup.musahaf.utils.extensions.toBitmap
import co.jp.smagroup.musahaf.utils.extensions.toSpannable
import co.jp.smagroup.musahaf.utils.toLocalizedNumber
import com.codebox.lib.android.utils.screenHelpers.dp

/**
 * Created by ${User} on ${Date}
 */
class FunctionalQuranText(private val context: Context, private val popupActions: PopupActions) {
    private val ayaNumberColor = if (MushafApplication.isDarkThemeEnabled) Color.WHITE else Color.BLACK
    private var clickedStartSpanPosition = 0
    private var clickedEndSpanPosition = 0

    fun getQuranDecoratedText(
        str: String,
        previousAyaLength: Int,
        aya: Aya
    ): SpannableString {
        var bookmarkState = aya.isBookmarked
        val ayaNumberInSurah = aya.numberInSurah.toString().toLocalizedNumber()
        val text = SpannableString(str)

        val startChar = text.length - ayaNumberInSurah.length - 1
        val endChar = text.length - 1

        val span = getAyaImageNumber(aya.numberInSurah, bookmarkState)
        text.setSpan(span, startChar, endChar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        text.setSpan(object : ClickableImageSpan() {
            override fun onClick(widget: TextView, x: Int, y: Int) {

                highlightSelection(widget, text, previousAyaLength)

                popupActions.show(x, y, aya) { bookmarkStateChanged ->
                    //Clear Highlighted on touch out side popup.
                    val clickedTextSpan = widget.text.toSpannable()
                    if (bookmarkStateChanged) {
                        bookmarkState = if (bookmarkStateChanged) !bookmarkState else bookmarkState
                        val newSpan = getAyaImageNumber(aya.numberInSurah, bookmarkState)

                        clickedTextSpan.setSpan(
                            newSpan,
                            previousAyaLength + startChar,
                            previousAyaLength + endChar,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    clearHighlighted(clickedTextSpan, true)
                }

            }
        }, startChar, endChar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //for image clickable
        return text
    }

    private fun highlightSelection(view: TextView, text: CharSequence, startAtIndex: Int) {
        val spannable = view.text.toSpannable()
        //On outside popup click clear selection.
        //Clear previous selection.
        clearHighlighted(spannable, false)
        clickedEndSpanPosition = startAtIndex + text.length - 1
        clickedStartSpanPosition = startAtIndex
        spannable.setSpan(
            TouchAyaHighlighter(),
            clickedStartSpanPosition,
            clickedEndSpanPosition,
            0
        )
    }

    private fun clearHighlighted(spannable: Spannable, clearAll: Boolean) {
        if (clearAll) {
            clickedStartSpanPosition = 0
            clickedEndSpanPosition = spannable.length - 1
        }

        val styleSpans: Array<out TouchAyaHighlighter> =
            spannable.getSpans(
                clickedStartSpanPosition,
                clickedEndSpanPosition,
                TouchAyaHighlighter::class.java
            )
        for (style in styleSpans)
            spannable.removeSpan(style)
    }

    private fun getAyaImageNumber(numberInSurah: Int, isBookmarked: Boolean): ImageSpan {

        val ayaDecorImg = if (isBookmarked) R.drawable.ic_aya_number_bookmarked else R.drawable.ic_aya_number

        val endAyaImage = TextDrawable.builder()
            .beginConfig().fontSize(dp(16)).textColor(ayaNumberColor).bold().endConfig()
            .buildTextVectorImage(context, numberInSurah.toString().toLocalizedNumber(), ayaDecorImg, dp(38), dp(38))

        return ImageSpan(context, endAyaImage.toBitmap(), ImageSpan.ALIGN_BASELINE)
    }
}