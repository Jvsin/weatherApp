package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
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
import com.example.weatherapp.cities.Location
import com.example.weatherapp.cities.cityList
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.phoneView.QuickWeatherView
import com.example.weatherapp.phoneView.WeatherViewPager
import com.example.weatherapp.tabletView.QuickWeatherTablet
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
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min

class MainPage : AppCompatActivity() {

    var actualTempUnit: Temperatures = Temperatures.CELSIUS
    var actualDistUnit: Distance = Distance.METERS
    var allCities: MutableSet<String>? = null
    var selectedCity: String = ""
    private lateinit var layout: LinearLayout
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        layout = findViewById<LinearLayout>(R.id.cityList)
        input = findViewById<EditText>(R.id.location_input)

        settingsUse()

        var locationList: Set<String> = loadLocations()
        allCities = locationList.toMutableSet()
        locationList.forEachIndexed { it, el ->
            Log.v("LISTA MIASTA", it.toString() +' '+ el)
        }
        fetchData(locationList, layout)

        setTimer()

        val showBtn: Button = findViewById(R.id.show_button)
        showBtn.setOnClickListener {
            val cityCheck: String = setCorrectString(input.text.toString())

            if(cityCheck.isNotEmpty()){
                fetchGeolocality(cityCheck, this)
            }
        }

