package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.content.Context
import android.util.Log
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.utils.quranicSymbol

fun textToWords(text: CharSequence): List<String> {
    val wordToTranslate = arrayListOf<String>()
    var word = ""
    var isCharAdded = false
    //get words before
    val textIndices = text.indices
    for (index in textIndices) {
        val currentChar = text[index]
        if (currentChar != '*' && currentChar != ' ' && !currentChar.quranicSymbol) {
            word += currentChar
            isCharAdded = true
        }
        if ((currentChar == ' ' || index == textIndices.last) && isCharAdded) {
            isCharAdded = false
            if (word.length > 1)
                wordToTranslate.add(word)
            word = ""
        }
    }
    Log.d("Word by word", wordToTranslate.toString())
    return wordToTranslate
}

fun selectedTextToOutput(context: Context, selectedText: String, aya: Aya): String =
    context.run {
        val page = "${getString(R.string.page)}: ${aya.page}"

        val surah =
            "${getString(R.string.surah)}: ${aya.surah!!.name}"

        val fullText =
            "{$selectedText} \n$page \n$surah \nvia @${getString(R.string.app_name_google_play)}"

        return@run fullText
    }
