package com.brilliancesoft.mushaf.ui.common.sharedComponent

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.actvity.newIntent

class ShortcutsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutIntent = intent
        val action = shortcutIntent?.action

        val intent = lastPageReadIntent()

        if (ACTION_JUMP_TO_LATEST == action) {
            intent.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                recordShortcutUsage(JUMP_TO_LATEST_SHORTCUT_NAME)
            }
        }

        finish()
        launchActivity<MainActivity>()
        startActivity(intent)
    }

    private fun lastPageReadIntent(): Intent {
        val bundle = Bundle()
        val lastPage = preferences.getInt(PreferencesConstants.LastSurahViewed, 0)
        bundle.putInt(ReadQuranActivity.START_AT_PAGE_KEY, lastPage + 1)
        ReadQuranActivity.startNewActivity(this, bundle)

        return intent
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun recordShortcutUsage(shortcut: String) {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager?.reportShortcutUsed(shortcut)
    }

    companion object {
        private const val ACTION_JUMP_TO_LATEST = "com.brilliancesoft.mushaf.ui.commen.last_page"
        private const val JUMP_TO_LATEST_SHORTCUT_NAME = "lastPage"
    }
}
