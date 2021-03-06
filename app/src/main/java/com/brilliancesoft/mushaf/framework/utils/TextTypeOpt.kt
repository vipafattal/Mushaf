package com.brilliancesoft.mushaf.framework.utils

import androidx.annotation.StringDef
import com.brilliancesoft.mushaf.model.Edition

/**
 * Created by ${User} on ${Date}
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION
)
@StringDef(Edition.TYPE_QURAN, Edition.TYPE_TAFSEER, Edition.TYPE_TRANSLATION)
@Retention(AnnotationRetention.BINARY)
annotation class TextTypeOpt