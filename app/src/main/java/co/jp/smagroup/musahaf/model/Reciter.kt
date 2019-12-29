package co.jp.smagroup.musahaf.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import co.jp.smagroup.musahaf.framework.database.RECITERS_TABLE


@Entity(
    tableName = RECITERS_TABLE,
    primaryKeys = ["number_in_mushaf", "edition_id"]
)
data class Reciter @Ignore constructor(
    @ColumnInfo(name = "number_in_mushaf")
    val number: Int,
    val edition_id: String,
    val surah_number: Int,
    val uri: Uri?,
    val name: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
    @Ignore
    var surah: Surah? = null
) {
    constructor(
        number: Int,
        edition_id: String,
        surah_number: Int,
        uri: Uri?,
        name: String,
        numberInSurah: Int,
        juz: Int,
        page: Int
    ) : this(number, edition_id, surah_number, uri, name, numberInSurah, juz, page, null)

    @Ignore
    constructor(aya: Aya, reciterIdentifier: String, reciterName: String, uri: Uri) : this(
        number = aya.number,
        edition_id = reciterIdentifier,
        surah_number = aya.surah_number,
        page = aya.page,
        juz = aya.juz,
        numberInSurah = aya.numberInSurah,
        name = reciterName,
        uri = uri
    )

    @Ignore
    constructor(reciterInfo: ReciterInfo) : this(
        number = reciterInfo.reciter.number,
        edition_id = reciterInfo.reciter.edition_id,
        surah_number = reciterInfo.reciter.surah_number,
        page = reciterInfo.reciter.page,
        juz = reciterInfo.reciter.juz,
        numberInSurah = reciterInfo.reciter.numberInSurah,
        name = reciterInfo.reciter.name,
        uri = reciterInfo.reciter.uri,
        surah = reciterInfo.surah
    )
}