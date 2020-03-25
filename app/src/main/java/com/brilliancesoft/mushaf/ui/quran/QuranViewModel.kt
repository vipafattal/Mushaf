package com.brilliancesoft.mushaf.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import kotlinx.coroutines.*

class QuranViewModel(private val repository: Repository) : ViewModel() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var mainMushaf: MutableLiveData<List<Aya>>


    fun prepareData() {
        coroutineScope.launch {
            if (QuranDataList.isEmpty())
                withContext(Dispatchers.IO) { loadAyatData() }
            mainMushaf.postValue(QuranDataList)
        }
    }

    fun getMainMushaf(): LiveData<List<Aya>> {
        if (!::mainMushaf.isInitialized)
            mainMushaf = MutableLiveData()

        return mainMushaf
    }

    private suspend fun loadAyatData() {
        QuranDataList = repository.getMusahafAyat(MushafConstants.MainMushaf)
    }

    fun updateBookmarkStateInData(aya: Aya) {
        val index = QuranDataList.indexOfFirst { it.number == aya.number }

        val newDatList = QuranDataList.toMutableList()
        newDatList[index] = aya.copy(isBookmarked = !aya.isBookmarked)

        QuranDataList = newDatList
    }

    override fun onCleared() {
        super.onCleared()
        job.cancelChildren()
    }

    companion object {
        var QuranDataList = listOf<Aya>()
            private set

        fun isQuranDataLoaded() = QuranDataList.isNotEmpty()
    }
}

