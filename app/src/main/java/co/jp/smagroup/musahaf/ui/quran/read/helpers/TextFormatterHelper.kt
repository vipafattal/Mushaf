package co.jp.smagroup.musahaf.ui.quran.read.helpers

import android.content.Context
import android.util.Log
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.utils.quranSpecialSimple

fun textToWords(text: CharSequence): ArrayList<String> {
    val wordToTranslate = arrayListOf<String>()
    var word = ""
    var isCharAdded = false
    //get words before
    val textIndices = text.indices
    for (index in textIndices) {
        val currentChar = text[index]
        if (currentChar != '*' && currentChar != ' ' && !currentChar.quranSpecialSimple) {
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
            "{$selectedText} \n$page \n$surah \nvia @${getString(R.string.app_name)} for Android"

        return@run fullText
    }
