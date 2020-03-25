package com.brilliancesoft.mushaf.framework.api

import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.utils.EditionTypeOpt
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface QuranCloudAPI {


    @GET("juz/{juz}/{edition}?in_client_id=rgZIdzuXTueT0uzhNXXCOzo0KVc2twCv")
    fun getQuran(
        @Path("juz") juz: Int,
        @Path("edition") identifier: String = MushafConstants.MainMushaf
    ): Deferred<Response<ApiModels.QuranApi>>

    //http://api.alquran.cloud/v1/edition/language
    @GET("edition/language")
    fun getSupportedLanguage(): Deferred<Response<ApiModels.SupportedLanguage>>

    //http://api.alquran.cloud/v1/edition/format/audio#
    // http://api.alquran.cloud/v1/edition?format=en&language=audio
    //http://api.alquran.cloud/v1/edition?format=text&language=ar
    @GET("edition")
    fun getEditions(
        @Query("format") format: String,
        @Query("language") language: String
    ): Deferred<Response<ApiModels.Editions>>

    @GET("edition")
    fun getAllEditions(): Deferred<Response<ApiModels.Editions>>

    @GET("edition")
    fun getEditionsByType(
        @EditionTypeOpt
        @Query("format")
        type: String
    ): Deferred<Response<ApiModels.Editions>>

}
