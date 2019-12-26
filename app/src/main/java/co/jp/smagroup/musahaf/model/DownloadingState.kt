package co.jp.smagroup.musahaf.model

import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.serialization.Serializable

/**
 * Created by ${User} on ${Date}
 */
@Serializable
@Table(database = MusahafDatabase::class, allFields = true)
data class DownloadingState(
    @PrimaryKey
    var identifier: String = "",
    var isDownloadCompleted: Boolean = false,
    var stopPoint: Int? = null
) : BaseModel() {
    companion object {
        fun downloadQuranTextCompleted(identifier: String): DownloadingState = DownloadingState(identifier, true, 30)
    }
}