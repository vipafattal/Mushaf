package co.jp.smagroup.musahaf.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.goTo(url: String) {
        val uriUrl = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }