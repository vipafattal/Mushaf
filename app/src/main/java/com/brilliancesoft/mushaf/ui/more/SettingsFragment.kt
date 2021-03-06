package com.brilliancesoft.mushaf.ui.more

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.MainActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.ui.more.textSize.TextSizeBottomSheet
import com.brilliancesoft.mushaf.ui.more.textSize.TextSizeViewModel
import com.brilliancesoft.mushaf.ui.openSourceLicenses.OpenSourceLicenseActivity
import com.brilliancesoft.mushaf.utils.LocaleHelper
import com.brilliancesoft.mushaf.utils.extensions.goTo
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.onPreferencesClick
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.utils.AppPreferences
import java.util.*


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private val sharedPreference = AppPreferences()

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        newValue as Boolean
        when (preference.key) {
            getString(R.string.dark_mode) -> {
                sharedPreference.put(SettingsPreferencesConstant.AppThemeKey, newValue)
                activity?.recreate()
            }

            getString(R.string.arabic_mode) -> {
                sharedPreference.put(
                    SettingsPreferencesConstant.AppLanguageKey,
                    if (newValue) "ar" else "en"
                )

                if (newValue == true) LocaleHelper.setAppLocale(activity!!, "ar")
                else LocaleHelper.setAppLocale(activity!!, "en")
                activity?.recreate()
            }

            getString(R.string.vertical_quran_page) ->
                sharedPreference.put(SettingsPreferencesConstant.VerticalQuranPageKey, newValue)

            getString(R.string.arabic_numbers) -> sharedPreference.put(
                SettingsPreferencesConstant.ArabicNumbersKey,
                newValue
            )
            getString(R.string.translation_with_aya) -> sharedPreference.put(
                SettingsPreferencesConstant.TranslationWithAyaKey,
                newValue
            )
        }
        return true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        (activity as MainActivity).updateToolbar(R.string.more)
        setPreferencesFromResource(R.xml.app_setting, rootKey)
        val darkMode = findPreference<CheckBoxPreference>(getString(R.string.dark_mode))!!
        val languageMode = findPreference<CheckBoxPreference>(getString(R.string.arabic_mode))!!
        val translationWithAya =
            findPreference<CheckBoxPreference>(getString(R.string.translation_with_aya))!!
        val arabicNumbers = findPreference<CheckBoxPreference>(getString(R.string.arabic_numbers))!!
        val verticalQuranPage =
            findPreference<CheckBoxPreference>(getString(R.string.vertical_quran_page))!!
        val textSize = findPreference<Preference>(getString(R.string.font_size_key))!!

        darkMode.isChecked = sharedPreference.getBoolean(SettingsPreferencesConstant.AppThemeKey)
        val currentLocal = sharedPreference.getStr(SettingsPreferencesConstant.AppLanguageKey)

        languageMode.isChecked = Locale.getDefault().language == "ar"

        translationWithAya.isChecked =
            sharedPreference.getBoolean(SettingsPreferencesConstant.TranslationWithAyaKey, true)
        arabicNumbers.isChecked =
            sharedPreference.getBoolean(SettingsPreferencesConstant.ArabicNumbersKey)

        verticalQuranPage.isChecked =
            sharedPreference.getBoolean(SettingsPreferencesConstant.VerticalQuranPageKey, false)

        textSize.summary = getString(UserPreferences.getFontSize())
        darkMode.onPreferenceChangeListener = this
        languageMode.onPreferenceChangeListener = this
        translationWithAya.onPreferenceChangeListener = this
        arabicNumbers.onPreferenceChangeListener = this
        verticalQuranPage.onPreferenceChangeListener = this

        onPreferencesClick(R.string.font_size_key){
            TextSizeBottomSheet.show(parentFragmentManager)
            viewModelOf<TextSizeViewModel>().selectedTextSize.observer(this){
                textSize.summary = getString(it)
            }
        }

        onPreferencesClick(R.string.data_disclaimer) {
            context?.goTo("https://alquran.cloud/")
        }

        onPreferencesClick(R.string.audio_quality_options) {
            val audioQualityDialog = AudioQualityDialog()
            audioQualityDialog.show(parentFragmentManager, AudioQualityDialog.TAG)
        }

        onPreferencesClick(R.string.open_source_license) {
            context?.launchActivity<OpenSourceLicenseActivity>()
        }
    }

    companion object {
        private const val TEXT_SIZE_DIALOG_DISMISS = 0
    }

}