package com.udacity.asteroidradar.repository

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            database.asteroidDao.insertAll(asteroid.asDatabaseModel())
        }
    }
    suspend fun deleteAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.delete(asteroid.asDatabaseModel())
        }
    }

}