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
        val visibility = arguments?.getString("visibility")
        var direction = arguments?.getString("direction")
        direction = direction?.let { convertWindDirection(it) }

        view.findViewById<TextView>(R.id.pressure).text = pressure
        view.findViewById<TextView>(R.id.humidity).text = humidity
        view.findViewById<TextView>(R.id.wind).text = wind
        view.findViewById<TextView>(R.id.clouds).text = cloudiness
        view.findViewById<TextView>(R.id.visibility).text = visibility
        view.findViewById<TextView>(R.id.windDirection).text = direction
    }

    private fun convertWindDirection(dir: String) : String{
        var check = dir.toInt()
        if(check in 1..25){
            return "Północ"
        }
        else if(check in 26..65){
            return "Północny Wschód"
        }
        else if(check in 66..115){
            return "Wschód"
        }
        else if(check in 116..155){
            return "Południowy Wschód"
        }
        else if(check in 156..205){
            return "Południe"
        }
        else if(check in 206..245){
            return "Południowy Zachód"
        }
        else if(check in 246..295){
            return "Zachód"
        }
        else if(check in 296..335){
            return "Północny Zachód"
        }
        else{
            return "Północ"
        }
    }
}