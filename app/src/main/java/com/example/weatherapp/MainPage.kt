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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.phoneView.QuickWeatherView
import com.example.weatherapp.phoneView.WeatherViewPager
import com.example.weatherapp.tabletView.WeatherViewTablet
import com.example.weatherapp.utils.Distance
import com.example.weatherapp.utils.Temperatures
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min

class MainPage : AppCompatActivity() {

    var actualTempUnit: Temperatures = Temperatures.CELSIUS
    var actualDistUnit: Distance = Distance.METERS
    var allCities: MutableSet<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        val layout = findViewById<LinearLayout>(R.id.cityList)
        val input = findViewById<EditText>(R.id.location_input)

        settingsUse()

        var locationList: Set<String> = loadLocations()
        allCities = locationList.toMutableSet()
        locationList.forEachIndexed { it, el ->
            Log.v("MIASTA: wczytanie ", it.toString() +' '+ el)
        }
        fetchData(locationList, layout)

        setTimer()

        val addBtn: Button = findViewById(R.id.add_location_button)
        addBtn.setOnClickListener {
            val newCity: String = input.text.toString()

            if (newCity.isNotEmpty()) {
                val added: Set<String> = (locationList + newCity).toSet()
                saveLocations(added)
//                addButton(layout, added.size - 1, newCity)
//                addButtonWithDelete(layout, added.size - 1, newCity)
                addButtonWithRemoveButton(layout,added.size - 1, newCity)
                fetchWeather(newCity)
                fetchForecast(newCity)
                locationList = added
                allCities = locationList.toMutableSet()
                input.text.clear()
            }
        }

        val showBtn: Button = findViewById(R.id.show_button)
        showBtn.setOnClickListener {
            val cityCheck: String = input.text.toString()

            fetchWeather(cityCheck)
            fetchForecast(cityCheck)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Znaleziono lokalizacje: ")
            builder.setMessage(cityCheck)
            builder.setNeutralButton("Otwórz") { dialog, which ->
                val intent = Intent(this, QuickWeatherView::class.java)
                intent.putExtra("location", cityCheck)
                intent.putExtra("tempUnit", actualTempUnit.toString())
                intent.putExtra("distUnit", actualDistUnit.toString())
                startActivity(intent)
            }
            builder.setPositiveButton("Dodaj do listy") { dialog, which ->
                val added: Set<String> = (locationList + cityCheck).toSet()
                saveLocations(added)
//                addButton(layout, added.size - 1, cityCheck)
//                addButtonWithDelete(layout, added.size - 1, cityCheck)
                addButtonWithRemoveButton(layout, added.size - 1, cityCheck)
                fetchWeather(cityCheck)
                fetchForecast(cityCheck)
                locationList = added
                input.text.clear()
            }
            builder.show()
        }

        val dataFetchBtn: Button = findViewById(R.id.fetchData)
        dataFetchBtn.setOnClickListener {
            locationList = allCities as MutableSet<String>
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
            val list = locationList.toMutableSet()
            clearAllCities(layout, list)
            list.clear()
            locationList = list.toSet()
            allCities = locationList.toMutableSet()
            Log.v("MIASTA: clearAll(): ", loadLocations().toString())
        }
    }

    private fun clearAllCities(layout: LinearLayout, cityList: MutableSet<String>) {
        cityList.forEachIndexed { id, city ->
            Log.v("MIASTA: usuwam: ", city)
            removeButton(layout, id)
            removeWeatherData(city)
            removeForecastData(city)
            removeLocation(city)
        }
        cityList.clear()
        allCities = cityList
//        saveLocations(cityList)
    }

    private fun fetchData(locationList: Set<String>, layout: LinearLayout) {
        if(locationList.isNotEmpty()){
            locationList.forEachIndexed{id, location ->
                fetchWeather(location)
                fetchForecast(location)
//                addButton(layout, id, location)
                Log.v("MIASTA: pobrano: ", "fetchData() " + location)
//                addButtonWithDelete(layout, locationList.size, location)
                addButtonWithRemoveButton(layout, locationList.size, location)
            }
        }
    }

    private fun fetchWeather(location: String){
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey&lang=pl"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient.Builder()
            .cache(null)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v("check", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.v("check23", json)
                }
                if (response.isSuccessful && json != null) {
                    val weatherData = Gson().fromJson(json, weatherClass::class.java)
                    val outputJson: String = Gson().toJson(weatherData)
                    Log.v("MIASTA: pobrano dane dla: ", location + ' ' + outputJson)
                    saveWeatherData(weatherData, location)
//                    runOnUiThread {
//                        saveWeatherData(weatherData, location)
//                    }
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
            .addHeader("Cache-Control", "no-cache")
            .build()

        val client = OkHttpClient.Builder()
            .cache(null)
            .build()

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
                    saveForecastData(forecastData, location)
//                    runOnUiThread {
//                        saveForecastData(forecastData, location)
//                    }
                } else {
                    Log.v("check","tutaj sie wywala")
                }
            }
        })
    }

    private fun saveWeatherData(weather: weatherClass, location: String) {
        if (allCities?.contains(location) == true) {
            removeWeatherData(location)
        }
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val gson = Gson()
        val json = gson.toJson(weather)
        editor?.putString("weather", json)
        editor?.apply()
    }

    private fun saveForecastData(forecast: forecastClass, location: String) {
        if (allCities?.contains(location) == true) {
            removeForecastData(location)
        }
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

    fun addButtonWithDelete(layout: LinearLayout, buttonId: Int, location: String) {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Szerokość
                LinearLayout.LayoutParams.WRAP_CONTENT) // Wysokość
        }

        val cityButton = Button(this).apply {
            text = location
            id = buttonId
            setOnClickListener {
                val intent = Intent(this@MainPage, WeatherViewPager::class.java)
                intent.putExtra("location", location)
                intent.putExtra("tempUnit", actualTempUnit.toString())
                intent.putExtra("distUnit", actualDistUnit.toString())
                startActivity(intent)
            }
        }

        val removeButton = Button(this).apply {
            text = "Usuń"
            setOnClickListener {
                layout.removeView(buttonLayout)
                removeWeatherData(location)
                removeForecastData(location)
                removeLocation(location)
                allCities?.remove(location)
                Log.v("MIASTA: lista allCities po:", allCities.toString())
                Log.v("MIASTA: lista po:", loadLocations().toString())
            }
        }

        buttonLayout.addView(cityButton)
        buttonLayout.addView(removeButton)
        layout.addView(buttonLayout)
    }

