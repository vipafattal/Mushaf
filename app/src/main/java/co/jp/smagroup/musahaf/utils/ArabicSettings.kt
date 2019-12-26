package co.jp.smagroup.musahaf.utils

import co.jp.smagroup.musahaf.ui.more.SettingsPreferencesConstant
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.isRightToLeft

const val spacedChar = "\u0640"
const val miniAlef = "\u0670"


//function source: https://stackoverflow.com/questions/18580287/how-could-i-remove-arabic-punctuation-form-a-string-in-java
//more details on:https://stackoverflow.com/a/23028130/6470661
val punctuationArray = arrayOf(
    "\u0610",
    "\u0611",
    "\u0612",
    "\u0613",
    "\u0614",
    "\u0615",
    "\u0616",
    "\u0617",
    "\u0618",
    "\u0619",
    "\u061A",
    "\u06D6",
    "\u06D7",
    "\u06D8",
    "\u06D9",
    "\u06DA",
    "\u06DB",
    "\u06DC",
    "\u06DD",
    "\u06DE",
    "\u06DF",
    "\u06E0",
    "\u06E1",
    "\u06E2",
    "\u06E3",
    "\u06E4",
    "\u06E5",
    "\u06E6",
    "\u06E7",
    "\u06E8",
    "\u06E9",
    "\u06EA",
    "\u06EB",
    "\u06EC",
    "\u06ED",
    "\u0640",
    "\u064B",
    "\u064C",
    "\u064D",
    "\u064E",
    "\u064F",
    "\u0650",
    "\u0651",
    "\u0652",
    "\u0653",
    "\u0654",
    "\u0655",
    "\u0656",
    "\u0657",
    "\u0658",
    "\u0659",
    "\u065A",
    "\u065B",
    "\u065C",
    "\u065D",
    "\u065E",
    "\u065F",
    "\u0670"
)
private var numbers = arrayOf(
    '0' to '٠',
    '1' to '١',
    '2' to '٢',
    '3' to '٣',
    '4' to '٤',
    '5' to '٥',
    '6' to '٦',
    '7' to '٧',
    '8' to '٨',
    '9' to '٩'
)
private val reciterNames =
    arrayOf(
        "ar.aymanswoaid" to "د.أيمن سويد",
        "ar.abdulbasitmurattal" to "عبد الباسط عبد الصمد المرتل",
        "ar.abdulsamad" to "عبدالباسط عبدالصمد",
        "ar.abdullahbasfar" to "عبد الله بصفر",
        "ar.abdurrahmaansudais" to "عبدالرحمن السديس",
        "ar.shaatree" to "أبو بكر الشاطري",
        "ar.ahmedajamy" to "أحمد بن علي العجمي",
        "ar.alafasy" to "مشاري العفاسي",
        "ar.hanirifai" to "هاني الرفاعي",
        "ar.husary" to "محمود خليل الحصري",
        "ar.husarymujawwad" to "محمود خليل الحصري المجود",
        "ar.hudhaify" to "علي بن عبدالرحمن الحذيفي",
        "ar.ibrahimakhbar" to "إبراهيم الأخضر",
        "ar.mahermuaiqly" to "ماهر المعيقلي",
        "ar.minshawi" to "محمد صديق المنشاوي",
        "ar.minshawimujawwad" to "محمد صديق المنشاوي المجود",
        "ar.muhammadayyoub" to "محمد أيوب",
        "ar.muhammadjibreel" to "محمد جبريل",
        "ar.saoodshuraym" to "سعود الشريم"
    )
private val reciterNameEnglish = arrayOf(
    "Ayman Sowaid" to "د.أيمن سويد",
    "Abdul Basit" to "عبد الباسط عبد الصمد المرتل",
    "Abdul Samad" to "عبدالباسط عبدالصمد",
    "Abdullah Basfar" to "عبد الله بصفر",
    "Abdurrahmaan As-Sudais" to "عبدالرحمن السديس",
    "Abu Bakr Ash-Shaatree" to "أبو بكر الشاطري",
    "Ahmed ibn Ali al-Ajamy" to "أحمد بن علي العجمي",
    "Alafasy" to "مشاري العفاسي",
    "Hani Rifai" to "هاني الرفاعي",
    "Husary" to "محمود خليل الحصري",
    "Husary (Mujawwad)" to "محمود خليل الحصري المجود",
    "Hudhaify" to "علي بن عبدالرحمن الحذيفي",
    "Ibrahim Akhdar" to "إبراهيم الأخضر",
    "Maher Al Muaiqly" to "ماهر المعيقلي",
    "Minshawi" to "محمد صديق المنشاوي",
    "Minshawy (Mujawwad)" to "محمد صديق المنشاوي المجود",
    "Muhammad Ayyoub" to "محمد أيوب",
    "Muhammad Jibreel" to "محمد جبريل",
    "Saood bin Ibraaheem Ash-Shuraym" to "سعود الشريم"
)

