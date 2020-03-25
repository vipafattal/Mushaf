package com.brilliancesoft.mushaf.model

import androidx.room.Embedded
import androidx.room.Relation

data class AyaWithInfo(
    @Embedded
    val aya: Aya,
    @Relation(
        parentColumn = "surah_number",
        entityColumn = "number"
    )
    var surah: Surah,
    @Relation(
        parentColumn = "edition_id",
        entityColumn = "identifier"
    )
    val edition: Edition
)