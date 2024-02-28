package com.udacity.asteroidradar

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.asteroidradar.database.AsteroidDao
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.runBlocking
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class AsteroidDatabaseTest {

    private lateinit var asteroidDao: AsteroidDao
    private lateinit var db: AsteroidDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AsteroidDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        asteroidDao = db.asteroidDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testInsertGetAndDeleteAsteroid() {
        val asteroid = Asteroid(
            id = 2448818,
            codename = "448818 (2011 UU20)",
            closeApproachDate = "2024-03-02",
            absoluteMagnitude = 17.81,
            estimatedDiameter = 1.6294460236,
            relativeVelocity = 11.5198280174,
            distanceFromEarth = 0.4337013405,
            isPotentiallyHazardous = false
        )
        runBlocking {
            asteroidDao.insertAll(asteroid.asDatabaseModel())
            var asteroidList = asteroidDao.getAsteroids()
            requireNotNull(asteroidList)
            assertEquals(1, asteroidList.size)
            assertEquals("448818 (2011 UU20)", asteroidList.asDomainModel()[0].codename)
            asteroidDao.delete(asteroid.asDatabaseModel())
            asteroidList = asteroidDao.getAsteroids()
            assertEquals(0, asteroidList?.size)
        }
    }

}

