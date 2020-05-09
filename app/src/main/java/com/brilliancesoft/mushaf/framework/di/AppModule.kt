package com.brilliancesoft.mushaf.framework.di

import androidx.room.Room
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.api.QuranCloudAPI
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.local.MetadataRepository
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.framework.database.MushafDatabase
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.codebox.lib.android.resoures.Stringer
import com.codebox.lib.extrenalLib.TinyDB
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by ${User} on ${Date}
 */

@Module
open class AppModule {

    @Provides
    @Singleton
    fun quranCloudAPI(): QuranCloudAPI {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(MushafConstants.BASE_URL)
            .client(client)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(QuranCloudAPI::class.java)
    }

    @Provides
    fun tinyDb(): TinyDB = TinyDB(MushafApplication.appContext)

    @Provides
    @Singleton
    fun repository(): Repository = Repository()

    @Provides
    @Singleton
    fun metadataRepository(): MetadataRepository = MetadataRepository()

    @Suppress("DEPRECATION")
    @Provides
    fun databaseDao(): MushafDatabase {
        val database = Room.databaseBuilder(
            MushafApplication.appContext,
            MushafDatabase::class.java,
            Stringer(R.string.app_name)
        ).createFromAsset("Mushaf.db")
            .addMigrations(MushafDatabase.MIGRATION_1_2)
            .build()
        return database
    }


}