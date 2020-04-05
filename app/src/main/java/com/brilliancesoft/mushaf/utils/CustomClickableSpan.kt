package com.brilliancesoft.mushaf.utils

import android.graphics.Rect
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.brilliancesoft.mushaf.utils.extensions.toSpannable
import kotlin.math.roundToInt


/**
 * Created by ${User} on ${Date}
 */
abstract class CustomClickableSpan : ClickableSpan() {

    var clickX: Int = -1
        private set
    var clickY: Int = -1
        private set

    final override fun onClick(widget: View) {
        val parentTextView = widget as TextView

        val parentTextViewRect = Rect()

        // Initialize values for the computing of clickedText position
        val completeText = parentTextView.text.toSpannable()
        val textViewLayout = parentTextView.layout

        val startOffsetOfClickedText = completeText.getSpanStart(this).toDouble()
        val endOffsetOfClickedText = completeText.getSpanEnd(this).toDouble()

        val startXCoordinatesOfClickedText =
            textViewLayout.getPrimaryHorizontal(startOffsetOfClickedText.toInt()).toDouble()
        val endXCoordinatesOfClickedText =
            textViewLayout.getPrimaryHorizontal(endOffsetOfClickedText.toInt()).toDouble()


        // Get the rectangle of the clicked text
        val currentLineStartOffset =
            textViewLayout.getLineForOffset(startOffsetOfClickedText.toInt())
        val currentLineEndOffset = textViewLayout.getLineForOffset(endOffsetOfClickedText.toInt())
        val keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect)


        // Update the rectangle position to his real position on screen
        val parentTextViewLocation = intArrayOf(0, 0)
        parentTextView.getLocationOnScreen(parentTextViewLocation)

        val parentTextViewTopAndBottomOffset =
            (parentTextViewLocation[1] - parentTextView.scrollY + parentTextView.compoundPaddingTop).toDouble()
        parentTextViewRect.top += parentTextViewTopAndBottomOffset.roundToInt()
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset.roundToInt()

        parentTextViewRect.left += (parentTextViewLocation[0].toDouble() +
                startXCoordinatesOfClickedText +
                parentTextView.compoundPaddingLeft.toDouble() - parentTextView.scrollX).roundToInt()
        parentTextViewRect.right =
            (parentTextViewRect.left + endXCoordinatesOfClickedText - startXCoordinatesOfClickedText).roundToInt()

        var x = (parentTextViewRect.left + parentTextViewRect.right) / 2
        val y = parentTextViewRect.bottom
        if (keywordIsInMultiLine) {
            x = parentTextViewRect.left
        }

        clickX = x; clickY = y
        onClick(parentTextView, x, y)
    }

    abstract fun onClick(widget: TextView, x: Int, y: Int)

    override fun updateDrawState(ds: TextPaint) { // override updateDrawState
        ds.isUnderlineText = false
    }
}