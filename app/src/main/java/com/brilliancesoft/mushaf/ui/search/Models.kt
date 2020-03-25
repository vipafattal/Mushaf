package com.brilliancesoft.mushaf.ui.search

import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import kotlinx.serialization.Serializable

/**
 * Created by ${User} on ${Date}
 */
object Models {
    @Serializable
    data class SearchableQuran(val data: Data)
    @Serializable
    data class Data(val ayahs: List<Aya>, val edition: Edition)
}