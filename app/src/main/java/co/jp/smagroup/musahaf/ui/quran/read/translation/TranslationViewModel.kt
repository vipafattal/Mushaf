package co.jp.smagroup.musahaf.ui.quran.read.translation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.model.Translation

/**
 * Created by ${User} on ${Date}
 */
class TranslationViewModel : ViewModel() {
    private lateinit var translations: MutableLiveData<Translation>
    fun setTranslationData(
        selected: List<Edition>,
        unSelected: List<Edition>,
        numberInMusahaf: Int
    ) {

        if (!::translations.isInitialized)
            translations = MutableLiveData()

        translations.value = Translation(numberInMusahaf, selected, unSelected)
    }

    fun getTranslation(): LiveData<Translation> = translations


}