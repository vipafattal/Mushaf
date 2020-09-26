package com.brilliancesoft.mushaf.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.codebox.lib.android.utils.AppPreferences
import java.util.*


object LocaleHelper {

    fun onAttach(context: Context): Context {
        val lang = getPersistedLanguage()
        return setAppLocale(context, lang)
    }


    fun setAppLocale(context: Context, language: String): Context {
        persist(language)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)

    }

    private fun getPersistedLanguage(): String {
        val preferences = AppPreferences.getInstance("language")
        return preferences.getStr(
            SettingsPreferencesConstant.AppLanguageKey,
            Locale.getDefault().language
        )
    }

    private fun persist(language: String) {
        val preferences = AppPreferences.getInstance("language")
        preferences.put(SettingsPreferencesConstant.AppLanguageKey, language)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }

    fun updateConfiguration(configuration: Configuration) {

        val lang = getPersistedLanguage()
        val locale = Locale(lang)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
        }
    }

}