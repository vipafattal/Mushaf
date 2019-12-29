package co.jp.smagroup.musahaf.model

import co.jp.smagroup.musahaf.framework.utils.TranslationTypeOpt

/**
 * Created by ${User} on ${Date}
 */
data class ReadTranslation(
    val ayaNumber: Int,
    val surah: Surah,
    val translationText: String,
    val quranicText: String,
    val editionInfo: Edition,
    @TranslationTypeOpt
    val translationOrTafsir: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
    val isBookmarked: Boolean

) {
    constructor(
        aya: Aya,
        translationText: String,
        quranicText: String,
        editionInfo: Edition,
        @TranslationTypeOpt translationOrTafsir: String,
        isBookmarked: Boolean
    ) : this(
        isBookmarked = isBookmarked,
        ayaNumber = aya.number,
        surah = aya.surah!!,
        numberInSurah = aya.numberInSurah,
        juz = aya.juz,
        page = aya.page,
        hizbQuarter = aya.hizbQuarter,
        quranicText = quranicText,
        translationText = translationText,
        editionInfo = editionInfo,
        translationOrTafsir = translationOrTafsir
    )

}