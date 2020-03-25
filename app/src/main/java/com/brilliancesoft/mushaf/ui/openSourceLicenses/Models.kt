package com.brilliancesoft.mushaf.ui.openSourceLicenses

import kotlinx.serialization.Serializable

/**
 * Created by ${User} on ${Date}
 */
object Models {
    @Serializable
    data class Data(val licenses: List<License>)
    @Serializable
    data class License(val title: String, val licenseInfo: String, val link:String?=null)
}