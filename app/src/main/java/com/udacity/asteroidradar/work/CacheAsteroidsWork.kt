package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.AsteroidRepository
import com.udacity.asteroidradar.database.getDatabase
import retrofit2.HttpException

class CacheAsteroidsWork(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "CacheAsteroidsWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidRepository(database)
        return try {
            repository.cacheAsteroids()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}
