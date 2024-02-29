package com.udacity.asteroidradar.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.ImageOfDay

class DataTransferObjects {

    @JsonClass(generateAdapter = true)
    data class ApiAsteroidsResponse(

        @Json(name="near_earth_objects") val asteroids: Map<String, List<ApiAsteroid>>,
        @Json(name="element_count") val elementCount: Int
    )
    @JsonClass(generateAdapter = true)
    data class ApiAsteroid(
                            @Json(name="name") val codename: String,
                            @Json(name="id") val id: String,
                           @Json(name="close_approach_data") val closeApproachData: List<ApiCloseApproachData>,
                           @Json(name="absolute_magnitude_h") val absoluteMagnitude: Double,
                           @Json(name="estimated_diameter")val estimatedDiameter: ApiEstimatedDiameter,
                           @Json(name="is_potentially_hazardous_asteroid") val isPotentiallyHazardous: Boolean)

    @JsonClass(generateAdapter = true)
    data class ApiCloseApproachData(
        @Json(name="close_approach_date") val closeApproachDate: String,
        @Json(name="relative_velocity") val relativeVelocity: ApiRelativeVelocity,
        @Json(name="miss_distance") val missDistance: ApiMissDistance
    )

    @JsonClass(generateAdapter = true)
    data class ApiRelativeVelocity(
        @Json(name="kilometers_per_second") val relativeVelocityKilometersPerSecond: String
    )

    @JsonClass(generateAdapter = true)
    data class ApiMissDistance(
        @Json(name="astronomical") val missDistanceAstronomical: String
    )

    @JsonClass(generateAdapter = true)
    data class ApiEstimatedDiameter(
        @Json(name="kilometers") val kilometers: ApiEstimatedDiameterKilometers
    )

    @JsonClass(generateAdapter = true)
    data class ApiEstimatedDiameterKilometers(
        @Json(name="estimated_diameter_max") val diameterMax: Double
    )

    @JsonClass(generateAdapter = true)
    data class ApiIODResponse(val title: String, val url: String, @Json(name="media_type") val mediaType: String)
}

fun DataTransferObjects.ApiAsteroidsResponse.asDomainModel(): List<Asteroid> {
    val asteroids = this.asteroids
    val asteroidList = ArrayList<Asteroid>()
    val nextSevenDaysFormattedDates = getNextSevenDaysFormattedDates()
    for (formattedDate in nextSevenDaysFormattedDates) {
        if (asteroids.containsKey(formattedDate)) {
            val dateAsteroidJsonArray = asteroids.get(formattedDate)
            if (dateAsteroidJsonArray != null) {
                for (asteroidJson in dateAsteroidJsonArray) {
                 val closeApproachData = asteroidJson.closeApproachData[0]
                    val asteroid = Asteroid(
                        asteroidJson.id.toLong(),
                        asteroidJson.codename,
                        formattedDate,
                        asteroidJson.absoluteMagnitude,
                        asteroidJson.estimatedDiameter.kilometers.diameterMax,
                        closeApproachData.relativeVelocity.relativeVelocityKilometersPerSecond.toDouble(),
                        closeApproachData.missDistance.missDistanceAstronomical.toDouble(),
                        asteroidJson.isPotentiallyHazardous
                    )
                    asteroidList.add(asteroid)
                }
            }
        }
    }
    return asteroidList
}

fun DataTransferObjects.ApiIODResponse.asDomainModel(): ImageOfDay {
    return ImageOfDay(
        title = title,
        url = url,
        mediaType = mediaType
    )
}