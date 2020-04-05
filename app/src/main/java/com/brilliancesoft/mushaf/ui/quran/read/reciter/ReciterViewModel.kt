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
    private lateinit var repeatWholeSet : MutableLiveData<Int>
    private var firstAyaNumberInQuran = 0

    fun setSurah(startAt: Int, firstAyaNumber: Int, surah: Surah) {
        if (!::surahLiveData.isInitialized) {
            surahLiveData = MutableLiveData()
            playFrom = MutableLiveData()
            playTo = MutableLiveData()
            repeatEachVerse = MutableLiveData()
            repeatWholeSet = MutableLiveData()
        }
        surahLiveData.value = startAt to surah
        playFrom.value = firstAyaNumber + startAt
        if (surah.numberOfAyahs == startAt)
            playTo.value = firstAyaNumber + startAt
        else
            playTo.value = firstAyaNumber + startAt + 1

        firstAyaNumberInQuran = firstAyaNumber
        repeatEachVerse.value = 1
        repeatWholeSet.value = 1
    }

    fun getSurah(): LiveData<Pair<Int, Surah>> = surahLiveData

    fun updatePlayFrom(startAt: Int) {
        playFrom.value = firstAyaNumberInQuran + startAt
    }

    fun updatePlayTo(endAt: Int) {
        playTo.value = firstAyaNumberInQuran + endAt
    }

    fun getPlayRange() = playFrom.value!!..playTo.value!!

    fun updateRepeatEachVerse(n:Int) {
        repeatEachVerse.value = n
    }
    fun updateRepeatWholeSet(n:Int) {
        repeatWholeSet.value = n
    }

    fun getRepeatEachVerse() = repeatEachVerse.value!!
    fun getRepeatWholeSet() = repeatWholeSet.value!!
}