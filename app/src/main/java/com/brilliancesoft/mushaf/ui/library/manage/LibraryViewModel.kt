package com.brilliancesoft.mushaf.ui.library.manage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.DownloadingState
import com.brilliancesoft.mushaf.model.Edition
import kotlinx.coroutines.*


class LibraryViewModel(private val repository: Repository) : ViewModel() {

    private lateinit var allAvailableEditions: MutableLiveData<List<Pair<Edition, DownloadingState>>>
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    fun getEditions(): LiveData<List<Pair<Edition, DownloadingState>>> {
        if (!::allAvailableEditions.isInitialized)
            allAvailableEditions = MutableLiveData()

            coroutineScope.launch {
               val editionsData = withContext(Dispatchers.IO) { repository.getAllEditionsWithState() }
                allAvailableEditions.postValue(editionsData)

        }
        return allAvailableEditions
    }

    fun updateDataDownloadState(editionIdentifier: String) {
        coroutineScope.launch {
           val editionsData = withContext(Dispatchers.IO) {
               allAvailableEditions.value?.map {
                    if (it.first.identifier == editionIdentifier)
                        it.first to repository.getDownloadState(editionIdentifier)
                    else it
                }
            }

            allAvailableEditions.postValue(editionsData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancelChildren()
    }

}


