package co.jp.smagroup.musahaf.model

import androidx.room.Entity
import co.jp.smagroup.musahaf.framework.database.SURAHS_TABLE
import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey


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
)