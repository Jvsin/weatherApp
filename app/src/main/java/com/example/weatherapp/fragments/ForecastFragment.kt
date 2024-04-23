package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weatherapp.R

class ForecastFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)

//        val cityName = "Warszau"
//        val temperature = "12°C"
//        val weatherDescription = "Not funny"

//        // Wyświetl dane na widoku
//        view.findViewById<TextView>(R.id.cityName).text = "Warszau"
//        view.findViewById<TextView>(R.id.temperature).text = "12°C"
//        view.findViewById<TextView>(R.id.overallStatus).text = "Not funny"
    }
}