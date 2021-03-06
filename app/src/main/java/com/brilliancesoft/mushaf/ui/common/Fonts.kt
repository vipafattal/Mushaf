package com.brilliancesoft.mushaf.ui.common

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.commen.Language

/**
 * Created by ${User} on ${Date}
 */
object Fonts {

    //Arabic fonts.
    const val NormalArFont = R.font.normal_ar
    const val Tafseer = NormalArFont

    //English fonts.
    const val NormalEnFont = R.font.normal_en
    const val EnglishTranslation = NormalEnFont

    fun getNormalFont(context: Context, languageCode: String): Typeface {
        @FontRes
        val fontId: Int = when (Language.getLanguageByCode(languageCode)) {
            is Language.Ar -> NormalArFont
            is Language.En -> NormalEnFont
            is Language.NotImplementedLanguage -> NormalEnFont
        }
        return ResourcesCompat.getFont(context, fontId)!!
    }

    fun getTranslationFont(context: Context, languageCode: String): Typeface {
        @FontRes
        val fontId: Int = when (Language.getLanguageByCode(languageCode)) {
            is Language.Ar -> Tafseer
            is Language.En -> EnglishTranslation
            is Language.NotImplementedLanguage -> EnglishTranslation
        }
        return ResourcesCompat.getFont(context, fontId)!!
    }
}