package com.brilliancesoft.mushaf.ui.quran.read.helpers

import android.content.Context
import android.text.TextUtils
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.ReadData
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.standard.collections.isLastItem


class QuranPageTextFormatter(private val textAction: FunctionalQuranText, context: Context) {
    private val basmalia = Stringify(R.string.basmalia, context)

    fun format(
        rawData: List<Aya>,
        outputTo: MutableList<in ReadData>
    ) {
        var pageText: CharSequence = ""
        if (rawData.isLastItem(0)) {
            val aya = rawData[0]
            pageText = textAction.getQuranDecoratedText(formatAya(aya), 0, aya)
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
                                    formatAya(aya),
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
                val removedBasmalia =
                    if (aya.surah!!.englishName != MushafConstants.Fatiha && aya.numberInSurah == 1)
                        aya.text.replace(basmalia, "")
                    else
                        aya.text


                val decoratedText =
                    textAction.getQuranDecoratedText(formatAya(aya), pageText.length, aya)
                pageText = TextUtils.concat(pageText, decoratedText)

            }
        }
    }

    companion object {
        fun formatAya(aya: Aya): String {
            //"${whiteSpaceMagnifier(aya.text)} ${aya.numberInSurah.toString().toLocalizedNumber()} "
            return "${aya.text} ${aya.numberInSurah.toLocalizedNumber()} "
        }

        fun formatAya(ayaText: String, numberInSurah: Int): String {
            //"${whiteSpaceMagnifier(aya.text)} ${aya.numberInSurah.toString().toLocalizedNumber()} "
            return "$ayaText ${numberInSurah.toLocalizedNumber()} "
        }
    }
}