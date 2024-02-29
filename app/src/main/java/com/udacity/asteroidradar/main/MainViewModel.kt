package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.ImageOfDay
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDomainModel
import com.udacity.asteroidradar.api.asteroidIsOldOrDateIsInvalid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.database.AsteroidRepository
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.util.ArrayList

enum class AsteroidApiStatus { LOADING, ERROR, DONE }
class MainViewModel(application: Application) : AndroidViewModel(application) {
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)


    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    private val _refreshButtonVisible = MutableLiveData<Boolean>()
    val refreshButtonVisible: LiveData<Boolean>
        get() = _refreshButtonVisible

    private val _headerText = MutableLiveData<String>()
    val headerText: LiveData<String>
        get() = _headerText

    private val _status = MutableLiveData<AsteroidApiStatus>()
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    private var allAsteroids: List<Asteroid>? = null
    private val _shownAsteroids = MutableLiveData<List<Asteroid>?>()
    val shownAsteroids: LiveData<List<Asteroid>?>
        get() = _shownAsteroids

    private val _iod = MutableLiveData<ImageOfDay?>()
    val iod: LiveData<ImageOfDay?>
        get() = _iod

    init {
        _headerText.value = "All"
        _refreshButtonVisible.value = true
        getDataFromNasa()
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun navigationDone() {
        _navigateToSelectedAsteroid.value = null
    }

    fun onRefreshClicked() {
        getDataFromNasa()
    }

    fun doneShowingSnackBar() {
        _status.value = AsteroidApiStatus.DONE
    }

    private fun getDataFromNasa() {
        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            getIod()
            val listResult = getAsteroidsFromDatabaseWithDatecheck()
            _status.value = appendAsteroidsFromApi(listResult)
            if (listResult.isNotEmpty()) {
                allAsteroids = listResult
                _shownAsteroids.value = listResult
                Timber.i("got ${listResult.size} items")
                Timber.i("shownAsteroids ${shownAsteroids.value?.size} items")
                _headerText.value = "All - ${shownAsteroids.value?.size} asteroids"
            }
        }

    }

    private suspend fun appendAsteroidsFromApi(listResult: ArrayList<Asteroid>): AsteroidApiStatus {
        try {
            val asteroidsFromApi = AsteroidApi.retrofitService.getAsteroidsForNextSevenDays().asDomainModel()
            Timber.i("Adding ${asteroidsFromApi.size} asteroids from api (may overwrite DB-Asteroids)")
            asteroidsFromApi.map { asteroidFromApi ->
                val duplicateAsteroidList = listResult.filter { it.id == asteroidFromApi.id }
                if (duplicateAsteroidList.isNotEmpty()) {
                    val index = listResult.indexOf(duplicateAsteroidList[0])
                    listResult[index] = asteroidFromApi
                } else {
                    listResult += asteroidFromApi
                }
            }
            listResult.sortBy { it.closeApproachDate }
            return AsteroidApiStatus.DONE
        } catch (e: Exception) {
            Timber.e("Failure getting Asteroid from Api: $e")
            return AsteroidApiStatus.ERROR
        }
    }

    private suspend fun getAsteroidsFromDatabaseWithDatecheck(): ArrayList<Asteroid> {
        val asteroidsFromDB = ArrayList<Asteroid>()
        withContext(Dispatchers.IO) {
            database.asteroidDao.getAsteroids()?.asDomainModel()?.map {
                if (!asteroidIsOldOrDateIsInvalid(it)) {
                    asteroidsFromDB += it
                }
            }
            Timber.i("Added ${asteroidsFromDB.size} current asteroids from Database.")
        }
        return asteroidsFromDB
    }

    private suspend fun getIod() {
        try {
            val iodResponse = AsteroidApi.retrofitService.getIOD()
            Timber.i("IOD Class: ${iodResponse.javaClass}")
            Timber.v("url: ${iodResponse.asDomainModel()}")
            _iod.value = iodResponse.asDomainModel()
        } catch (e: Exception) {
            Timber.e("Failure getting IOD: $e")

        }
    }

    fun filterAsteroids(type: Int) {
        when (type) {
            R.id.show_saved_menu -> {
                showOnlySaved()
                _refreshButtonVisible.value = false
            }

            R.id.show_today_menu -> {
                _shownAsteroids.value = allAsteroids?.filter { it.isFromToday }
                _headerText.value = "Today - ${shownAsteroids.value?.size} asteroids"
                _refreshButtonVisible.value = false
            }

            else -> {
                _shownAsteroids.value = allAsteroids
                _headerText.value = "All - ${shownAsteroids.value?.size} asteroids"
                _refreshButtonVisible.value = true
            }
        }
    }

    private fun showOnlySaved() {
        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            _shownAsteroids.value = asteroidRepository.getAsteroids()
            _headerText.value = "Saved - ${shownAsteroids.value?.size} asteroids"
            _status.value = AsteroidApiStatus.DONE
        }
    }

}

