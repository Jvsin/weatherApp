package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val tempFourthDay = arguments?.getString("tempFourthDay")
        val tempFifthDay = arguments?.getString("tempFifthDay")

        val thirdDay = arguments?.getString("thirdDayDate")
        val fourthDay = arguments?.getString("fourthDayDate")
        val fifthDay = arguments?.getString("fifthDayDate")

        val firstWeatherID = arguments?.getString("firstWeatherID")
        val secondWeatherID = arguments?.getString("secondWeatherID")
        val thirdWeatherID = arguments?.getString("thirdWeatherID")
        val fourthWeatherID = arguments?.getString("fourthWeatherID")
        val fifthWeatherID = arguments?.getString("fifthWeatherID")

        view.findViewById<TextView>(R.id.nextDayTemp).text = tempFirstDay
        view.findViewById<TextView>(R.id.secondDayTemp).text = tempSecondDay
        view.findViewById<TextView>(R.id.thirdDayTemp).text = tempThirdDay
        view.findViewById<TextView>(R.id.fourthDayTemp).text = tempFourthDay
        view.findViewById<TextView>(R.id.fifthDayTemp).text = tempFifthDay

        view.findViewById<TextView>(R.id.thirdDay).text = thirdDay
        view.findViewById<TextView>(R.id.fourthDay).text = fourthDay
        view.findViewById<TextView>(R.id.fifthDay).text = fifthDay

        if (firstWeatherID != null) {
            setWeatherIcon(view, firstWeatherID, R.id.weatherIconFirst)
        }
        if (secondWeatherID != null) {
            setWeatherIcon(view, secondWeatherID, R.id.weatherIconSecond)
        }
        if (thirdWeatherID != null) {
            setWeatherIcon(view, thirdWeatherID, R.id.weatherIconThird)
        }
        if (fourthWeatherID != null) {
            setWeatherIcon(view, fourthWeatherID, R.id.weatherIconThird)
        }
        if (fifthWeatherID != null) {
            setWeatherIcon(view, fifthWeatherID, R.id.weatherIconThird)
        }
    }

    private fun setWeatherIcon(view: View, id: String, icon: Int) {
        when(id){
            "800" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.sun)
            "801" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.few_clouds)
            "802" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.scattered_clouds)
            "803" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.broken_clouds)
            "521" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.shower_rain)
            "500" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.rain)
            "211" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.thunderstorm)
            "601" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.snow)
            "701" -> view.findViewById<ImageView>(icon).setImageResource(R.drawable.mist)
        }
    }
}