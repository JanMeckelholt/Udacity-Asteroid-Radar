package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDomainModel
import com.udacity.asteroidradar.api.formatDate
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.util.Calendar

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

    private val _headerText = MutableLiveData<String>()
    val headerText: LiveData<String>
        get() = _headerText

    private val _status = MutableLiveData<AsteroidApiStatus>()
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    private var allAsteroids : List<Asteroid>? = null
    private val _shownAsteroids = MutableLiveData<List<Asteroid>?>()
    val shownAsteroids: LiveData<List<Asteroid>?>
        get() = _shownAsteroids

    private val _iod = MutableLiveData<String?>()
    val iod: LiveData<String?>
        get() = _iod
    init {
        _headerText.value = "All"
        getDataFromNasa()
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }
    fun navigationDone() {
        _navigateToSelectedAsteroid.value = null
    }


    private fun getDataFromNasa() {
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
                    allAsteroids = listResult
                    _shownAsteroids.value = listResult
                    Timber.i("got ${listResult.size} items")
                }
                _status.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                Timber.e("Failure getting Asteroid Properties: $e")
                _status.value = AsteroidApiStatus.ERROR
                allAsteroids = null
            }
        }

    }

    fun filterAsteroids(type: Int) {
        when (type) {
            R.id.show_saved_menu -> {
                showOnlyFiltered()
                _headerText.value = "Saved"
            }

            R.id.show_today_menu -> {
                _shownAsteroids.value  = allAsteroids?.filter { it.isFromToday }
                _headerText.value = "Today"
            }

            else -> {
                _shownAsteroids.value  = allAsteroids
                _headerText.value = "All"
            }
        }
    }

    private fun showOnlyFiltered(){
        viewModelScope.launch{
            _status.value = AsteroidApiStatus.LOADING
            _shownAsteroids.value = asteroidRepository.getAsteroids()
            _status.value = AsteroidApiStatus.DONE
        }
    }

}

