package co.jp.smagroup.musahaf.utils


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import co.jp.smagroup.musahaf.R

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

    fun shareText(activity: Activity, text: String, @StringRes shareTitle: Int = R.string.share_ayah_text) {
        shareViaIntent(activity, text, shareTitle)
    }

    private fun shareViaIntent(activity: Activity, text: String, @StringRes titleResId: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        activity.startActivity(Intent.createChooser(intent, activity.getString(titleResId)))
    }

}