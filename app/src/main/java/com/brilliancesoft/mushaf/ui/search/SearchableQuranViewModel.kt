package com.brilliancesoft.mushaf.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import kotlinx.coroutines.*

class SearchableQuranViewModel(private val repository: Repository) : ViewModel() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var _searchableMushaf: MutableLiveData<List<Aya>>


    fun getSearchableMushaf(): LiveData<List<Aya>> {
        if (!::_searchableMushaf.isInitialized)
            _searchableMushaf = MutableLiveData()

        coroutineScope.launch {
            val data =
                withContext(Dispatchers.IO) { repository.getMusahafAyat(MushafConstants.SimpleQuran) }
            _searchableMushaf.postValue(data)

        }

        return _searchableMushaf
    }

    override fun onCleared() {
        super.onCleared()
        job.cancelChildren()
    }
}

