package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
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

//    var cityList: Set<String>? = null
    var actualTempUnit: Temperatures = Temperatures.CELSIUS
    var actualDistUnit: Distance = Distance.METERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        val layout = findViewById<LinearLayout>(R.id.cityList)
        val input = findViewById<EditText>(R.id.location_input)

        settingsUse()

        var locationList: Set<String> = loadLocations()
        locationList.forEachIndexed { it, el ->
            Log.v("lista miasta: wczytanie", it.toString() + el)
        }
        fetchData(locationList, layout)

        val addBtn: Button = findViewById(R.id.add_location_button)
        addBtn.setOnClickListener {
            val newCity: String = input.text.toString()

            if (newCity.isNotEmpty()) {
                val added: Set<String> = (locationList + newCity).toSet()
                saveLocations(added)
                addButton(layout, added.size - 1, newCity)
//                addButtonWithRemoveButton(layout, locationList.size, newCity)
                fetchWeather(newCity)
                fetchForecast(newCity)
                locationList = added
            }
        }

        val dataFetchBtn: Button = findViewById(R.id.fetchData)
        dataFetchBtn.setOnClickListener {
            if (locationList.isNotEmpty()) {
                locationList.forEach { location, ->
                    fetchWeather(location)
                    fetchForecast(location)
                }
            }
            Toast.makeText(this, "Pobrano najnowsze dane", Toast.LENGTH_SHORT).show()
        }

        val cityRemoveBtn: Button = findViewById(R.id.deleteCity)
        cityRemoveBtn.setOnClickListener {
            Log.v("lista miasta: przed usunięciem", locationList.toString())
            val list = locationList.toMutableSet()
            clearAllCities(layout, list)
            Log.v("lista miasta: po kliknieciu usun", list.toString())
        }
    }

    private fun clearAllCities(layout: LinearLayout, cityList: MutableSet<String>) {
        cityList.forEachIndexed { id, city ->
            Log.v("lista miasta: miasto do usuniecia: ", id.toString() + city)
            removeButton(layout, id)
            removeWeatherData(city)
            removeForecastData(city)
            Log.v("lista check", id.toString() + city)
            removeLocation(city)
        }
        cityList.clear()
        Log.v("lista miasta: po usunięciu wszystkich", loadLocations().toString())
        Log.v("lista check po clear", cityList.toString())
//        saveLocations(cityList)
    }

    private fun fetchData(locationList: Set<String>, layout: LinearLayout) {
        if(locationList.isNotEmpty()){
            locationList.forEachIndexed{id, location ->
                fetchWeather(location)
                fetchForecast(location)
                addButton(layout, id, location)
//                addButtonWithRemoveButton(layout, locationList.size, location)
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
        Log.v("lista check", buttonId.toString() + location)
        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Szerokość
            LinearLayout.LayoutParams.WRAP_CONTENT) // Wysokość
        button.text = location
        button.id = buttonId
        Log.v("enumy: ", actualTempUnit.toString() + ' ' + actualDistUnit.toString())
        button.setOnClickListener {
            val intent = Intent(this, WeatherViewPager::class.java)
            intent.putExtra("location", location)
            intent.putExtra("tempUnit", actualTempUnit.toString())
            intent.putExtra("distUnit", actualDistUnit.toString())
            startActivity(intent)
        }

        layout.addView(button)
    }

//

    fun addButtonWithRemoveButton(layout: LinearLayout, buttonId: Int, location: String) {
        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.width = 3
        button.text = location
        button.id = buttonId

        val removeButton = Button(this)
        removeButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        removeButton.text = "X"
        removeButton.width = 1
        removeButton.setOnClickListener {
            layout.removeView(button)
        }

        button.setOnClickListener {
            val intent = Intent(this, WeatherViewPager::class.java)
            intent.putExtra("location", location)
            startActivity(intent)
        }

        val horizontalLayout = LinearLayout(this)

        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        horizontalLayout.addView(button)
        horizontalLayout.addView(removeButton)

        layout.addView(horizontalLayout)
    }
    private fun removeButton(layout: LinearLayout, buttonId: Int) {
        Log.v("lista miasta: usuwanie buttona: ", buttonId.toString())
        val button = findViewById<Button>(buttonId)
        layout.removeView(button)
    }

    fun saveLocations(locations: Set<String>) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(locations)
        Log.v("flaga json", json)
        editor.putString("locations", json)
        editor.apply()
    }
    fun loadLocations(): Set<String> {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("locations", null)
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }

//    fun deleteLocation(location: String) {
//        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.remove(location)
//        editor.apply()
//    }

    fun removeLocation(locationToRemove: String) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("locations", null)
        val type = object : TypeToken<Set<String>>() {}.type
        var locations: Set<String> = gson.fromJson(json, type)

        if (locations.contains(locationToRemove)) {
            locations = locations.filter { it != locationToRemove }.toSet()

            val editor = sharedPreferences.edit()
            val newJson = gson.toJson(locations)
            editor.putString("locations", newJson)
            editor.apply()
        }
    }

    private fun settingsUse(){
        val tempSpinner = findViewById<Spinner>(R.id.chooseTempUnits)
        val tempUnits = arrayOf("Celsjusze", "Kelviny", "Farenhajty")
        val adapterTemp = ArrayAdapter(this, android.R.layout.simple_spinner_item, tempUnits)
        tempSpinner.adapter = adapterTemp

        tempSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val unit = parent?.getItemAtPosition(position).toString()
                actualTempUnit = when(unit) {
                    "Kelviny" -> Temperatures.KELVINS
                    "Celsjusze" -> Temperatures.CELSIUS
                    "Farenhajty" -> Temperatures.FAHRENHEITS
                    else -> Temperatures.CELSIUS
                }
                Toast.makeText(parent?.context, "Wybrano: $unit", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                actualTempUnit = Temperatures.CELSIUS
            }
        }

        val distSpinner = findViewById<Spinner>(R.id.chooseDistUnits)
        val distUnits = arrayOf("Metry", "Mile")
        val adapterDist = ArrayAdapter(this, android.R.layout.simple_spinner_item, distUnits)
        distSpinner.adapter = adapterDist

        distSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val unit = parent?.getItemAtPosition(position).toString()
                actualDistUnit = when(unit) {
                    "Metry" -> Distance.METERS
                    "Mile" -> Distance.MILES
                    else -> Distance.METERS
                }
                Toast.makeText(parent?.context, "Wybrano: $unit", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                actualDistUnit = Distance.METERS
            }
        }


    }

}