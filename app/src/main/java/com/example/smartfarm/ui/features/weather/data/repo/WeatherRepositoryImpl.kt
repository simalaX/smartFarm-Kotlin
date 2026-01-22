package com.example.smartfarm.ui.features.weather.data.repo

import com.example.smartfarm.activity.BuildConfig  // Changed this line
import com.example.smartfarm.ui.features.weather.data.mapper.toWeatherData
import com.example.smartfarm.ui.features.weather.data.remote.ApiResult
import com.example.smartfarm.ui.features.weather.data.remote.WeatherApiService
import com.example.smartfarm.ui.features.weather.data.remote.safeApiCall
import com.example.smartfarm.ui.features.weather.domain.models.WeatherData

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService
) : WeatherRepository {

    private suspend fun fetchAccuWeatherData(locationQuery: String): ApiResult<WeatherData> {
        // 1. Search for Location Key
        val locationResult = safeApiCall {
            apiService.searchLocation(
                apiKey = BuildConfig.WEATHER_API_KEY,
                location = locationQuery
            )
        }

        val locationKey: String
        val locationName: String

        when (locationResult) {
            is ApiResult.Success -> {
                val location = locationResult.data.firstOrNull()
                if (location != null) {
                    locationKey = location.Key
                    locationName = "${location.LocalizedName}, ${location.Country.EnglishName}"
                } else {
                    return ApiResult.Error(Exception("Location not found"), "Could not find location: $locationQuery")
                }
            }
            is ApiResult.Error -> return locationResult
            else -> return ApiResult.Error(Exception("Unknown error"), "Failed to search for location")
        }

        // 2. Get Current Conditions
        val currentConditionsResult = safeApiCall {
            apiService.getCurrentConditions(
                locationKey = locationKey,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
        }

        val currentConditions = when (currentConditionsResult) {
            is ApiResult.Success -> currentConditionsResult.data.firstOrNull()
            else -> null
        }

        // 3. Get 5-Day Forecast
        val forecastResult = safeApiCall {
            apiService.getFiveDayForecast(
                locationKey = locationKey,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
        }

        return when (forecastResult) {
            is ApiResult.Success -> {
                val weatherData = forecastResult.data.toWeatherData(locationName, currentConditions)
                ApiResult.Success(weatherData)
            }
            is ApiResult.Error -> forecastResult
            else -> ApiResult.Error(Exception("Unknown error"), "Failed to fetch forecast data")
        }
    }

    private suspend fun fetchAccuWeatherDataByCoordinates(lat: Double, lon: Double): ApiResult<WeatherData> {
        // 1. Search for Location Key by Coordinates
        val locationResult = safeApiCall {
            apiService.searchLocationByCoordinates(
                apiKey = BuildConfig.WEATHER_API_KEY,
                coordinates = "$lat,$lon"
            )
        }

        val locationKey: String
        val locationName: String

        when (locationResult) {
            is ApiResult.Success -> {
                val location = locationResult.data
                locationKey = location.Key
                locationName = "${location.LocalizedName}, ${location.Country.EnglishName}"
            }
            is ApiResult.Error -> return locationResult
            else -> return ApiResult.Error(Exception("Unknown error"), "Failed to search for location by coordinates")
        }

        // 2. Get Current Conditions
        val currentConditionsResult = safeApiCall {
            apiService.getCurrentConditions(
                locationKey = locationKey,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
        }

        val currentConditions = when (currentConditionsResult) {
            is ApiResult.Success -> currentConditionsResult.data.firstOrNull()
            else -> null
        }

        // 3. Get 5-Day Forecast
        val forecastResult = safeApiCall {
            apiService.getFiveDayForecast(
                locationKey = locationKey,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
        }

        return when (forecastResult) {
            is ApiResult.Success -> {
                val weatherData = forecastResult.data.toWeatherData(locationName, currentConditions)
                ApiResult.Success(weatherData)
            }
            is ApiResult.Error -> forecastResult
            else -> ApiResult.Error(Exception("Unknown error"), "Failed to fetch forecast data")
        }
    }

    override suspend fun getWeatherByLocation(location: String): Flow<ApiResult<WeatherData>> = flow {
        emit(ApiResult.Loading)
        val result = fetchAccuWeatherData(location)
        emit(result)
    }

    override suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Flow<ApiResult<WeatherData>> = flow {
        emit(ApiResult.Loading)
        val result = fetchAccuWeatherDataByCoordinates(lat, lon)
        emit(result)
    }
}