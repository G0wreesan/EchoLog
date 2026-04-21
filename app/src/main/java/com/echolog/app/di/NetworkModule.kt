package com.echolog.app.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://czsgpgpuuakqbqnxyree.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN6c2dwZ3B1dWFrcWJxbnh5cmVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3ODQ2MzEsImV4cCI6MjA5MjM2MDYzMX0.syRyI61kUZ1ybkkDyBG61EIMoindpo_V6mauIBtUZMY"
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest
}