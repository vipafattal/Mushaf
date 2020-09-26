package com.brilliancesoft.mushaf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brilliancesoft.mushaf.framework.database.helpers.EDITIONS_TABLE
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = EDITIONS_TABLE)
data class Edition(
    @PrimaryKey
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val format: String,
    val type: String
)  {

    companion object {
        const val TYPE_TAFSEER = "tafsir"
        const val TYPE_TRANSLATION = "translation"
        const val TYPE_QURAN = "quran"
    }
}
