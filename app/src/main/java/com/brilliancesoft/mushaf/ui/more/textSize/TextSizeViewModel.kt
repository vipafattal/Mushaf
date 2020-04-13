package com.brilliancesoft.mushaf.ui.more.textSize

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextSizeViewModel :ViewModel() {
    val selectedTextSize:MutableLiveData<Int>
    init {
        selectedTextSize = MutableLiveData()
    }
}