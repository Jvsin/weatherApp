package com.example.weatherapp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.databinding.BasicDataBinding
import com.example.weatherapp.fragments.AdditionalDataFragment
import com.example.weatherapp.fragments.BasicDataFragment
import com.example.weatherapp.fragments.ForecastFragment
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    val location : String = "Warsaw"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
                    Log.v("dupa", json)
                }
                if (response.isSuccessful && json != null) {
                    val weather = Gson().fromJson(json, weatherClass::class.java)
                    val outputJson: String = Gson().toJson(weather)
                    runOnUiThread {
                        setBasicFrag(weather)
                        setAdditionalFrag(weather)
                    }
                } else {
                    Log.v("dupa", "tutaj sie wywala")
                }
            }
        })

        addFragment(R.id.fragment_forecast, ForecastFragment())
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
        bundle.putString("temperature", temperatureConvert(weather.main.temp))
        bundle.putString("status", weather.weather[0].description)
        bundle.putString("time", timeStampConvert(weather.dt.toLong(), weather.timezone))
        bundle.putString("minTemp", temperatureConvert(weather.main.temp_min))
        bundle.putString("maxTemp", temperatureConvert(weather.main.temp_max))
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
        bundle.putString("wind", weather.main.humidity.toString() + "%")
        bundle.putString("cloudiness", weather.clouds.all.toString() + "%")

        additionalDataFrag.arguments = bundle
        addFragment(R.id.fragment_additional_data, additionalDataFrag)
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

    private fun temperatureConvert(kelvin: Double) : String {
        var temp = kelvin.toInt()
        temp -= 273
        return "$temp°C"
    }
}