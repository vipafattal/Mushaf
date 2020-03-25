package com.brilliancesoft.mushaf.utils.extensions

import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.codebox.lib.standard.lambda.unitFun

inline fun PreferenceFragmentCompat.onPreferencesClick(
    @StringRes key: Int,
    crossinline doOnClick: unitFun
) {
    findPreference<Preference>(getString(key))!!.setOnPreferenceClickListener {
        doOnClick()
        true
    }
}