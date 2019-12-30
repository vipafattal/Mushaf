package co.jp.smagroup.musahaf.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import kotlinx.coroutines.*

class QuranViewModel(private val repository: Repository) : ViewModel() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var _mainMushaf: MutableLiveData<List<Aya>>
    val mainMusahaf: LiveData<List<Aya>>
        get() = _getMainMushaf()

    fun prepareMainMushaf() {
        if (!::_mainMushaf.isInitialized)
            _mainMushaf = MutableLiveData()
        coroutineScope.launch {
            if (QuranDataList.isEmpty())
                withContext(Dispatchers.IO) { loadAyatData() }
            _mainMushaf.postValue(QuranDataList)
        }
    }


    private suspend fun loadAyatData() {
        QuranDataList = repository.getMusahafAyat(MusahafConstants.MainMusahaf)
    }

    fun updateBookmarkStateInData(aya: Aya) {
        val index = QuranDataList.indexOf(aya)

        val newDatList = QuranDataList.toMutableList()
        newDatList[index] =  aya.copy(isBookmarked = !aya.isBookmarked)

        QuranDataList = newDatList
    }

    @Suppress("FunctionName")
    private fun _getMainMushaf(): LiveData<List<Aya>> {
        if (!::_mainMushaf.isInitialized)
            _mainMushaf = MutableLiveData()

        return _mainMushaf
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