        val dataFetchBtn: Button = findViewById(R.id.fetchData)
        dataFetchBtn.setOnClickListener {
            val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

            if(isConnected){
                locationList = allCities as MutableSet<String>
                if (locationList.isNotEmpty()) {
                    locationList.forEach { location, ->
                        fetchWeather(location)
                        fetchForecast(location)
                    }
                }
                Toast.makeText(this, "Pobrano najnowsze dane", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
            }
        }

        val cityRemoveBtn: Button = findViewById(R.id.deleteCity)
        cityRemoveBtn.setOnClickListener {
            val list = locationList.toMutableSet()
            clearAllCities(layout, list)
            list.clear()
            locationList = list.toSet()
            allCities = locationList.toMutableSet()
            layout.removeAllViews()
            Log.v("MIASTA: clearAll(): ", loadLocations().toString())
        }
    }

//    @SuppressLint("ServiceCast")
    private fun fetchGeolocality(city: String, context: Context) {
        val apiKey = "6e88eafae4cebe1a2a7de5aedb56ee7b"
        val url = "http://api.openweathermap.org/geo/1.0/direct?q=$city&limit=5&appid=$apiKey"

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        if (isConnected) {
            val request = Request.Builder()
                .url(url)
                .build()

            val client = OkHttpClient.Builder()
                .cache(null)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.v("GEOLOKACJE: ", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        Log.v("GEOLOKACJE: ", json)
                    }
                    if (response.isSuccessful && json != null) {
                        Log.v("GEOLOKACJE: ", "LOKALIZACJA poprawna, pobrałem jsona")
                        val locationsData = Gson().fromJson(json, cityList::class.java)
                        val outputJson: String = Gson().toJson(locationsData)
                        runOnUiThread {
                            openDialogWithCities(context, locationsData)
                        }
                    } else {
                        Log.v("GEOLOKACJE: ", "Lokalizacja niepoprawna")
                    }
                }
            })
        } else {
            Toast.makeText(this, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createListOfCities(citiesList: cityList) : List<Location>{
        Log.v("GEOLOKACJE: ", "tworze liste " + citiesList.toString())
        val list = mutableListOf<Location>()
        var counter = 0
        citiesList.forEach { citySearchItem ->
            Log.v("GEOLOKACJE: ", citySearchItem.name + ' ' + citySearchItem.country)
            val location = Location(citySearchItem.name, citySearchItem.country)
            list.add(counter, location)
            counter++
        }
        Log.v("GEOLOKACJE: ", "lista: " + list.toString())
        return list
    }

    private fun openDialogWithCities(context: Context, searchedCities: cityList){
        var citiesList: List<Location> = createListOfCities(searchedCities)
        val citiesArray = citiesList.map { "${it.name}, ${it.country}" }.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Znaleziono: ")
        builder.setItems(citiesArray) { dialog, which ->
            val check = citiesList[which]
            selectedCity = check.name + ',' + check.country

            fetchWeather(selectedCity)
            fetchForecast(selectedCity)

//            val builder = AlertDialog.Builder(this)
            builder.setTitle("NOWA LOKALIZACJA")
            builder.setMessage(selectedCity)
            builder.setNeutralButton("Otwórz") { dialog, which ->
                if(!isTablet()){
                    val intent = Intent(this, QuickWeatherView::class.java)
                    intent.putExtra("location", selectedCity)
                    intent.putExtra("tempUnit", actualTempUnit.toString())
                    intent.putExtra("distUnit", actualDistUnit.toString())
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this, QuickWeatherTablet::class.java)
                    intent.putExtra("location", selectedCity)
                    intent.putExtra("tempUnit", actualTempUnit.toString())
                    intent.putExtra("distUnit", actualDistUnit.toString())
                    startActivity(intent)
                }
            }
            builder.setPositiveButton("Dodaj do listy") { dialog, which ->
                var locationList = allCities as MutableSet<String>
                val added: MutableSet<String> = (locationList + selectedCity).toMutableSet()
                saveLocations(added)
                addButtonWithRemoveButton(layout, added.size - 1, selectedCity)
                fetchWeather(selectedCity)
                fetchForecast(selectedCity)
                allCities = added
                Log.v("MIASTA: ", "MUTABLE LIST: " + allCities.toString())
                input.text.clear()
            }
            builder.setNegativeButton("Zamknij") { dialog, which ->
                removeLocation(selectedCity)
                input.text.clear()
            }
            builder.show()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun clearAllCities(layout: LinearLayout, cityList: MutableSet<String>) {
        cityList.forEachIndexed { id, city ->
            Log.v("MIASTA: usuwam: ", city)
            removeButton(layout, id)
//            removeWeatherData(city)
//            removeForecastData(city)
            removeLocation(city)
        }
        cityList.clear()
        Log.v("MIASTA:", "clearAll po wszystkim: " + cityList.toString())
        allCities = cityList
        Log.v("MIASTA:", "allcities po wszystkim: " + allCities.toString())
        saveLocations(cityList)
    }

    private fun fetchData(locationList: Set<String>, layout: LinearLayout) {
        if(allCities?.isNotEmpty() == true){
            allCities?.forEachIndexed{id, location ->
                fetchWeather(location)
                fetchForecast(location)
                Log.v("MIASTA: pobrano: ", "fetchData() " + location)
                allCities?.size?.let { addButtonWithRemoveButton(layout, it, location) }
            }
        }
    }

    private fun fetchWeather(location: String){
        Log.v("location check", location)
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
                    Log.v("BOOLEAN: ", "Nieprawidłowa lokalizacja")
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
                    Log.v("check", "nie pobrało jsona")
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


    private fun addButtonWithRemoveButton(layout: LinearLayout, buttonId: Int, location: String) {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
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
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, // Wysokość
                0.2f) // waga
            setTextColor(Color.RED)
            setOnClickListener {
                layout.removeView(buttonLayout)
//                removeWeatherData(location)
//                removeForecastData(location)
                removeLocation(location)
                Log.v("MIASTA: ", "usuwanie " + location)
                Log.v("MIASTA: ", "allCities " + allCities.toString())
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

    fun saveSettings(temp: Temperatures, dist: Distance) {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonTemp = gson.toJson(temp)
        editor.putString("temperature", jsonTemp)
        val jsonDist = gson.toJson(dist)
        editor.putString("distance", jsonDist)
        editor.apply()
    }
    fun setSettings() : Pair<Temperatures, Distance>  {
        val sharedPreferences = getSharedPreferences("city_list", Context.MODE_PRIVATE)
        val gson = Gson()

        val temperatureJson = sharedPreferences.getString("temperature", null)
        val distanceJson = sharedPreferences.getString("distance", null)

        var temp = gson.fromJson(temperatureJson, Temperatures::class.java)
        var dist = gson.fromJson(distanceJson, Distance::class.java)
        return Pair(temp, dist)
    }

    private fun removeLocation(locationToRemove: String) {
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
            editor.remove(locationToRemove)
            editor.putString("locations", newJson)
            editor.apply()
            Log.v("MIASTA: po usunieciu lokalizacji: ", locationToRemove + ' ' + loadLocations().toString())
        }

        val dir = File(filesDir.parent + "/shared_prefs/")
        val file = File(dir, "$locationToRemove.xml")

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.v("Usuwanie pliku", "Plik $locationToRemove SharedPreferences został usunięty")
            } else {
                Log.v("Usuwanie pliku", "Nie udało się usunąć pliku SharedPreferences")
            }
        } else {
            Log.v("Usuwanie pliku", "Plik SharedPreferences nie istnieje")
        }
    }

    private fun settingsUse(){
        var sett = setSettings()

        val tempSpinner = findViewById<Spinner>(R.id.chooseTempUnits)
        val tempUnits = arrayOf("Celsjusze", "Kelviny", "Farenhajty")
        val adapterTemp = ArrayAdapter(this, android.R.layout.simple_spinner_item, tempUnits)
        tempSpinner.adapter = adapterTemp
//        var initialPosition = Temperatures.CELSIUS
        var initialPosition = when (sett.first) {
            Temperatures.KELVINS -> 1
            Temperatures.FAHRENHEITS -> 2
            else -> 0
        }

        tempSpinner.setSelection(initialPosition)

        tempSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val unit = parent?.getItemAtPosition(position).toString()
                actualTempUnit = when(unit) {
                    "Kelviny" -> Temperatures.KELVINS
                    "Celsjusze" -> Temperatures.CELSIUS
                    "Farenhajty" -> Temperatures.FAHRENHEITS
                    else -> Temperatures.CELSIUS
                }
                saveSettings(actualTempUnit, actualDistUnit)
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

        initialPosition = when (sett.second) {
            Distance.MILES -> 1
            else -> 0
        }

        distSpinner.setSelection(initialPosition)

        distSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val unit = parent?.getItemAtPosition(position).toString()
                actualDistUnit = when(unit) {
                    "Metry" -> Distance.METERS
                    "Mile" -> Distance.MILES
                    else -> Distance.METERS
                }
                saveSettings(actualTempUnit, actualDistUnit)
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
                    Log.v("TIMER"," Main page pobrał: " +  location)
                }
            }
        },0,  10000
        )
    }

    private fun setCorrectString(text: String) : String {
        return text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

}