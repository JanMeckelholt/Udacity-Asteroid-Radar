package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))

    .baseUrl(Constants.NASA_BASE_URL)
    .build()

interface AsteroidApiService {
    @GET(Constants.ASTEROID_PATH)
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String=Constants.NASA_API_KEY,
    ): DataTransferObjects.ApiAsteroidsResponse    @GET(Constants.ASTEROID_PATH)

    suspend fun getAsteroidsForNextSevenDays(
        @Query("start_date") startDate: String = getFormatDateToday(),
        @Query("end_date") endDate: String = getFormatDateInSevenDays(),
        @Query("api_key") apiKey: String=Constants.NASA_API_KEY,
    ): DataTransferObjects.ApiAsteroidsResponse

    @GET(Constants.IOD_PATH)
    suspend fun getIOD(
        @Query("api_key") apiKey: String=Constants.NASA_API_KEY,
    ): DataTransferObjects.ApiIODResponse
}

object AsteroidApi {
    val retrofitService : AsteroidApiService by lazy {
        retrofit.create(AsteroidApiService::class.java)
    }
}
