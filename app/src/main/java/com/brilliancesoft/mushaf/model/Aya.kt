package com.brilliancesoft.mushaf.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.database.helpers.AYAT_TABLE
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["number_in_mushaf", "edition_id"],
    tableName = AYAT_TABLE
)
data class Aya @Ignore constructor(
    @ColumnInfo(name = "number_in_mushaf")
    val number: Int,
    var surah_number: Int = 0,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
    var edition_id: String = "",
    val isBookmarked: Boolean = false,
    @Ignore
    var surah: Surah?,
    @Ignore
    var edition: Edition? = null
) {
    constructor(
        number: Int,
        surah_number: Int,
        text: String,
        numberInSurah: Int,
        juz: Int,
        page: Int,
        hizbQuarter: Int,
        edition_id: String,
        isBookmarked: Boolean
    ) : this(
        number,
        surah_number,
        text,
        numberInSurah,
        juz,
        page,
        hizbQuarter,
        edition_id,
        isBookmarked, null, null
    )

    @Ignore
    constructor(aya: Aya, edition: Edition) : this(
        number = aya.number,
        edition_id = edition.identifier,
        surah_number = aya.surah_number,
        text = aya.text,
        numberInSurah = aya.numberInSurah,
        juz = aya.juz,
        page = aya.page,
        hizbQuarter = aya.hizbQuarter,
        isBookmarked = aya.isBookmarked,
        surah = aya.surah,
        edition = aya.edition
    )

    @Ignore
    constructor(ayaWithInfo: AyaWithInfo) : this(
        number = ayaWithInfo.aya.number,
        edition_id = ayaWithInfo.edition.identifier,
        surah_number = ayaWithInfo.aya.surah_number,
        text = ayaWithInfo.aya.text,
        numberInSurah = ayaWithInfo.aya.numberInSurah,
        juz = ayaWithInfo.aya.juz,
        page = ayaWithInfo.aya.page,
        hizbQuarter = ayaWithInfo.aya.hizbQuarter,
        isBookmarked = ayaWithInfo.aya.isBookmarked,
        surah = ayaWithInfo.surah,
        edition = ayaWithInfo.edition
    )

    fun getFormattedAya(): String {
        //"${whiteSpaceMagnifier(aya.text)} ${aya.numberInSurah.toString().toLocalizedNumber()} "
        return "${getTextWithoutBasmalih()} ${numberInSurah.toLocalizedNumber()} "
    }

    private fun getEndIndexOfBasmalih(): Int {
        val endOfBasmalih = text.split(' ')[3]
        return text.indexOf(endOfBasmalih, 0) + endOfBasmalih.length
    }

    private fun getTextWithoutBasmalih(): String {
        return if (numberInSurah == 1
            && surah!!.englishName != MushafConstants.Fatiha
            && surah!!.englishName != MushafConstants.Tawba
        )
            text.substring(startIndex = getEndIndexOfBasmalih())
        else text
    }

    companion object {
        @JvmField
        val SAJDA_LIST: List<Int> =
            listOf(
                1160,
                1722,
                1951,
                2138,
                2308,
                2613,
                2672,
                2915,
                3185,
                3518,
                3994,
                4256,
                4846,
                5905,
                6125
            )
    }


}
