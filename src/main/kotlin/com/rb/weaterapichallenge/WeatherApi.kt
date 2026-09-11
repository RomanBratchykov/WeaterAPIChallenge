package com.rb.weaterapichallenge

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ForecastResponse(
    val location: Location,
    val forecast: Forecast
)

data class Location(
    val name: String
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day,
    val hour: List<Hour>?
)

data class Day(
    @SerializedName("mintemp_c") val minTempC: Double,
    @SerializedName("maxtemp_c") val maxTempC: Double,
    val avghumidity: Double,
    @SerializedName("maxwind_kph") val maxWindKph: Double
)

data class Hour(
    @SerializedName("wind_dir") val windDir: String
)

// API Interface
interface WeatherApiService {
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 2
    ): ForecastResponse
}
