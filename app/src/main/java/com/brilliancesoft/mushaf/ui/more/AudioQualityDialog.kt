package com.brilliancesoft.mushaf.ui.more

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.brilliancesoft.mushaf.R
import com.codebox.lib.android.utils.AppPreferences
import kotlinx.android.synthetic.main.dialog_audio_quality.*

/**
 * Created by ${User} on ${Date}
 */
class AudioQualityDialog : DialogFragment() {

    private val preferences = AppPreferences()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window!!.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
        when (preferences.getInt(SettingsPreferencesConstant.AudioQualityKey, 1)) {
            0 -> low_audio_quality.isChecked = true
            1 -> medium_audio_quality.isChecked = true
            else -> high_audio_quality.isChecked = true
        }

        audioQualityGroup.setOnCheckedChangeListener { _, checkedId ->
            val qualityNumber: Int = when (checkedId) {
                R.id.low_audio_quality -> 0
                R.id.medium_audio_quality -> 1
                else -> 2
            }
            preferences.put(SettingsPreferencesConstant.AudioQualityKey, qualityNumber)
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_audio_quality, container, false)

    companion object {
        const val TAG = "Dialog-Audio-Quality"
    }
}