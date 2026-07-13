package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.remote.RetrofitClient
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpisodeCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isEmpty()) return Result.failure()

        val mediaDao = AppDatabase.getDatabase(applicationContext).mediaDao()
        val tmdbApi = RetrofitClient.tmdbApi

        try {
            // Get tracked TV shows
            val tvShows = mediaDao.getMediaItemsByType("tv").first()

            val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayDateStr = todayFormat.format(Date())

            for (show in tvShows) {
                val details = tmdbApi.getTvDetails(
                    tvId = show.id,
                    apiKey = apiKey
                )

                details.next_episode_to_air?.let { nextEpisode ->
                    if (nextEpisode.air_date == todayDateStr) {
                        NotificationHelper.showNotification(
                            applicationContext,
                            "New Episode Today!",
                            "A new episode of ${show.title ?: details.name} is airing today!"
                        )
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("EpisodeCheckWorker", "Error checking episodes", e)
            return Result.retry()
        }
    }
}
