package com.brilliancesoft.mushaf.ui.quran.read.reciter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.brilliancesoft.mushaf.utils.toArabicReciterName
import com.brilliancesoft.mushaf.utils.toEnglishReciterName
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.views.listeners.onClick
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_reciter_playing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */
class ReciterBottomSheet : BottomSheetDialogFragment(), AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var repository: Repository
    private val reciters = mutableListOf<Edition>()
    private lateinit var viewModel: ReciterViewModel
    private var numberOfAyaInSurah = 0
    private var selectedReciterEdition = Edition("", "", "", "", "", "")
    private val sharedPrefs = AppPreferences()
    private var isStreamingOnline = true
    private lateinit var parentActivity: ReadQuranActivity
    var isComingFromMediaPlayer = false

    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelOf<ReciterViewModel>()

        parentActivity = activity as ReadQuranActivity
        parentActivity.coroutineScope.launch {
            var data = repository.getAvailableReciters()
            if (isRightToLeft != 1) data =
                data.map { it.copy(name = it.identifier.toArabicReciterName(it.name)) }

            reciters.addAll(data)
            reciterNameSpinner.init(reciters.map(Edition::name).toTypedArray())
            if (data.isNotEmpty()) {
                val lastSelectedReciterId =
                    sharedPrefs.getStr(PreferencesConstants.LastChosenReciter, data[0].identifier)
                val reciter = data.first { it.identifier == lastSelectedReciterId }
                reciterNameSpinner.setSelection(data.indexOf(reciter))
                selectedReciterEdition = reciter
            }
        }

        viewModel.getSurah().observer(viewLifecycleOwner) { (startAt, surah) ->
            numberOfAyaInSurah = surah.numberOfAyahs
            var i = 1
            val pointArray = Array(surah.numberOfAyahs) { i++ }

            startPointSpinner.init(pointArray)
            endPointSpinner.init(pointArray)
            if (!isComingFromMediaPlayer) {
                startPointSpinner.setSelection(pointArray.indexOf(startAt))
                endPointSpinner.setSelection(
                    if (startAt == numberOfAyaInSurah) pointArray.indexOf(startAt) else pointArray.indexOf(
                        startAt + 1
                    )
                )
            } else {
                startPointSpinner.setSelection(pointArray.indexOf(previousStartAyaNumber))
                endPointSpinner.setSelection(pointArray.indexOf(previousEndAyaNumber))
            }

        }
        val repeatTimesArray = arrayOf(getString(R.string.no_repeating), "2", "3", "4")
        repeatWholeSetSpinner.init(repeatTimesArray)
        repeatEachVerseSpinner.init(repeatTimesArray)

        //Selecting last repeat times if user clicks on setting buttons previously.
        if (viewModel.getRepeatEachVerse() > 1)
            repeatEachVerseSpinner.setSelection(
                repeatTimesArray.indexOf(
                    viewModel.getRepeatEachVerse().toString()
                )
            )
        if (viewModel.getRepeatWholeSet() > 1)
            repeatWholeSetSpinner.setSelection(
                repeatTimesArray.indexOf(
                    viewModel.getRepeatWholeSet().toString()
                )
            )


        play_button.onClick {
            play()
        }
        isStreamingOnline =
            sharedPrefs.getBoolean(PreferencesConstants.LastPlayOfflineOrOnline, true)
        saveFiles.isChecked = isStreamingOnline

        saveFiles.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.put(PreferencesConstants.LastPlayOfflineOrOnline, isChecked)
            isStreamingOnline = isChecked
        }

        play_surah_to_end_checkbox.isChecked = false
    }


    fun <T> Spinner.init(items: Array<T>) {
        context?.let { mContext ->
            val spinnerArrayAdapter =
                ArrayAdapter(mContext, android.R.layout.simple_spinner_item, items)
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = spinnerArrayAdapter
            onItemSelectedListener = this@ReciterBottomSheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_reciter_playing, container, false)

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        (parent?.selectedView as? TextView)?.let { selectedTextView ->

            val textSelected = selectedTextView.text.toString()
            when (parent.id) {
                R.id.startPointSpinner -> {
                    val selectedAya = textSelected.toInt()
                    viewModel.updatePlayFrom(selectedAya)
                    updateEndPointSpinner(selectedAya)
                    previousStartAyaNumber = selectedAya

                }
                R.id.endPointSpinner -> {
                    val selectedAya = textSelected.toInt()
                    viewModel.updatePlayTo(selectedAya)
                    updateStartPointSpinner(selectedAya)
                    previousEndAyaNumber = selectedAya
                }

                R.id.reciterNameSpinner -> {
                    val reciterEdition = reciters.first { it.name == textSelected }
                    sharedPrefs.put(
                        PreferencesConstants.LastChosenReciter,
                        reciterEdition.identifier
                    )
                    selectedReciterEdition = reciterEdition
                }
                R.id.repeatEachVerseSpinner -> viewModel.updateRepeatEachVerse(if (textSelected.length >= 2) 1 else textSelected.toInt())
                R.id.repeatWholeSetSpinner -> viewModel.updateRepeatWholeSet(if (textSelected.length >= 2) 1 else textSelected.toInt())
            }
        }
    }

    private fun updateEndPointSpinner(startPoint: Int) {
        val firstItem = endPointSpinner.selectedView as TextView
        val playTo = firstItem.text.toString().toInt()

        if (startPoint >= playTo) {
            if (startPoint == numberOfAyaInSurah)
                endPointSpinner.setSelection(startPoint - 1)
            else
                endPointSpinner.setSelection(startPoint)
        }
    }

    private fun updateStartPointSpinner(endPoint: Int) {
        val firstItem = startPointSpinner.selectedView as TextView
        val playFrom = firstItem.text.toString().toInt()

        if (endPoint <= playFrom) {
            if (endPoint == 1)
                startPointSpinner.setSelection(0)
            else
                startPointSpinner.setSelection(endPoint - 1)
        }
    }


    private fun play() {
        parentActivity.coroutineScope.launch {
            if (endPointSpinner.isEnabled)
                viewModel.updatePlayTo(numberOfAyaInSurah)

            val playRange = viewModel.getPlayRange()

            val ayat = withContext(Dispatchers.IO) {
                repository.getAyatByRange(
                    playRange.first,
                    playRange.last
                )
            }
            if (UserPreferences.isArabicLocal)
                selectedReciterEdition = selectedReciterEdition.copy(
                    name = selectedReciterEdition.name.toEnglishReciterName(),
                    englishName = ""
                )

            val reciterPlayer = ReciterPlayer(
                ayat,
                parentActivity,
                repository.localRepositoryMetadata,
                selectedReciterEdition,
                viewModel.getRepeatEachVerse(),
                viewModel.getRepeatWholeSet()
            )
            //Releasing previous exo player.
            // if (parentActivity.playerView.isShown)
            //   parentActivity.releasePlayer()

            reciterPlayer.play(isStreamingOnline, playRange)
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as BaseActivity).systemUiVisibility(true)
    }

    companion object {
        const val TAG = "ReciterBottomSheet"
        private var previousStartAyaNumber = 0
        private var previousEndAyaNumber = 0
    }
}
