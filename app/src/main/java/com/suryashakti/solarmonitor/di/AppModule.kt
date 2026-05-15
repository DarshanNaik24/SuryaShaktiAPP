package com.suryashakti.solarmonitor.di

import android.content.Context
import androidx.room.Room
import com.suryashakti.solarmonitor.BuildConfig
import com.suryashakti.solarmonitor.data.AppDatabase
import com.suryashakti.solarmonitor.data.EnergyLogDao
import com.suryashakti.solarmonitor.data.UserDao
import com.suryashakti.solarmonitor.data.SolarPlantDao
import com.suryashakti.solarmonitor.network.GeminiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "solar_monitor_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideEnergyLogDao(database: AppDatabase): EnergyLogDao {
        return database.energyLogDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideSolarPlantDao(database: AppDatabase): SolarPlantDao {
        return database.solarPlantDao()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(): GeminiApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                val url = request.url.newBuilder()
                    .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                    .build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
