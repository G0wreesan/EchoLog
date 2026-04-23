package com.echolog.app.di

import android.content.Context
import androidx.room.Room
import com.echolog.app.data.AppDatabase
import com.echolog.app.data.LogDao
import com.echolog.app.data.LogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "echolog_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLogDao(database: AppDatabase): LogDao {
        return database.logDao()
    }

    @Provides
    @Singleton
    fun provideLogRepository(
        logDao: LogDao,
        supabaseClient: SupabaseClient // Use the Client to get Postgrest
    ): LogRepository {
        return LogRepository(logDao, supabaseClient.postgrest)
    }
}