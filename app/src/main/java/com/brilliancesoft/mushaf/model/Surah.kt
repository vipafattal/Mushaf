package com.brilliancesoft.mushaf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.database.helpers.SURAHS_TABLE
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = SURAHS_TABLE)
data class Surah(
    @PrimaryKey
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int
) {
    val isFatihaOrTawba: Boolean
        get() = englishName == MushafConstants.Fatiha || englishName == MushafConstants.Tawba

}