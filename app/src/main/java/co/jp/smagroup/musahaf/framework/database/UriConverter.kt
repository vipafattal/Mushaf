package co.jp.smagroup.musahaf.framework.database

import android.net.Uri
import androidx.room.TypeConverter


class  UriConverter {

    @TypeConverter
    fun fromUri(model: Uri?): String =
        model.toString()

    @TypeConverter
    fun toUri(data: String?): Uri =
    Uri.parse(data)
}