package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.CustomClickableSpan
import com.brilliancesoft.mushaf.utils.TextDrawable
import com.brilliancesoft.mushaf.utils.extensions.getTextSizeFromType
import com.brilliancesoft.mushaf.utils.extensions.toBitmap
import com.brilliancesoft.mushaf.utils.extensions.toSpannable
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import kotlin.math.roundToInt

/**
 * Created by ${User} on ${Date}
 */
class QuranicSpanText(
    textSizeType: Int,
    private val context: Context,
    private val popupActions: PopupActions?
) {

    var textSize: Float = getTextSizeFromType(textSizeType)

    private val ayaNumberColor =
        if (UserPreferences.isDarkThemeEnabled) Color.WHITE else Color.BLACK
    private var clickedStartSpanPosition = 0
    private var clickedEndSpanPosition = 0

    fun applyPreviewSpans(rawText: String, TextSizeType: Int): SpannableString {
        val text = SpannableString(rawText)
        textSize = getTextSizeFromType(TextSizeType)
        for (i in text.indices) {
            val ch = text[i]

            if (ch.isDigit()) {
                text.setSpan(
                    getAyaImageNumber(ch.toString().toInt(), false),
                    i - 1,
                    i + 1,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }
        return text
    }

    fun applyQuranSpans(
        formattedAyaText: String,
        previousAyaLength: Int,
        aya: Aya
    ): SpannableString {
        require(popupActions != null)

        var bookmarkState = aya.isBookmarked
        val ayaNumberInSurah = aya.numberInSurah.toString().toLocalizedNumber()
        val text = SpannableString(formattedAyaText)

        val startChar = text.length - ayaNumberInSurah.length - 1
        val endChar = text.length - 1

        text.setSpan(
            getAyaImageNumber(aya.numberInSurah, bookmarkState),
            startChar,
            endChar,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        text.setSpan(object : CustomClickableSpan() {
            override fun onClick(widget: TextView, x: Int, y: Int) {

                highlightSelection(widget, text, previousAyaLength)
                popupActions.show(y, aya.number) { bookmarkStateChanged ->
                    val clickedTextSpan = widget.text.toSpannable()
                    //Update Aya image if bookmark state changed.
                    if (bookmarkStateChanged) {
                        bookmarkState = if (bookmarkStateChanged) !bookmarkState else bookmarkState

                        clickedTextSpan.setSpan(
                            getAyaImageNumber(aya.numberInSurah, bookmarkState),
                            previousAyaLength + startChar,
                            previousAyaLength + endChar,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    //Clear Highlighted on touch out side popup.
                    clearHighlighted(clickedTextSpan)
                }
            }
        }, 0, endChar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //for image clickable
        return text
    }

    private fun highlightSelection(view: TextView, text: CharSequence, startAtIndex: Int) {
        val spannable = view.text.toSpannable()
        //On outside popup click clear selection.
        //Clear previous selection.
        clickedEndSpanPosition = startAtIndex + text.length - 1
        clickedStartSpanPosition = startAtIndex

        spannable.setSpan(
            TouchAyaHighlighter(),
            clickedStartSpanPosition,
            clickedEndSpanPosition,
            0
        )
    }

    private fun clearHighlighted(spannable: Spannable) {
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
        val ayaDecorImg =
            if (isBookmarked) R.drawable.ic_aya_number_bookmarked else R.drawable.ic_aya_number

        val endAyaImage = TextDrawable.builder()
            .beginConfig().fontSize((textSize * 0.60f).roundToInt()).textColor(ayaNumberColor)
            .bold().endConfig()
            .buildTextVectorImage(
                context,
                numberInSurah.toString().toLocalizedNumber(),
                ayaDecorImg,
                (textSize * 1.40).roundToInt(),
                (textSize * 1.40).roundToInt()
            )

        return ImageSpan(context, endAyaImage.toBitmap(), ImageSpan.ALIGN_BASELINE)
    }
}