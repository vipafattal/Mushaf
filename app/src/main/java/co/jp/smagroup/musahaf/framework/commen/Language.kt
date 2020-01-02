package co.jp.smagroup.musahaf.framework.commen

/**
 * Created by ${User} on ${Date}
 */
sealed class Language(val code: String, val name: String) {
    init {
        assert(code.length == 2) { "Please send a language name consisted of two chars." }
    }

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