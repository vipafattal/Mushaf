package com.brilliancesoft.mushaf.ui.common.sharedComponent

import android.content.Context
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant.AppThemeKey
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant.SelectedTextSizeKey
import com.brilliancesoft.mushaf.utils.LocaleHelper
import com.codebox.lib.android.utils.AppPreferences
import java.util.*

object UserPreferences {

    private lateinit var appPreferences:AppPreferences

    fun init(context: Context,preferences: AppPreferences) {
        appPreferences = preferences
        val currentLanguage = preferences.getStr(SettingsPreferencesConstant.AppLanguageKey, "")
        //If this first time user launch the app, we will save system app local.
        if (currentLanguage == "")
            saveDefaultAppLocal(context)

        if (getFontSize() == R.string.medium)
            saveFontSize(R.string.medium_font_size)
    }

    val isDarkThemeEnabled
        get() = appPreferences.getBoolean(AppThemeKey)

    fun getAppTheme(): Int =
        if (isDarkThemeEnabled) R.style.AppTheme_Dark
        else R.style.AppTheme

    fun getFontSize(): Int =
        appPreferences.getInt(SelectedTextSizeKey ,R.string.medium_font_size)

    fun saveFontSize(sizeName:Int) {
        appPreferences.put(SelectedTextSizeKey , sizeName)
    }


    private fun saveDefaultAppLocal(context: Context) {
        val systemLanguage = Locale.getDefault().language
        //System Language is Arabic save that so we show Arabic mode is activated in @SettingsFragment.
        if (systemLanguage == "ar") appPreferences.put(SettingsPreferencesConstant.AppLanguageKey, "ar")
        //If system local is not english then forcing the app to English language.
        else if (systemLanguage != "en") LocaleHelper.setAppLocale(context, "en")
    }

}