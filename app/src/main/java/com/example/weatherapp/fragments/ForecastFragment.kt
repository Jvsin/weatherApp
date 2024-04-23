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
        super.onViewCreated(view, savedInstanceState)

        val tempFirstDay = arguments?.getString("tempFirstDay")
        val tempSecondDay = arguments?.getString("tempSecondDay")
        val tempThirdDay = arguments?.getString("tempThirdDay")
        val thirdDay = arguments?.getString("thirdDayDate")

        view.findViewById<TextView>(R.id.nextDayTemp).text = tempFirstDay
        view.findViewById<TextView>(R.id.secondDayTemp).text = tempSecondDay
        view.findViewById<TextView>(R.id.thirdDayTemp).text = tempThirdDay
        view.findViewById<TextView>(R.id.thirdDay).text = thirdDay
    }
}