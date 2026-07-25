package com.example.data.model

data class WeatherInfo(
    val location: String = "Local Garden",
    val tempC: Int = 28,
    val condition: String = "Partly Cloudy",
    val humidityPercent: Int = 65,
    val rainProbabilityPercent: Int = 20,
    val windSpeedKmh: Int = 12,
    val uvIndex: Int = 6,
    val wateringRecommendation: String = "Moderate watering recommended today. Soil moisture level is good."
)
