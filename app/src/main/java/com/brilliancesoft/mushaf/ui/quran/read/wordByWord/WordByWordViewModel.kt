package com.brilliancesoft.mushaf.ui.quran.read.wordByWord

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by ${User} on ${Date}
 */
class WordByWordViewModel : ViewModel() {
    @Suppress("JoinDeclarationAndAssignment")
    private var wordByWordData: MutableLiveData<List<Pair<String?, String>>>
    
    init {
        wordByWordData = MutableLiveData()
    }
    
    fun setWordByWordData(wordsByWords: List<Pair<String?, String>>) {
        wordByWordData.value = wordsByWords
    }
    
    fun getWordByWord(): LiveData<List<Pair<String?, String>>> = wordByWordData
    
}