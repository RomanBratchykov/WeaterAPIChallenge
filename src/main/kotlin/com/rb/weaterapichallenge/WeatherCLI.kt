package com.rb.weaterapichallenge

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

import io.github.cdimascio.dotenv.dotenv

@Component
class WeatherCLI : CommandLineRunner {

    override fun run(vararg args: String) {
        val dotenv = dotenv()
        val apiKey = dotenv["WEATHER_API_KEY"]
        if (apiKey.isNullOrEmpty()) {
            println("Error: WEATHER_API_KEY environment variable is not set.")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        val cities = listOf("Chisinau", "Madrid", "Kyiv", "Amsterdam")
        
        println("Fetching forecasts for tomorrow...")

        runBlocking {
            try {
                val responses = cities.map { city ->
                    async {
                        service.getForecast(apiKey, city, days = 2)
                    }
                }.awaitAll()

                printTable(responses)
            } catch (e: Exception) {
                println("Failed to retrieve weather data: \${e.message}")
            }
        }
    }

    private fun printTable(responses: List<ForecastResponse>) {
        if (responses.isEmpty()) return

        val tomorrowStr = LocalDate.now().plusDays(1).toString()
        
        println("-".repeat(110))
        println(String.format("%-15s | %-12s | %-12s | %-12s | %-12s | %-12s | %-12s", "City", "Date", "Min Temp(C)", "Max Temp(C)", "Humidity(%)", "Wind(kph)", "Wind Dir"))
        println("-".repeat(110))

        responses.forEach { response ->
            val tomorrowForecast = response.forecast.forecastday.find { it.date == tomorrowStr }
                ?: response.forecast.forecastday.getOrNull(1)

            if (tomorrowForecast != null) {
                val day = tomorrowForecast.day
                val windDir = tomorrowForecast.hour?.getOrNull(12)?.windDir ?: tomorrowForecast.hour?.firstOrNull()?.windDir ?: "N/A"
                
                println(String.format(
                    "%-15s | %-12s | %-12.1f | %-12.1f | %-12.1f | %-12.1f | %-12s",
                    response.location.name,
                    tomorrowForecast.date,
                    day.minTempC,
                    day.maxTempC,
                    day.avghumidity,
                    day.maxWindKph,
                    windDir
                ))
            } else {
                println(String.format("%-15s | %-12s | %-12s", response.location.name, "N/A", "Data unavailable for tomorrow"))
            }
        }
        println("-".repeat(110))
    }
}
