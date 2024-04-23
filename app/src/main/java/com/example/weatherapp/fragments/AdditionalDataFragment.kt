package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weatherapp.R

class AdditionalDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pressure = arguments?.getString("pressure")
        val humidity = arguments?.getString("humidity")
        val wind = arguments?.getString("wind")
        val cloudiness = arguments?.getString("cloudiness")

        view.findViewById<TextView>(R.id.pressure).text = pressure
        view.findViewById<TextView>(R.id.humidity).text = humidity
        view.findViewById<TextView>(R.id.wind).text = wind
        view.findViewById<TextView>(R.id.clouds).text = cloudiness

    }
}