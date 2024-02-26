package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDomainModel
import com.udacity.asteroidradar.api.formatDate
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.util.Calendar

enum class AsteroidApiStatus { LOADING, ERROR, DONE }
class MainViewModel : ViewModel() {
    private val _status = MutableLiveData<AsteroidApiStatus>()
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    private val _asteroids = MutableLiveData<List<Asteroid>?>()
    val asteroids: LiveData<List<Asteroid>?>
        get() = _asteroids

    private val _iod = MutableLiveData<String?>()
    val IOD: LiveData<String?>
        get() = _iod
    init {
        getAsteroids()
    }

    private fun getAsteroids() {
        viewModelScope.launch {
            try {
                _status.value= AsteroidApiStatus.LOADING
                val calendar = Calendar.getInstance()
                val today = formatDate(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val inSevenDays = formatDate(calendar.time)
                val asteroidsResponse = AsteroidApi.retrofitService.getAsteroids(startDate = inSevenDays, endDate = today )
                val iodResponse = AsteroidApi.retrofitService.getIOD()
                Timber.i("Asteroids Class: ${asteroidsResponse.javaClass}")
                Timber.i("IOD Class: ${iodResponse.javaClass}")
                Timber.v("url: ${iodResponse.asDomainModel()}")
                _iod.value = iodResponse.asDomainModel()
                val listResult = asteroidsResponse.asDomainModel()
                if (listResult.isNotEmpty()) {
                    _asteroids.value = listResult
                    Timber.i("got ${listResult.size} items")
                }
                _status.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                Timber.e("Failure getting Asteroid Properties: $e")
                _status.value = AsteroidApiStatus.ERROR
                _asteroids.value = ArrayList()
            }
        }

    }
}

