package com.brilliancesoft.mushaf.utils

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi

class TextSelectionCallback(
    private val data: Any,
    context: Context,
    @MenuRes private var menuResId: Int,
    private val mTextView: TextView,
    private val onActionItemClickListener: OnActionItemClickListener
) : ActionMode.Callback {
    companion object {
        const val Copy = android.R.id.copy
        const val Cut = android.R.id.cut
        @RequiresApi(Build.VERSION_CODES.M)
        const val Share = android.R.id.shareText
        const val SelectAll = android.R.id.selectAll
    }

    private val clipboard: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


    private var mode: ActionMode? = null
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        mode.menuInflater.inflate(menuResId, menu)
        //mode.title = title
        // mode.subtitle = subtitle
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        // menu.removeItem(android.R.id.selectAll);
        // Remove the "cut" option
        menu.removeItem(android.R.id.cut)
        // Remove the "copy all" option
        // menu.removeItem(android.R.id.copy);
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        this.mode = null
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        var start = mTextView.selectionStart
        var end = mTextView.selectionEnd

        if (end > mTextView.text.lastIndex) end = mTextView.selectionEnd - 1
        if (start < 0) start = 0

        val value = onActionItemClickListener.onActionItemClick(data, item, start..end, clipboard)
        if (value) mode.finish()
        return value
    }

    interface OnActionItemClickListener {
        fun onActionItemClick(
            data: Any,
            item: MenuItem,
            selectedRange: IntRange,
            clipboard: ClipboardManager
        ): Boolean
    }
/*    fun startActionMode(view: View,
                        @MenuRes menuResId: Int,
                        title: String? = null,
                        subtitle: String? = null) {
        this.menuResId = menuResId
        //this.title = title
        //this.subtitle = subtitle
        view.startActionMode(this)
    }*/
/*
    fun finishActionMode() {
        mode?.finish()
    }*/


}