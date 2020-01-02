package co.jp.smagroup.musahaf.ui.quran.read.wordByWord

import co.jp.smagroup.musahaf.framework.commen.MushafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.utils.isPunctuation
import co.jp.smagroup.musahaf.utils.removePunctuation
import com.codebox.lib.standard.stringsUtils.match
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by ${User} on ${Date}
 */
class WordByWordLoader(val repository: Repository) {

    fun getDataWordByWord(
        coroutineScope: CoroutineScope,
        selectedData: ArrayList<String>,
        ayaFromSelectedText: Aya,
        onSuccess: (List<Pair<String?, String>>) -> Unit,
        doOnEmptyData: () -> Unit
    ) {
        val ayaResult = mutableListOf<Pair<String?, String>>()

        coroutineScope.launch {
            val wordByWordPageData =
                withContext(Dispatchers.IO) {
                    repository.getPage(
                        MushafConstants.WordByWord,
                        ayaFromSelectedText.page
                    )
                }

            if (wordByWordPageData != null) {
                for (arabicWord in selectedData) {
                    val surahEnglishName = ayaFromSelectedText.surah!!.englishName
                    var definition: String? =
                        getWordDefinitionMethod1(wordByWordPageData, arabicWord, surahEnglishName)
                    if (definition != null) ayaResult.add(definition to arabicWord)
                    else {
                        definition = getWordDefinitionMedthod2(
                            wordByWordPageData,
                            arabicWord,
                            surahEnglishName
                        )
                        ayaResult.add(definition to arabicWord)
                    }
                }
                onSuccess(ayaResult)
            } else
                doOnEmptyData()
        }
    }

    private fun getWordDefinitionMethod1(
        wordByWordPageData: List<Aya>,
        arabicWord: String,
        surahEnglishName: String
    ): String? {
        var wordDefinition: String? = null
        val aya = wordByWordPageData.firstOrNull {
            val wordByWordAya = it.text.removePunctuation()
            val arabicNoPunctuation = arabicWord.removePunctuation()
            val arabicWithHamza = arabicWord.removePunctuation("ٲ")
            val arabicWithAlef = arabicWord.removePunctuation("ا")

            ((wordByWordAya.match(arabicNoPunctuation) ||
                    wordByWordAya.match(arabicWithHamza) ||
                    wordByWordAya.match(arabicWithAlef)) && it.surah!!.englishName == surahEnglishName)

        }
        if (aya != null) wordDefinition = getWordDefinition(aya, arabicWord)

        return wordDefinition
    }

    private fun getWordDefinitionMedthod2(
        wordByWordPageData: List<Aya>,
        arabicWord: String,
        surahEnglishName: String
    ): String? {
        var wordDefinition: String? = null

        val aya = wordByWordPageData.firstOrNull {
            val wordByWordAya = it.text.removePunctuation()
            val arabicNoPunctuation = arabicWord.removePunctuation()
            val arabicWithHamza = arabicWord.removePunctuation("ٲ")
            val arabicWithAlef = arabicWord.removePunctuation("ا")

            ((wordByWordAya.find(arabicNoPunctuation) ||
                    wordByWordAya.find(arabicWithHamza) ||
                    wordByWordAya.find(arabicWithAlef)) && it.surah!!.englishName == surahEnglishName)
        }

        if (aya != null) wordDefinition = getWordDefinition(aya, arabicWord)

        return wordDefinition
    }

    private fun getWordDefinition(
        resultAya: Aya,
        arabicWord: String
    ): String? {
        var definition: String? = null
        var wordDict = ""
        val arabicWordNoPunctuation = arabicWord.removePunctuation()
        val arabicWordWithAlef = arabicWord.removePunctuation("ٲ")
        val correctedText = resultAya.text.replace("ٮ", "ى")
        for (index in 0 until correctedText.length) {
            val currentChar = correctedText[index]
            if (currentChar in 'A'..'Z' || currentChar in 'a'..'z')
                continue
            if (currentChar != '|' && currentChar != '$' && currentChar != ' ' && !currentChar.isDigit()) {
                if (!currentChar.isPunctuation)
                    wordDict += currentChar
            }
            if (wordDict == arabicWordNoPunctuation || wordDict == arabicWordWithAlef) {
                val rawData = correctedText.substring(index, correctedText.length - 1)
                val englishWordAya = rawData.split('|', limit = 3)
                definition = englishWordAya[1]
                break
            }
            if (currentChar == '|') {
                wordDict = ""
            }
        }

        return definition
    }

    private fun String.find(query: String) = matches("$query(.*)".toRegex())

}