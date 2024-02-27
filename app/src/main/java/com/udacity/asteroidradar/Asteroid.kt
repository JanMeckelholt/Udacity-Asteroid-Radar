package com.udacity.asteroidradar

import android.os.Parcelable
import com.udacity.asteroidradar.api.formatDate
import com.udacity.asteroidradar.database.DatabaseAsteroid
import kotlinx.android.parcel.Parcelize
import java.util.Calendar

@Parcelize
data class Asteroid(
    val id: Long,
    val codename: String,
    val closeApproachDate: String,
    val absoluteMagnitude: Double,
    val estimatedDiameter: Double,
    val relativeVelocity: Double,
    val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean,
) : Parcelable {
    val isFromToday: Boolean
        get() = closeApproachDate == formatDate(Calendar.getInstance().time)
}

fun Asteroid.asDatabaseModel(): DatabaseAsteroid {
    return DatabaseAsteroid(
        id, codename, closeApproachDate, absoluteMagnitude, estimatedDiameter, relativeVelocity, distanceFromEarth, isPotentiallyHazardous
    )
}