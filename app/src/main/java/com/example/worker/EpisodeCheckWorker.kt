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

        val tmdbApi = RetrofitClient.tmdbApi

        try {
            // Get tracked TV shows from Firestore
            val firestoreRepository = com.example.data.firebase.FirestoreRepository()
            val userMedia = firestoreRepository.observeUserMedia().first()
            val tvShows = userMedia.filter { it.mediaType == "tv" }

            val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayDateStr = todayFormat.format(Date())

            for (show in tvShows) {
                val details = tmdbApi.getTvDetails(
                    tvId = show.id,
                    apiKey = apiKey
                )

                val nextEpisode = details.next_episode_to_air
                val lastEpisode = details.last_episode_to_air
                
                val hasEpisodeToday = (nextEpisode?.air_date == todayDateStr) || (lastEpisode?.air_date == todayDateStr)

                if (hasEpisodeToday) {
                    val episode = if (nextEpisode?.air_date == todayDateStr) nextEpisode else lastEpisode
                    NotificationHelper.showNotification(
                        applicationContext,
                        "حلقة جديدة اليوم!",
                        "حلقة جديدة من ${show.title.ifEmpty { details.name }} تُعرض اليوم!"
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("EpisodeCheckWorker", "Error checking episodes", e)
            return Result.retry()
        }
    }
}
