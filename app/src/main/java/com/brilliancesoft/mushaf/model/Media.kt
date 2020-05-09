package com.brilliancesoft.mushaf.model


import com.brilliancesoft.mushaf.framework.commen.MediaLinkBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Serializable
data class
Media(
    var link: String = "",
    var reciterId: String = "",
    var title: String = "",
    var subtitle: String = "",
    @Transient
    val tag: Any? = null
) {


    fun toJson(): String = Json.stringify(serializer(), this)


    companion object {
        fun create(aya: Aya, reciterEdition: Edition):Media {
            return Media(
                        link = MediaLinkBuilder.linkGenerator(aya.number, reciterEdition.identifier),
                        reciterId = reciterEdition.identifier,
                        title = reciterEdition.name,
                        subtitle = reciterEdition.identifier,
                        tag = aya

                    )
                }
        }


        fun fromJson(json: String): Media {
            require(json.isNotEmpty())
            return Json.parse(serializer(), json)
        }


}
