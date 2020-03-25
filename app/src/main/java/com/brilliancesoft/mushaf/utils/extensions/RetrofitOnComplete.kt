package com.brilliancesoft.mushaf.utils.extensions

import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.Deferred
import retrofit2.Response
import java.io.IOException
import java.net.ProtocolException
import java.net.UnknownHostException

/**
 * Created by ${User} on ${Date}
 */

@Keep
suspend inline fun <reified T> Deferred<Response<T>>.onComplete(
    onSuccess: (data: T) -> Unit,
    onError: (e: String) -> Unit
) {
    var errorMsg: String? = null
    var data: T? = null
    val className = T::class

    try {
        val response = this@onComplete.await()
        if (response.isSuccessful) {
            data = response.body()
        } else {
            Log.d("Unknown error for $className", response.errorBody().toString())
            errorMsg = "Unknown error"
        }
    } catch (e: UnknownHostException) {
        Log.d("Get response for $className", e.message ?: "Unknown error")
        errorMsg = "No internet connection"
    } catch (io: IOException) {
        Log.d("Get response for $className", io.message ?: "Unknown error")
        errorMsg = "Timeout"
    } catch (pro: ProtocolException) {
        Log.d("Get response for $className", pro.message ?: "Unknown error")
        errorMsg = "Connection Error"
    }
    if (data != null)
        onSuccess(data)
    else
        onError(errorMsg ?: "Unknown Error")

}

/*suspend inline fun <reified T> Deferred<Response<T>>.onComplete(
    onComplete: (prepareMainList: T?, errorMsg: String?) -> Unit
) {
    var errorMsg: String? = null
    var prepareMainList: T? = null
    val className = T::class.java.name

    try {
        val response = this@onComplete.await()
        if (response.isSuccessful) {
            prepareMainList = response.body()
        } else {
            Log.d("Unknown error for $className", response.errorBody().toString())
            errorMsg = "Unknown error"
        }
    } catch (e: UnknownHostException) {
        Log.d("Get response for $className", e.message ?: "Unknown error")
        errorMsg = "No internet connection"
    } catch (timeOut: SocketTimeoutException) {
        Log.d("Get response for $className", timeOut.message ?: "Unknown error")
        errorMsg = "Timeout"
    }
    onComplete(prepareMainList, errorMsg)
}*/
