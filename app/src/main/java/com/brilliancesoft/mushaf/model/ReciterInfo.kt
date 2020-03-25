package com.brilliancesoft.mushaf.model

import androidx.room.Embedded
import androidx.room.Relation

data class ReciterInfo(
    @Embedded
    val reciter: Reciter,
    @Relation(
        parentColumn = "surah_number",
        entityColumn = "number"
    )
    val surah: Surah
)