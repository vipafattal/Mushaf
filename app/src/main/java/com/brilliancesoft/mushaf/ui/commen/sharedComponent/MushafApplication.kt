package com.brilliancesoft.mushaf.ui.commen.sharedComponent

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.di.AppComponent
import com.brilliancesoft.mushaf.framework.di.DaggerAppComponent
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.utils.LocaleHelper
import com.codebox.lib.android.os.MagentaX
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.standard.delegation.DelegatesExt
import com.tonyodev.fetch2.FetchConfiguration
import java.util.*


/**
 * Created by ${User} on ${Date}
 */
class MushafApplication : MultiDexApplication() {
    companion object {
        var appComponent: AppComponent by DelegatesExt.notNullSingleValue()
            private set

        var appContext: MushafApplication by DelegatesExt.notNullSingleValue()
            private set

        private lateinit var sharedPrefs: AppPreferences


        val isDarkThemeEnabled
            get() = sharedPrefs.getBoolean(SettingsPreferencesConstant.AppThemeKey)

    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        initDagger()
        sharedPrefs = AppPreferences()
        MagentaX.init(this)

        val currentLanguage = sharedPrefs.getStr(SettingsPreferencesConstant.AppLanguageKey, "")
        //If no language set in Settingsfragment, this will saveDefaultAppLocal().
        if (currentLanguage == "") {
            saveDefaultAppLocal(this)
        }
    }


    private fun saveDefaultAppLocal(context: Context) {

        val systemLanguage = Locale.getDefault().language
        //System Language is Arabic save that so we show Arabic mode is activated in @SettingsFragment.
        if (systemLanguage == "ar") sharedPrefs.put(SettingsPreferencesConstant.AppLanguageKey, "ar")
        //If system local is not english then forcing the app to English language.
        else if (systemLanguage != "en") LocaleHelper.setAppLocale(context, "en")


        /*  else
              setAppLocale(context, "en")*/
    }


    fun getAppTheme(): Int =
        if (isDarkThemeEnabled) R.style.AppTheme_Dark
        else R.style.AppTheme


    private fun initDagger() {
        appComponent = DaggerAppComponent.builder().build()
    }

    fun fetchConfiguration(): FetchConfiguration =
        FetchConfiguration.Builder(this)
            .enableLogging(true)
            .setDownloadConcurrentLimit(2)
            .setAutoRetryMaxAttempts(3)
            .build()



    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}