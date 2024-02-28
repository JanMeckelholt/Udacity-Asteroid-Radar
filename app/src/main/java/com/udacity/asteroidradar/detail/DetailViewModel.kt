package com.udacity.asteroidradar.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.database.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailViewModel(application: Application, val asteroid: Asteroid) : AndroidViewModel(application) {
    class Factory(val app: Application, val asteroid: Asteroid) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Timber.i("creating DetailViewModel ${asteroid.codename}")
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(app, asteroid) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _isSaved = MutableLiveData<Boolean?>()
    val isSaved: LiveData<Boolean?>
        get() = _isSaved

    private val _requestConfirmDelete = MutableLiveData<Boolean?>()
    val requestConfirmDelete: LiveData<Boolean?>
        get() = _requestConfirmDelete

    fun onSaveOrDeleteClicked() {
        isSaved.value?.let {
            if (it) {
                _requestConfirmDelete.value = true
            } else {
                viewModelScope.launch {
                    saveAsteroid()
                }
            }
        }
    }

    fun requestDelete() {
        _requestConfirmDelete.value = false
        viewModelScope.launch {
            deleteAsteroid()
        }
    }
    fun cancelDelete() {
        _requestConfirmDelete.value = false
    }

    private suspend fun saveAsteroid() {
        asteroidRepository.insertAsteroid(asteroid)
        _isSaved.value = true
    }

    private suspend fun deleteAsteroid() {
        asteroidRepository.deleteAsteroid(asteroid)
        _isSaved.value = false
    }

    init {
        _isSaved.value = null
        getIsSavedStatus()
    }

    private fun getIsSavedStatus() {
        viewModelScope.launch {
            _isSaved.value = asteroidRepository.getAsteroids()?.contains(asteroid)
            Timber.i("in launch: ${asteroid.codename} is saved: ${_isSaved.value}")
        }
    }

}