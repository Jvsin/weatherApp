package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        fetchWeather("Warsaw")
        fetchForecast("Warsaw")

        val button: Button = findViewById(R.id.button_weather)
        button.setOnClickListener {
            val intent = Intent(this, WeatherViewPager::class.java)
            startActivity(intent)
        }
    }

    private fun fetchWeather(location: String){
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey&lang=pl"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v("dupa", "failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.v("dupa23", json)
                }
                if (response.isSuccessful && json != null) {
                    val weatherData = Gson().fromJson(json, weatherClass::class.java)
                    val outputJson: String = Gson().toJson(weatherData)
//
                    runOnUiThread {
                        saveWeatherData(weatherData, location)
                    }
                } else {
                    Log.v("dupa", "tutaj sie wywala")
                }
            }
        })
    }

    private fun fetchForecast(location: String) {
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$location&appid=$apiKey&lang=pl"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v("dupa","failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.v("dupa", json )
                }
                if (response.isSuccessful && json != null) {
                    val forecastData: forecastClass = Gson().fromJson(json, forecastClass::class.java)
                    val outputJson: String = Gson().toJson(forecastData)
                    Log.v("dupa1", "output json forecast: " + outputJson)
                    runOnUiThread {
                        saveForecastData(forecastData, location)
                    }
                } else {
                    Log.v("dupa","tutaj sie wywala")
                }
            }
        })
    }

    private fun saveWeatherData(weather: weatherClass, location: String) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val gson = Gson()
        val json = gson.toJson(weather)
        editor?.putString("weather", json)
        editor?.apply()
    }

    private fun saveForecastData(forecast: forecastClass, location: String) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val gson = Gson()
        val json = gson.toJson(forecast)
        editor?.putString("forecast", json)
        editor?.apply()
    }
}