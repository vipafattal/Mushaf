package com.brilliancesoft.mushaf.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import kotlinx.coroutines.launch

class QuranViewModel(private val repository: Repository) : ViewModel() {

    private lateinit var mainMushaf: MutableLiveData<List<Aya>>

    private val _bookmarkedAyaBookmarked: MutableLiveData<Boolean> = MutableLiveData()
    val bookmarkedAyaBookmarked: LiveData<Boolean>
        get() = _bookmarkedAyaBookmarked

    fun getMainMushaf(): LiveData<List<Aya>> {
        if (!::mainMushaf.isInitialized)
            mainMushaf = MutableLiveData()

            viewModelScope.launch {
                if (MainQuranList.isEmpty())
                MainQuranList = repository.getMusahafAyat(MushafConstants.MainMushaf)
                mainMushaf.postValue(MainQuranList)
            }

        return mainMushaf
    }


    fun updateBookmarkStateInData(aya: Aya) {
        val index = MainQuranList.indexOfFirst { it.number == aya.number }
        val newDatList = MainQuranList.toMutableList()
        val newAya = aya.copy(isBookmarked = !aya.isBookmarked)
        newDatList[index] = newAya
        if (newAya.isBookmarked)
            _bookmarkedAyaBookmarked.postValue(true)
        MainQuranList = newDatList
    }

    companion object {
        var MainQuranList = listOf<Aya>()
            private set

        fun isQuranDataLoaded() = MainQuranList.isNotEmpty()
    }
}

