package co.jp.smagroup.musahaf.ui.more

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.ui.MainActivity
import co.jp.smagroup.musahaf.ui.openSourceLicenses.OpenSourceLicenseActivity
import co.jp.smagroup.musahaf.utils.LocaleHelper
import co.jp.smagroup.musahaf.utils.extensions.goTo
import co.jp.smagroup.musahaf.utils.extensions.onPreferencesClick
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.utils.AppPreferences


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

        darkMode.isChecked = sharedPreference.getBoolean(SettingsPreferencesConstant.AppThemeKey)
        val currentLocal = sharedPreference.getStr(SettingsPreferencesConstant.AppLanguageKey)
        languageMode.isChecked = currentLocal == "ar"
        translationWithAya.isChecked =
            sharedPreference.getBoolean(SettingsPreferencesConstant.TranslationWithAyaKey, true)
        arabicNumbers.isChecked =
            sharedPreference.getBoolean(SettingsPreferencesConstant.ArabicNumbersKey)

        darkMode.onPreferenceChangeListener = this
        languageMode.onPreferenceChangeListener = this
        translationWithAya.onPreferenceChangeListener = this
        arabicNumbers.onPreferenceChangeListener = this


        onPreferencesClick(R.string.data_disclaimer) {
            context?.goTo("https://alquran.cloud/")
        }

        onPreferencesClick(R.string.audio_quality_options) {
            val audioQualityDialog = AudioQualityDialog()
            fragmentManager?.let { audioQualityDialog.show(it, AudioQualityDialog.TAG) }
        }
        onPreferencesClick(R.string.open_source_license) {
            context?.launchActivity<OpenSourceLicenseActivity>()
        }


    }

}