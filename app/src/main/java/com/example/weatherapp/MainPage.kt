package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        //deklaracje przycisków i layoutów
        val layout = findViewById<LinearLayout>(R.id.cityList)
        val input = findViewById<EditText>(R.id.location_input)

        val locationList: List<String> = loadLocations()
//        locationList.forEachIndexed { it, el ->
//            Log.v("miasta", it.toString() +  el)
//        }
        fetchData(locationList, layout)

        val addBtn: Button = findViewById(R.id.add_location_button)
        addBtn.setOnClickListener {
            val newCity: String = input.text.toString()

            val added: List<String> = (locationList + newCity).toList()
            saveLocations(added)
            addButton(layout, locationList.size, newCity)
            fetchWeather(newCity)
            fetchForecast(newCity)
        }

        val dataFetchBtn: Button = findViewById(R.id.fetchData)
        dataFetchBtn.setOnClickListener {
            if(locationList.isNotEmpty()){
                locationList.forEach{location,  ->
                    fetchWeather(location)
                    fetchForecast(location)
                }
            }
            Toast.makeText(this, "Pobrano najnowsze dane", Toast.LENGTH_SHORT).show()
        }

        val cityRemoveBtn: Button = findViewById(R.id.deleteCity)
        cityRemoveBtn.setOnClickListener {
            val added = locationList.toMutableList()
            //TODO: obsluga usuwania konkretnego miasta po nazwie lub indeksie
            removeButton(layout, 0)
            removeWeatherData(locationList[0])
            removeForecastData(locationList[0])
            added.remove("Glowno")
            Log.v("usuwanie", added.toString())
            saveLocations(added.toList())
        }
    }

    private fun fetchData(locationList: List<String>, layout: LinearLayout) {
        if(locationList.isNotEmpty()){
            locationList.forEachIndexed{id, location ->
                fetchWeather(location)
                fetchForecast(location)
                addButton(layout, id, location)
            }
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
                Log.v("check", "failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.v("check23", json)
                }
                if (response.isSuccessful && json != null) {
                    val weatherData = Gson().fromJson(json, weatherClass::class.java)
                    val outputJson: String = Gson().toJson(weatherData)
//
                    runOnUiThread {
                        saveWeatherData(weatherData, location)
                    }
                } else {
                    Log.v("check", "tutaj sie wywala")
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
                Log.v("check","failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.v("dupa", json )
                }
                if (response.isSuccessful && json != null) {
                    val forecastData: forecastClass = Gson().fromJson(json, forecastClass::class.java)
                    val outputJson: String = Gson().toJson(forecastData)
                    Log.v("check1", "output json forecast: " + outputJson)
                    runOnUiThread {
                        saveForecastData(forecastData, location)
                    }
                } else {
                    Log.v("check","tutaj sie wywala")
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

    private fun removeWeatherData(location: String) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("weather")
        editor.apply()
    }

    private fun removeForecastData(location: String) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("forecast")
        editor.apply()
    }

    fun addButton(layout: LinearLayout, buttonId: Int, location: String) {
        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Szerokość przycisku
            LinearLayout.LayoutParams.WRAP_CONTENT) // Wysokość przycisku
        button.text = location
        button.id = buttonId

        button.setOnClickListener {
            val intent = Intent(this, WeatherViewPager::class.java)
            intent.putExtra("location", location)
            startActivity(intent)
        }

        layout.addView(button)
    }
    fun removeButton(layout: LinearLayout, buttonId: Int) {
        val button = findViewById<Button>(buttonId)
        layout.removeView(button)
    }

    fun saveLocations(locations: List<String>) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(locations)
        editor.putString("locations", json)
        editor.apply()
    }
    fun loadLocations(): List<String> {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("locations", null)
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteLocation(location: String) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(location)
        editor.apply()
    }

}