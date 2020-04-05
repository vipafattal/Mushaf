package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.text.TextUtils
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.ReadData

class QuranPageTextFormatter(private val textAction: FunctionalQuranText) {

    fun format(
        rawData: List<Aya>,
        outputTo: MutableList<in ReadData>
    ) {
        var pageText: CharSequence = ""
        //This only true for page 48 of surah Al-Barqara.
        if (rawData.lastIndex == 0) {
            val aya = rawData[0]
            pageText = textAction.getQuranDecoratedText(aya.getFormattedAya(), 0, aya)
            outputTo.add(ReadData(aya, pageText, false))
        } else {
            rawData.forEachIndexed { index, aya ->
                val isLastAyaInPage = index == rawData.lastIndex

                if ((pageText.isNotEmpty())) {
                    aya.surah!!.englishName
                    val prvAya = rawData[index - 1]
                    if (isLastAyaInPage || prvAya.surah!!.englishName != aya.surah!!.englishName) {
                        val isNewSurah =
                            rawData.firstOrNull { it.numberInSurah == 1 && it.surah!!.englishName == prvAya.surah!!.englishName } != null

                        if (isLastAyaInPage) {
                            pageText = TextUtils.concat(
                                pageText,
                                textAction.getQuranDecoratedText(
                                    aya.getFormattedAya(),
                                    pageText.length,
                                    aya
                                )
                            )
                            outputTo.add(ReadData(prvAya, pageText, isNewSurah))
                            return@forEachIndexed
                        }
                        outputTo.add(ReadData(prvAya, pageText, isNewSurah))
                        pageText = ""
                    }
                }

                val decoratedText =
                    textAction.getQuranDecoratedText(aya.getFormattedAya(), pageText.length, aya)
                pageText = TextUtils.concat(pageText, decoratedText)

            }
        }
    }
}