package com.brilliancesoft.mushaf.ui.common.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.codebox.lib.standard.lambda.unitFun


class LoadingDialog : DialogFragment() {

    var doOnEnd: unitFun? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AppTheme_TransparentDialog)
        builder.setView(R.layout.dialog_loading)
        val alertDialog = builder.create()

        builder.setCancelable(false)
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)

        alertDialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                context?.let { MushafToast.makeShort(it, R.string.wait) }
                true // pretend we've processed it
            } else false // pass on to be processed as normal

        }

        return alertDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        doOnEnd?.invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        val baseActivity = (activity as BaseActivity)
        if (baseActivity.currentSystemVisibility)
            baseActivity.systemUiVisibility(true)
    }

    companion object {
        const val TAG = "Dialog-Loading"
    }
}