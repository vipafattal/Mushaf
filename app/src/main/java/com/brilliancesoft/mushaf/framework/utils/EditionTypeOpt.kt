package com.brilliancesoft.mushaf.framework.utils

import androidx.annotation.StringDef
import com.brilliancesoft.mushaf.framework.commen.MushafConstants

/**
 * Created by ${User} on ${Date}
 */
@Target(AnnotationTarget.VALUE_PARAMETER,AnnotationTarget.LOCAL_VARIABLE,AnnotationTarget.FIELD,AnnotationTarget.PROPERTY,AnnotationTarget.PROPERTY_GETTER,AnnotationTarget.FUNCTION)
@StringDef(MushafConstants.Text, MushafConstants.Audio)
@Retention(AnnotationRetention.BINARY)
annotation class EditionTypeOpt