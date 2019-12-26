package co.jp.smagroup.musahaf.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.CacheManager
import com.codebox.lib.standard.collections.filters.singleIdx
import kotlinx.serialization.list
import kotlinx.coroutines.*
import kotlinx.serialization.UnstableDefault

class QuranViewModel(private val repository: Repository) : ViewModel() {

    @UnstableDefault
    private val cacheMaker = CacheManager()
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var _mainMusahaf: MutableLiveData<List<Aya>>
    val mainMusahaf: LiveData<List<Aya>>
        get() = _getMainMusahaf()

    fun prepareMainMusahaf() {
        if (!::_mainMusahaf.isInitialized)
            _mainMusahaf = MutableLiveData()
        coroutineScope.launch {
            if (QuranDataList.isEmpty())
                withContext(Dispatchers.IO) { loadAyatData() }
            _mainMusahaf.postValue(QuranDataList)
        }
    }


    @UseExperimental(UnstableDefault::class)
    private suspend fun loadAyatData() {
        val cachedData = cacheMaker.getSavedList(MusahafConstants.MainMusahaf, Aya.serializer().list)
        if (cachedData != null) QuranDataList = cachedData
        else {
            QuranDataList = repository.getMusahafAyat(MusahafConstants.MainMusahaf)
            saveAyatArrayToCache()
        }
    }

    @UseExperimental(UnstableDefault::class)
    private suspend fun saveAyatArrayToCache() {
        cacheMaker.saveList(MusahafConstants.MainMusahaf, Aya.serializer().list, QuranDataList)
    }

    fun updateBookmarkStateInData(aya: Aya) {
        val (oldAya,index) = QuranDataList.singleIdx { it.number == aya.number }
        oldAya.isBookmarked = !aya.isBookmarked
        val newDatList = QuranDataList.toMutableList()
        newDatList[index] = aya
        QuranDataList = newDatList
        coroutineScope.launch { saveAyatArrayToCache() }
    }

    @Suppress("FunctionName")
    private fun _getMainMusahaf(): LiveData<List<Aya>> {
        if (!::_mainMusahaf.isInitialized)
            _mainMusahaf = MutableLiveData()

        return _mainMusahaf
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

