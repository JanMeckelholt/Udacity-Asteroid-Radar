package com.udacity.asteroidradar.database

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDomainModel
import com.udacity.asteroidradar.api.asteroidIsOldOrDateIsInvalid
import com.udacity.asteroidradar.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AsteroidRepository(private val database: AsteroidDatabase) {
//    val asteroidsLivedata: LiveData<List<Asteroid>> = database.asteroidDao.getAsteroidsLiveData().map {
//        it.asDomainModel()
//    }


    //val asteroids: List<Asteroid>? = database.asteroidDao.getAsteroids()?.asDomainModel()

    suspend fun getAsteroids(): List<Asteroid>? {
        return withContext(Dispatchers.IO) {
            database.asteroidDao.getAsteroids()?.asDomainModel()
        }

    }

    suspend fun insertAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.insertAll(asteroid.asDatabaseModel())
        }
    }

    suspend fun deleteAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.delete(asteroid.asDatabaseModel())
        }
    }

    suspend fun cacheAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroids = AsteroidApi.retrofitService.getAsteroidsForNextSevenDays()
            asteroids.asDomainModel().map { asteroid ->
                database.asteroidDao.insertAll(asteroid.asDatabaseModel())
            }
        }
    }

    suspend fun cleanupOldAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroids = database.asteroidDao.getAsteroids()
            asteroids?.map { asteroid ->
                if (asteroidIsOldOrDateIsInvalid(asteroid.asDomainModel())) {
                    database.asteroidDao.delete(asteroid)
                }
            }
        }
    }

}