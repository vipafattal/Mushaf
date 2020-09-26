package com.brilliancesoft.mushaf.ui.quran.read.reciter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brilliancesoft.mushaf.model.Surah

/**
 * Created by ${User} on ${Date}
 */
class ReciterViewModel : ViewModel() {

    private lateinit var surahLiveData: MutableLiveData<Pair<Int, Surah>>
    private lateinit var playFrom: MutableLiveData<Int>
    private lateinit var playTo: MutableLiveData<Int>
    private lateinit var repeatEachVerse: MutableLiveData<Int>
    private lateinit var repeatWholeSet: MutableLiveData<Int>


    init {
        if (!IsInitialized) {
            surahLiveData = MutableLiveData()
            playFrom = MutableLiveData()
            playTo = MutableLiveData()
            repeatEachVerse = MutableLiveData()
            repeatWholeSet = MutableLiveData()
            IsInitialized = true
        }
    }

    fun setSurah(startAt: Int, firstAyaNumber: Int, surah: Surah) {
        PlayingSurah = surah
        StartAtAya = startAt
        surahLiveData.value = startAt to surah
        playFrom.value = firstAyaNumber + startAt
        if (surah.numberOfAyahs == startAt)
            playTo.value = firstAyaNumber + startAt
        else
            playTo.value = firstAyaNumber + startAt + 1

        FirstAyaNumberInQuran = firstAyaNumber
        repeatEachVerse.value = 1
        repeatWholeSet.value = 1
    }

    fun getSurah(): LiveData<Pair<Int, Surah>> {
        surahLiveData.value = StartAtAya to PlayingSurah!!
        return surahLiveData
    }

    fun updatePlayFrom(startAt: Int) {
        StartAtAya = FirstAyaNumberInQuran + startAt
        playFrom.value = StartAtAya
    }

    fun updatePlayTo(endAt: Int) {
        EndAtAya = FirstAyaNumberInQuran + endAt
        playTo.value = EndAtAya
    }

    fun getPlayRange(): IntRange {

        playFrom.value = StartAtAya
        playTo.value = EndAtAya

        return playFrom.value!!..playTo.value!!
    }

    fun updateRepeatEachVerse(n: Int) {
        RepeatEachVerse = n
        repeatEachVerse.value = RepeatEachVerse
    }

    fun updateRepeatWholeSet(n: Int) {
        RepeatSet = n
        repeatWholeSet.value = RepeatSet
    }

    fun getRepeatEachVerse(): Int {
        repeatEachVerse.value = RepeatEachVerse
        return repeatEachVerse.value!!
    }
    fun getRepeatWholeSet(): Int {
        repeatWholeSet.value = RepeatSet
        return repeatWholeSet.value!!
    }

    override fun onCleared() {
        super.onCleared()
        IsInitialized = false
    }

    companion object {
        private var PlayingSurah: Surah? = null
        private var IsInitialized = false
        private var FirstAyaNumberInQuran = -1
        private var StartAtAya = -1
        private var EndAtAya = -1
        private var RepeatSet = -1
        private var RepeatEachVerse = -1

    }
}