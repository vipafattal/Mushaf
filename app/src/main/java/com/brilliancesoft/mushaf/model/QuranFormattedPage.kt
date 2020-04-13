package com.brilliancesoft.mushaf.model

/**
 * Created by ${User} on ${Date}
 */
data class QuranFormattedPage(
    val aya: Aya,
    val pagedText: CharSequence,
    val isNewSurah: Boolean
)