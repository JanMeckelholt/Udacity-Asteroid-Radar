package com.udacity.asteroidradar.repository

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AsteroidRepository(private val database: AsteroidDatabase) {
//    val asteroidsLivedata: LiveData<List<Asteroid>> = database.asteroidDao.getAsteroidsLiveData().map {
//        it.asDomainModel()
//    }


    //val asteroids: List<Asteroid>? = database.asteroidDao.getAsteroids()?.asDomainModel()

    suspend fun getAsteroids(): List<Asteroid>? {
        return withContext(Dispatchers.IO){

             database.asteroidDao.getAsteroids()?.asDomainModel()
        }

    }

    suspend fun insertAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            val idsInDb = database.asteroidDao.insertAll(asteroid.asDatabaseModel())
            Timber.i("IDs in DB: $idsInDb")
            val num = database.asteroidDao.getAsteroidsLiveData().value?.size
            Timber.i("num in DB: $num")
        }
    }
}