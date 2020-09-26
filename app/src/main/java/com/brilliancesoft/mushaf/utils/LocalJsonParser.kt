package com.brilliancesoft.mushaf.utils

import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication.Companion.appContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream

/**
 * Created by ${User} on ${Date}
 */

object LocalJsonParser {

    fun <T> parse(assetsFilePath: String, dataSerializer: KSerializer<List<T>>): List<T> {
        val data = appContext.assets.open(assetsFilePath).stringify()

        return Json.decodeFromString(dataSerializer, data)
    }

    fun <T> parse(
        assetsFilePath: String,
        deserializationStrategy: DeserializationStrategy<T>
    ): T {
        val data = appContext.assets.open(assetsFilePath).stringify()

        return Json.decodeFromString(deserializationStrategy, data)
    }

    fun <T> parse(
        assetsFilePath: String,
        deserializationStrategy: DeserializationStrategy<T>,
        ignoreUnknownKeys: Boolean = false
    ): T {
        val data = appContext.assets.open(assetsFilePath).stringify()
        val json = Json { this.ignoreUnknownKeys = ignoreUnknownKeys }
        return json.decodeFromString(deserializationStrategy, data)
    }

    private fun InputStream.stringify(): String {
        return try {
            val bytes = ByteArray(available())
            read(bytes, 0, bytes.size)
            String(bytes)
        } catch (e: IOException) {
            ""
        }
    }
}