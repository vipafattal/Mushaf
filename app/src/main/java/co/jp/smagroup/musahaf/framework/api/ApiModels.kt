package co.jp.smagroup.musahaf.framework.api

import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import com.google.gson.annotations.SerializedName


/**
 * Created by ${User} on ${Date}
 */
object ApiModels {

    data class QuranApi(val data: QuranDataApi, val code: Int, val status: String)

    data class QuranDataApi(val ayahs: List<Aya>, val edition: Edition, val number: Int)

    data class SupportedLanguage(
        @SerializedName("data")
        val languages: List<String>,
        val code: Int, val status: String
    )

    data class Editions(@SerializedName("data") var editions: List<Edition>, val code: Int, val status: String)
}