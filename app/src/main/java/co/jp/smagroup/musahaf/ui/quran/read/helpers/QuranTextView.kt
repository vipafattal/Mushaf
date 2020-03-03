package co.jp.smagroup.musahaf.ui.quran.read.helpers

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.AppCompatTextView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.utils.TextSelectionCallback


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