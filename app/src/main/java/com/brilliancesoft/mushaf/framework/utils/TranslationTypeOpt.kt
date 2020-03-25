package com.brilliancesoft.mushaf.framework.utils

import androidx.annotation.StringDef
import com.brilliancesoft.mushaf.model.Edition

/**
 * Created by ${User} on ${Date}
 */
@Target(AnnotationTarget.VALUE_PARAMETER,AnnotationTarget.LOCAL_VARIABLE,AnnotationTarget.FIELD,AnnotationTarget.PROPERTY,AnnotationTarget.PROPERTY_GETTER,AnnotationTarget.FUNCTION)
@StringDef(Edition.Tafsir,Edition.Translation)
@Retention(AnnotationRetention.SOURCE)
annotation class TranslationTypeOpt