//

    fun addButtonWithRemoveButton(layout: LinearLayout, buttonId: Int, location: String) {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Szerokość
                LinearLayout.LayoutParams.WRAP_CONTENT) // Wysokość
        }

        val cityButton = Button(this).apply {
            text = location
            id = buttonId
            layoutParams = LinearLayout.LayoutParams(
                0, // Szerokość
                LinearLayout.LayoutParams.WRAP_CONTENT, // Wysokość
                1f) // Waga
            setOnClickListener {
                if(isTablet()){
                    val intent = Intent(this@MainPage, WeatherViewTablet::class.java)
                    intent.putExtra("location", location)
                    intent.putExtra("tempUnit", actualTempUnit.toString())
                    intent.putExtra("distUnit", actualDistUnit.toString())
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this@MainPage, WeatherViewPager::class.java)
                    intent.putExtra("location", location)
                    intent.putExtra("tempUnit", actualTempUnit.toString())
                    intent.putExtra("distUnit", actualDistUnit.toString())
                    startActivity(intent)
                }
//                val intent = Intent(this@MainPage, WeatherViewPager::class.java)
//                intent.putExtra("location", location)
//                intent.putExtra("tempUnit", actualTempUnit.toString())
//                intent.putExtra("distUnit", actualDistUnit.toString())
//                startActivity(intent)
            }
        }

        val removeButton = Button(this).apply {
            text = "Usuń"
            layoutParams = LinearLayout.LayoutParams(
                0, // Szerokość
                LinearLayout.LayoutParams.WRAP_CONTENT, // Wysokość
                0.2f) // Waga
            setOnClickListener {
                layout.removeView(buttonLayout)
                removeWeatherData(location)
                removeForecastData(location)
                removeLocation(location)
                allCities?.remove(location)
                Log.v("MIASTA: lista allCities po:", allCities.toString())
                Log.v("MIASTA: lista po:", loadLocations().toString())

            }
        }

        buttonLayout.addView(cityButton)
        buttonLayout.addView(removeButton)
        layout.addView(buttonLayout)

    }
    private fun removeButton(layout: LinearLayout, buttonId: Int) {
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


    fun removeLocation(locationToRemove: String) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("locations", null)
        val type = object : TypeToken<Set<String>>() {}.type
        var locations: Set<String> = gson.fromJson(json, type)
        Log.v("MIASTA: usuwam lokalizacje: ", locationToRemove)
        if (locations.contains(locationToRemove)) {
            locations = locations.filter { it != locationToRemove }.toSet()
            val editor = sharedPreferences.edit()
            val newJson = gson.toJson(locations)
            editor.putString("locations", newJson)
            editor.apply()
            Log.v("MIASTA: po usunieciu lokalizacji: ", locationToRemove + ' ' + loadLocations().toString())
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

    private fun isTablet(): Boolean {
        val metrics = resources.displayMetrics
        val dpWidth = metrics.widthPixels / metrics.density
        val dpHeight = metrics.heightPixels / metrics.density
        val smallestWidth = min(dpWidth, dpHeight)
        return smallestWidth >= 600
    }

    private fun setTimer() {
        val timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                allCities?.forEachIndexed{ id, location ->
                    fetchWeather(location)
                    fetchForecast(location)
                    Log.v("TIMER: pobrał: ", location)
                }
            }
        },0,  60000 * 10
        )
    }

}