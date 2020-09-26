package com.brilliancesoft.mushaf.model

import androidx.room.Embedded
import androidx.room.Relation

data class AyaWithBookmark(
    @Embedded
    val bookmark: Bookmark,
    @Relation(
        parentColumn = "bookmark_ayaNumber",
        entityColumn = "number_in_mushaf"
    )
    val aya: Aya
)