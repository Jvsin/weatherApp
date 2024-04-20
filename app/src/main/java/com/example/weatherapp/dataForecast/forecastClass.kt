package com.example.weatherapp.dataForecast

import kotlin.collections.List

data class forecastClass(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<com.example.weatherapp.dataForecast.List>,
    val message: Int
)