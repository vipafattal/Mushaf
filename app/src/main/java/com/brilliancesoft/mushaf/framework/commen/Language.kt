package com.brilliancesoft.mushaf.framework.commen

/**
 * Created by ${User} on ${Date}
 */
sealed class Language(code: String, val name: String) {

    object Ar : Language("ar", "Arabic")
    object En : Language("en", "English")

    object NotImplementedLanguage : Language("", "")

    companion object {
        fun getLanguageByCode(code: String): Language = when (code) {
            "ar" -> Ar
            "en" -> En
            else -> NotImplementedLanguage
        }
    }
}