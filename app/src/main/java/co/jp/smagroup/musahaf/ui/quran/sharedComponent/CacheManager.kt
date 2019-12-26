package co.jp.smagroup.musahaf.ui.quran.sharedComponent

import com.codebox.lib.android.utils.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

/**
 * Created by ${User} on ${Date}
 */
@UnstableDefault
class CacheManager {
    private val preferences = AppPreferences.getInstance(cachedDataFileName)

    init {
        prepareCache()
    }

    private fun prepareCache() {
        val currentSavedCacheVersion = preferences.getInt(cacheVersionKey)
        if (currentSavedCacheVersion != cacheVersion) {
            preferences.clear()
            preferences.put(
                cacheVersionKey,
                cacheVersion
            )
        }
    }

    fun <T> getSavdObject(cacheKey: String, deserializationStrategy: DeserializationStrategy<T>) :T?{
        val data = preferences.getStr(cacheKey, "")
        return if (data.isNotEmpty())
            Json.parse(deserializationStrategy, data)
        else null
    }

    suspend fun <T> getSavedList(cacheKey: String, dataSerializer: KSerializer<List<T>>): List<T>? {
        val data = preferences.getStr(cacheKey, "")
        return if (data.isNotEmpty())
            withContext(Dispatchers.IO) { Json.parse(dataSerializer, data) }
        else null
    }

    fun <T> saveObject(cacheKey: String, serializationStrategy: SerializationStrategy<T>, data: T) {
       val serializedObject= Json.stringify(serializationStrategy, data)
        preferences.put(cacheKey, serializedObject)
    }

    suspend fun <T> saveList(cacheKey: String, dataSerializer: KSerializer<List<T>>, data: List<T>) {
        val serializedData =
            withContext(Dispatchers.IO) { Json.stringify(dataSerializer, data) }
        preferences.put(cacheKey, serializedData)
    }

    companion object {
        private const val cacheVersion = 1
        private const val cacheVersionKey = "Cache-Version-Key"
        private const val cachedDataFileName = "Cached-Data"
    }
}