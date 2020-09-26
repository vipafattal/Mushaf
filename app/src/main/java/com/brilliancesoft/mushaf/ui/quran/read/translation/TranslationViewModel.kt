package com.brilliancesoft.mushaf.ui.quran.read.translation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.Translation
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.utils.extensions.addIfNotNull
import com.codebox.lib.extrenalLib.TinyDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by ${User} on ${Date}
 */
class TranslationViewModel(
    private val repository: Repository,
    private val tinyDB: TinyDB

) : ViewModel() {

    private lateinit var _translations: MutableLiveData<Translation>
    val translations: LiveData<Translation>
        get() = _translations

    fun setAyaNumber(numberInMusahaf: Int) {
        if (!::_translations.isInitialized)
            _translations = MutableLiveData()

        viewModelScope.launch {
            _translations.value = getTranslationOfAya(numberInMusahaf)
        }
    }

    private suspend fun getTranslationOfAya(numberInMusahaf: Int): Translation {

        val downloadedEditions = withContext(Dispatchers.IO) {
            repository.getDownloadedTafseer(MushafConstants.Text)
        }.filter { it.identifier != MushafConstants.SimpleQuran }.toMutableList()

        val userSelectedTranslation = tinyDB.getListString(PreferencesConstants.LastUsedTranslation)

        val selectedEditions: MutableList<Edition> = mutableListOf()
        val unSelectedEditions: MutableList<Edition>

        if (userSelectedTranslation.isNotEmpty()) {
            for (identifier in userSelectedTranslation) {
                val selected = downloadedEditions.firstOrNull { it.identifier == identifier }
                selectedEditions.addIfNotNull(selected)
            }
            downloadedEditions.removeAll(selectedEditions)
            unSelectedEditions = downloadedEditions
        } else
            unSelectedEditions = downloadedEditions.toMutableList()

        return Translation(numberInMusahaf, selectedEditions, unSelectedEditions)
    }


}