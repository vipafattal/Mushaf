package com.brilliancesoft.mushaf.utils

import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication.Companion.appContext
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

/**
 * Created by ${User} on ${Date}
 */

object LocalJsonParser {

    @UnstableDefault
    fun <T> parse(assetsFilePath: String, dataSerializer: KSerializer<List<T>>): List<T> {
        val data = appContext.assets.open(assetsFilePath).stringify()
        val json = Json.nonstrict

        return json.parse(dataSerializer, data)
    }

    @UnstableDefault
    fun <T> parse(assetsFilePath: String, deserializationStrategy: DeserializationStrategy<T>): T {
        val data = appContext.assets.open(assetsFilePath).stringify()
        val json = Json.nonstrict

        return json.parse(deserializationStrategy, data)
    }

    private fun InputStream.stringify(): String {
        try {
            val bytes = kotlin.ByteArray(available())
            read(bytes, 0, bytes.size)
            return kotlin.text.String(bytes)
        } catch (e: IOException) {
            return ""
        }
    }
}