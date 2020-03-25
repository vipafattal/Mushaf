package com.brilliancesoft.mushaf.framework.commen

import com.brilliancesoft.mushaf.R

/**
 * Created by ${User} on ${Date}
 */
object MushafConstants {
    const val BASE_URL = "http://api.alquran.cloud/v1/"

    const val MainMushaf = "quran-uthmani"
    const val WordByWord = "quran-wordbyword"
    const val SimpleQuran = "quran-simple-clean"

    const val Text = "text"
    const val Audio = "audio"

    const val AyatNumber = 6236
    const val SurahsNumber = 604
    const val Fatiha = "Al-Faatiha"
    const val Tawba = "At-Tawba"

    @Suppress("DEPRECATION")
    @JvmField
    val AppName: String = Stringer(R.string.app_name)

}