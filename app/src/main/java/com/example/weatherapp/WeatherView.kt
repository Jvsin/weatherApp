package com.example.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WeatherView : AppCompatActivity() {

    private val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
//    private lateinit var weatherService: WeatherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
    }

}
