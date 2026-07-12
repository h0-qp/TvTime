package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.MediaDao
import com.example.data.remote.RetrofitClient
import com.example.data.remote.TmdbApi
import com.example.data.repository.MediaRepository

import com.example.data.firebase.AuthRepository

interface AppContainer {
    val tmdbApi: TmdbApi
    val mediaDao: MediaDao
    val mediaRepository: MediaRepository
    val authRepository: AuthRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val tmdbApi: TmdbApi by lazy {
        RetrofitClient.tmdbApi
    }

    override val mediaDao: MediaDao by lazy {
        AppDatabase.getDatabase(context).mediaDao()
    }
    
    override val mediaRepository: MediaRepository by lazy {
        MediaRepository(tmdbApi, mediaDao)
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepository()
    }
}
