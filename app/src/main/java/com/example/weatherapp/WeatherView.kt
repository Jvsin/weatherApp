package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.Weather
import com.example.weatherapp.dataWeather.weatherClass
import com.google.gson.Gson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class WeatherView : AppCompatActivity() {

    private val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
    private var location: String = ""

    private lateinit var cityNameView: TextView
    private lateinit var temperatureView: TextView
    private lateinit var statusView: TextView

    //    private lateinit var fetchButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        cityNameView = findViewById(R.id.cityNameView)
        temperatureView = findViewById(R.id.temperature)
        statusView = findViewById(R.id.overallStatus)

        location = "Warsaw"
        fetchWeather(location)
        fetchForecast(location)
    }

    private fun fetchWeather(location: String) {
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey"

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
//                    val weather = parseWeatherJson(json)
                    val weather: weatherClass = Gson().fromJson(json, weatherClass::class.java)
                    val outputJson: String = Gson().toJson(weather)


                    Log.v("dupa", "output json weather: " + outputJson)
//                    runOnUiThread {
//                        updateWeatherUI(weather)
//                    }
                } else {
                    Log.v("dupa","tutaj sie wywala")
                }
            }
        })
    }


    private fun fetchForecast(location: String) {
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$location&appid=$apiKey"

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
//                    val weather = parseWeatherJson(json)
                    val forecast: forecastClass = Gson().fromJson(json, forecastClass::class.java)
                    val outputJson: String = Gson().toJson(forecast)


                    Log.v("dupa", "output json forecast: " + outputJson)
//                    runOnUiThread {
//                        updateWeatherUI(weather)
//                    }
                } else {
                    Log.v("dupa","tutaj sie wywala")
                }
            }
        })
    }
    private fun parseWeatherJson(json: String): Weather {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Weather::class.java)
        return adapter.fromJson(json) ?: throw JsonDataException("Invalid JSON format")
    }

    private fun updateWeatherUI(weather: Weather) {
        cityNameView.text = location
//        val temperatureText = getString(R.string.temperature, weather.temperature)

//        temperatureView.text = temperatureText
    }

}
