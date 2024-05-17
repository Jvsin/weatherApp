package com.example.weatherapp.phoneView

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.utils.Distance
import com.example.weatherapp.R
import com.example.weatherapp.utils.Temperatures
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.fragments.AdditionalDataFragment
import com.example.weatherapp.fragments.BasicDataFragment
import com.example.weatherapp.fragments.ForecastFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class WeatherViewPager : AppCompatActivity() {

    private var location : String = ""
    private lateinit var viewPager: ViewPager2
    private var tempUnit: Temperatures = Temperatures.CELSIUS
    private var distUnit: Distance = Distance.METERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        location = intent.getStringExtra("location").toString()

        var check = intent.getStringExtra("tempUnit").toString()
        tempUnit = Temperatures.valueOf(check)
        check = intent.getStringExtra("distUnit").toString()
        distUnit = Distance.valueOf(check)

        viewPager = findViewById(R.id.pager)
        viewPager.adapter = ViewPagerAdapter(this, location, tempUnit, distUnit)
    }

    override fun onBackPressed () {
        super.onBackPressed()
        super.onPause()
    }

    private fun addFragment(containerId: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(containerId, fragment)
            .commit()
    }

    private fun setBasicFrag (weather: weatherClass) {
        val basicDataFrag = BasicDataFragment()
        val bundle = Bundle()

        bundle.putString("city", weather.name)
        bundle.putString("temperature", temperatureConvert(weather.main.temp, tempUnit))
        bundle.putString("status", weather.weather[0].description)
        bundle.putString("weatherID", weather.weather[0].id.toString())
        bundle.putString("time", timeStampConvert(weather.dt.toLong(), weather.timezone))
        bundle.putString("minTemp", temperatureConvert(weather.main.temp_min, tempUnit))
        bundle.putString("maxTemp", temperatureConvert(weather.main.temp_max, tempUnit))
        bundle.putString("sunrise", shortTimeStamp(weather.sys.sunrise.toLong(), weather.timezone))
        bundle.putString("sunset", shortTimeStamp(weather.sys.sunset.toLong(), weather.timezone))

        basicDataFrag.arguments = bundle
        addFragment(R.id.fragment_basic_weather, basicDataFrag)
    }

    private fun setAdditionalFrag(weather: weatherClass) {
        val additionalDataFrag = AdditionalDataFragment()
        val bundle = Bundle()

        bundle.putString("pressure", weather.main.pressure.toString() + "hPa")
        bundle.putString("humidity", weather.main.humidity.toString() + "%")
        bundle.putString("wind", weather.wind.speed.toString() + "m/s")
        bundle.putString("cloudiness", weather.clouds.all.toString() + "%")

        additionalDataFrag.arguments = bundle
        addFragment(R.id.fragment_additional_data, additionalDataFrag)
    }

    private fun setForecastFrag(forecast : forecastClass) {
        val forecastFrag = ForecastFragment()
        val bundle = Bundle()

        bundle.putString("tempFirstDay",temperatureConvert(forecast.list[4].main.temp, tempUnit))
        bundle.putString("firstDayDate",dateTimeStamp(forecast.list[4].dt.toLong(), forecast.city.timezone))
        bundle.putString("firstWeatherID",forecast.list[4].weather[0].id.toString())
        bundle.putString("tempSecondDay",temperatureConvert(forecast.list[12].main.temp, tempUnit))
        bundle.putString("secondDayDate",dateTimeStamp(forecast.list[12].dt.toLong(), forecast.city.timezone))
        bundle.putString("secondWeatherID",forecast.list[12].weather[0].id.toString())
        bundle.putString("tempThirdDay",temperatureConvert(forecast.list[20].main.temp, tempUnit))
        bundle.putString("thirdDayDate",dateTimeStamp(forecast.list[20].dt.toLong(), forecast.city.timezone))
        bundle.putString("thirdWeatherID",forecast.list[20].weather[0].id.toString())

        forecastFrag.arguments = bundle
        addFragment(R.id.fragment_forecast, forecastFrag)
    }
    private fun timeStampConvert(time: Long, timezone: Int) : String {
        val date = Date(time * 1000)

        val hours = timezone / 3600
        val minutes = (timezone % 3600) / 60
        val timeZone = String.format("GMT%+d:%02d", hours, minutes)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)

        return dateFormat.format(date)
    }

    private fun shortTimeStamp(time: Long, timezone: Int) : String {
        val date = Date(time * 1000)

        val hours = timezone / 3600
        val minutes = (timezone % 3600) / 60
        val timeZone = String.format("GMT%+d:%02d", hours, minutes)

        val dateFormat = SimpleDateFormat("HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)

        return dateFormat.format(date)
    }

    private fun dateTimeStamp(time: Long, timezone: Int) : String {
        val date = Date(time * 1000)

        val hours = timezone / 3600
        val minutes = (timezone % 3600) / 60
        val timeZone = String.format("GMT%+d:%02d", hours, minutes)

        val dateFormat = SimpleDateFormat("EEEE")
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)

        var result = " "
        when(dateFormat.format(date)){
            "Monday" -> result = "Poniedziałek"
            "Tuesday" -> result = "Wtorek"
            "Wednesday" -> result = "Środa"
            "Thursday" -> result = "Czwartek"
            "Friday" -> result = "Piątek"
            "Saturday" -> result = "Sobota"
            "Sunday" -> result = "Niedziela"
        }
        return result
    }

    private fun temperatureConvert(kelvin: Double, tempEnum: Temperatures) : String {
        var temp = kelvin.toInt()
        when(tempEnum){
            Temperatures.CELSIUS -> {
                temp -= 273
                return "$temp°C"
            }
            Temperatures.KELVINS -> return "$temp°K"
            Temperatures.FAHRENHEITS -> {
                temp = ((temp * 9)/5 - 459.67).toInt()
                return "$temp°F"
            }
        }
    }

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
}