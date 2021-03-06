package com.brilliancesoft.mushaf.ui.common.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.utils.extensions.updatePadding
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.standard.lambda.unitFun

class ConformDialog : DialogFragment() {
    var onConfirm: unitFun? = null

    companion object {
        const val TAG: String = "Conform-Dialog"

        fun show(titleDialog: Int, fragmentManager: FragmentManager): ConformDialog {
            val conformDialog = ConformDialog()
            conformDialog.title = titleDialog
            conformDialog.show(fragmentManager, TAG)
            return conformDialog
        }
    }

    @StringRes
    private var title: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return AlertDialog.Builder(requireContext()).apply {

            setCustomTitle(TextView(context).apply {
                text = getString(title)
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
                textSize = 21f
                updatePadding(
                    dp(13),
                    dp(13),
                    dp(13),
                    dp(10)
                )
            })
            setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                onConfirm?.invoke()
            }.setNegativeButton(R.string.no) { _: DialogInterface?, _: Int ->
                dismiss()
            }
        }.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        val baseActivity = (activity as BaseActivity)
        if (baseActivity.currentSystemVisibility)
            baseActivity.systemUiVisibility(true)
    }

}
