package co.jp.smagroup.musahaf.framework.di

import androidx.room.Room
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.api.QuranCloudAPI
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.framework.database.MusahafDatabase
import co.jp.smagroup.musahaf.framework.database.MushafDao
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
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
            .baseUrl(MusahafConstants.BASE_URL)
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

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun databaseDao(): MushafDao {
        val database = Room.databaseBuilder(
            MushafApplication.appContext,
            MusahafDatabase::class.java,
            Stringer(R.string.app_name)
        )/*.createFromAsset("Mushaf.db")*/.build()

        return database.mushafDao()
    }

}