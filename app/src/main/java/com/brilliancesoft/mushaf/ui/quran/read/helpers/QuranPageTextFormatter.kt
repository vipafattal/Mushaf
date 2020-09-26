package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.text.TextUtils
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.QuranFormattedPage

class QuranPageTextFormatter(private val textAction: QuranicSpanText) {

    fun format(rawData: List<Aya>): MutableList<QuranFormattedPage> {
        val outputTos = mutableListOf<QuranFormattedPage>()

        var pageText: CharSequence = ""
        //This only true for page 48 of surah Al-Barqara.
        if (rawData.lastIndex == 0) {
            val aya = rawData[0].copy(text = rawData[0].text.replace("\n" , ""))
            pageText = textAction.applyQuranSpans(
                aya.getFormattedAya(),
                0,
                aya
            )
            outputTos.add(QuranFormattedPage(aya, pageText, false))
        } else {
            rawData.forEachIndexed { index, rawAya ->
                val isLastAyaInPage = index == rawData.lastIndex
                val aya = rawAya.copy(text = rawAya.text.replace("\n" , ""))

                if ((pageText.isNotEmpty())) {
                    aya.surah!!.englishName
                    val prvAya = rawData[index - 1]
                    if (isLastAyaInPage || prvAya.surah!!.englishName != aya.surah!!.englishName) {
                        val isNewSurah =
                            rawData.firstOrNull { it.numberInSurah == 1 && it.surah!!.englishName == prvAya.surah!!.englishName } != null

                        if (isLastAyaInPage) {
                            pageText = TextUtils.concat(
                                pageText,
                                textAction.applyQuranSpans(
                                    aya.getFormattedAya(),
                                    pageText.length,
                                    aya
                                )
                            )
                            outputTos.add(QuranFormattedPage(prvAya, pageText, isNewSurah))
                            return@forEachIndexed
                        }
                        outputTos.add(QuranFormattedPage(prvAya, pageText, isNewSurah))
                        pageText = ""
                    }
                }

                val decoratedText =
                    textAction.applyQuranSpans(aya.getFormattedAya(), pageText.length, aya)
                pageText = TextUtils.concat(pageText, decoratedText)

            }
        }
        return outputTos
    }
}