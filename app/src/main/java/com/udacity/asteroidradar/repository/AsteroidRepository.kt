package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AsteroidRepository(private val database: AsteroidDatabase) {
    val asteroids : LiveData<List<Asteroid>> =database.asteroidDao.getAsteroids().map {
        it.asDomainModel()
    }

    suspend fun insertAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            val idsInDb = database.asteroidDao.insertAll(asteroid.asDatabaseModel())
            Timber.i("IDs in DB: $idsInDb")
            val num = database.asteroidDao.getAsteroids().value?.size
            Timber.i("num in DB: $num")
        }
    }
}