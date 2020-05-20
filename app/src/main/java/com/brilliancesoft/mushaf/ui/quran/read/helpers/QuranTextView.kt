package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.utils.CustomClickableSpan
import com.brilliancesoft.mushaf.utils.TextSelectionCallback
import com.brilliancesoft.mushaf.utils.extensions.toSpannable
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.os.appHandler
import com.codebox.lib.android.os.wait


class QuranTextView : AppCompatTextView {

    //Clickable span deliver click event to this Listener.
    private val touchEvent = object : LinkMovementMethod() {
        override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
            Selection.removeSelection(buffer)
            return super.onTouchEvent(widget, buffer, event)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun callClickOnSpan(aya: Aya) {
        val start = text.indexOf(aya.numberInSurah.toLocalizedNumber()) - 1
        val end = start + 1

        val customClickableSpans: Array<out CustomClickableSpan> =
            text.toSpannable().getSpans(start, end, CustomClickableSpan::class.java)

        if (customClickableSpans.isEmpty())
            throw IllegalArgumentException("the given range(start..end) doesn't contain any clickable span")

        val clickableSpan = customClickableSpans[0]
        clickableSpan.onClick(this)

        var v: View? = parent.parent as? View
        while (v != null && v !is RecyclerView) {
            v = v.parent as? View
        }

        (v as RecyclerView).scrollY = clickableSpan.clickY
        appHandler.wait(200) {
            clickableSpan.onClick(this)
        }
    }


    fun init() {
    }


    fun selectionTextCallBack(
        data: Any,
        onActionItemClickListener: TextSelectionCallback.OnActionItemClickListener,
        @MenuRes menu: Int = R.menu.menu_selection_options
    ) {
        /*
            Warning priority must be kept so we don't face in selection error on Api 21:
              1.Setting TextSelectionCallback.
              2.Setting movementMethod.
         */
        customSelectionActionModeCallback = TextSelectionCallback(
            data,
            context,
            menu,
            this,
            onActionItemClickListener
        )
        movementMethod = touchEvent
    }

    //Fixes some text selection issue on some Devices.
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            val text = text
            setText(null)
            setText(text)
        }
        return super.dispatchTouchEvent(event)
    }
}