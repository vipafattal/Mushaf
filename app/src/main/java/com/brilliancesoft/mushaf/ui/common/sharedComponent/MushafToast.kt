package com.brilliancesoft.mushaf.ui.common.sharedComponent

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.brilliancesoft.mushaf.R
import kotlinx.android.synthetic.main.layout_toast.view.*


object MushafToast {

    @IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ToastDuration

        @JvmStatic
        fun make(context: Context, message: String, @ToastDuration showingDuration: Int) {
            val layout = View.inflate(context, R.layout.layout_toast, null)

            layout.toast_text.text = message
            val toast = Toast(context)
            with(toast) {
                view = layout
                duration = showingDuration
                show()
            }
        }

        @JvmStatic
        fun make(context: Context, @StringRes message: Int, @ToastDuration showingDuration: Int) {
            val layout = View.inflate(context, R.layout.layout_toast, null)

            layout.toast_text.text = context.getString(message)
            val toast = Toast(context)
            with(toast) {
                view = layout
                duration = showingDuration
                show()
            }
        }

        @JvmStatic
        fun makeShort(context: Context, message: String) {
            make(
                context,
                message,
                Toast.LENGTH_SHORT
            )
        }

        @JvmStatic
        fun makeLong(context: Context, message: String) {
            make(
                context,
                message,
                Toast.LENGTH_LONG
            )
        }

        @JvmStatic
        fun makeShort(context: Context, @StringRes message: Int) {
            make(
                context,
                message,
                Toast.LENGTH_SHORT
            )
        }

        @JvmStatic
        fun makeLong(context: Context, @StringRes message: Int) {
            make(
                context,
                message,
                Toast.LENGTH_LONG
            )
        }

}