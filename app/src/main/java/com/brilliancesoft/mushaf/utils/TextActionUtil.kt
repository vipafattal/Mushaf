package com.brilliancesoft.mushaf.utils


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.codebox.lib.android.resoures.Stringify

object TextActionUtil {

    fun copyToClipboard(activity: Activity, text: String) {
        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(activity.getString(R.string.app_name), text)
        cm.setPrimaryClip(clip)
        Toast.makeText(
            activity, activity.getString(R.string.ayah_copied_popup),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun shareText(context: Context, text: String, @StringRes shareTitle: Int) {
        shareViaIntent(context, text, shareTitle)
    }

    fun shareAya(context: Context, aya: Aya) {
        val text =
            "{${aya.text}} \npage:${aya.page} \nsurah:${aya.surah!!.name}  \nvia @${Stringify(
                R.string.app_name_google_play,
                context
            )}"
        shareViaIntent(context, text, R.string.share_ayah_text)
    }


    private fun shareViaIntent(context: Context, text: String, @StringRes titleResId: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, context.getString(titleResId)))
    }

}