fun String.toLocalizedRevelation() =
    when (this) {
        "Meccan" -> if (isRightToLeft == 1) this else "مكية"
        // "Medinan"
        else -> if (isRightToLeft == 1) this else "مدنية"
    }

fun Int.getAyaWord() =
    if (isRightToLeft != 1) {
        if (this > 10) "آية" else "آيات"
    } else "verses"


//fun String.toLocalizedNumber() = String.format("%d", this)
private val sharedPreference = AppPreferences()

fun String.toLocalizedNumber(): String {
    var output = this
    if (isRightToLeft != 1 || sharedPreference.getBoolean(SettingsPreferencesConstant.ArabicNumbersKey))
        for ((englishNumber, arabicNumber) in numbers)
            output = output.replace(englishNumber, arabicNumber)

    return output
}

fun String.toCurrentLanguageNumber(): String {
    var output = this
    if (isRightToLeft != 1 )
        for ((englishNumber, arabicNumber) in numbers)
            output = output.replace(englishNumber, arabicNumber)

    return output
}

fun Int.toCurrentLanguageNumber(): String {
    var output = this.toString()
    if (isRightToLeft != 1 )
        for ((englishNumber, arabicNumber) in numbers)
            output = output.replace(englishNumber, arabicNumber)

    return output
}

fun Int.toLocalizedNumber(): String {
    var output = this.toString()
    if (isRightToLeft != 1 || sharedPreference.getBoolean(SettingsPreferencesConstant.ArabicNumbersKey))
        for ((englishNumber, arabicNumber) in numbers)
            output = output.replace(englishNumber, arabicNumber)

    return output
}

fun String.toArabicReciterName(originalName: String): String {
    var output = this
    for ((englishName, arabicName) in reciterNames)
        if (this == englishName) {
            output = arabicName
            break
        }
    return if (output == this)
        originalName
    else output
}

fun String.toEnglishReciterName(): String {
    var output = this
    for ((englishName, arabicName) in reciterNameEnglish)
        if (this == arabicName) {
            output = englishName
            break
        }
    return output
}


fun formatMiniAlef(quranicText: CharSequence): String {
    var output = ""


    for (index in 0 until quranicText.length) {
        val char = quranicText[index]
        val prvChar = if (index > 0) quranicText[index - 1].toString() else ""
        output += if (char.toString() != miniAlef || (prvChar == "ى")) char
        else spacedChar + char
    }
    return output
}

fun String.removePunctuation(miniAlefReplacement: String): String {
    //Remove honorific sign
    var output = this.replace("ٮ", "ى")
    for (punct in punctuationArray) {
        val replacement = if (punct == "\u0670") miniAlefReplacement else if (punct == "ٮ") "ى" else ""
        output = output.replace(punct, replacement)
    }
    return output
}

fun String.removePunctuation(): String {

    var output = this.replace("ٮ", "ى")

    for (punct in punctuationArray) {
        val replacement = if (punct == "ٮ") "ى" else ""
        output = output.replace(punct, replacement)
    }
    return output
}

fun String.removeAllPunctuation(): String {
    var output = this.replace(miniAlef, "ا")
    output = output.replace("ٱ", "ا")

    for (punct in punctuationArray) {

        output = output.replace(punct, "")
    }
    return output
}

val Char.isPunctuation: Boolean
    get() = this.toString().equalAnyOf(*punctuationArray)


private fun String.equalAnyOf(vararg chars: String): Boolean {
    var isEqual = false
    for (c in chars) {
        if (this != c)
            continue
        isEqual = true
        break
    }
    return isEqual
}

private fun Char.equalAnyOf(vararg chars: Char): Boolean {
    var isEqual = false
    for (c in chars) {
        if (this != c)
            continue
        isEqual = true
        break
    }

    return isEqual
}


val Char.quranSpecialSimple: Boolean
    get() = equalAnyOf('۞', '۩')
