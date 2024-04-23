package com.example.weatherapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weatherapp.R
import com.example.weatherapp.dataWeather.Weather
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.databinding.BasicDataBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.dataForecast.forecastClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import android.app.Activity

class BasicDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_basic_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val city = arguments?.getString("city")
        val temp = arguments?.getString("temperature")
        val status = arguments?.getString("status")
        val time = arguments?.getString("time")
        val minTemp = arguments?.getString("minTemp")
        val maxTemp = arguments?.getString("maxTemp")
        val sunrise = arguments?.getString("sunrise")
        val sunset = arguments?.getString("sunset")

        view.findViewById<TextView>(R.id.cityName).text = city
        view.findViewById<TextView>(R.id.temperature).text = temp
        view.findViewById<TextView>(R.id.overallStatus).text = status
        view.findViewById<TextView>(R.id.time).text = time
        view.findViewById<TextView>(R.id.tempMin).text = minTemp
        view.findViewById<TextView>(R.id.tempMax).text = maxTemp
        view.findViewById<TextView>(R.id.sunrise).text = sunrise
        view.findViewById<TextView>(R.id.sunset).text = sunset
    }
}