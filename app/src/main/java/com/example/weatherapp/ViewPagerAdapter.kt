package com.example.weatherapp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weatherapp.dataForecast.forecastClass
import com.example.weatherapp.dataWeather.weatherClass
import com.example.weatherapp.fragments.AdditionalDataFragment
import com.example.weatherapp.fragments.BasicDataFragment
import com.example.weatherapp.fragments.ForecastFragment
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class ViewPagerAdapter(private val activity: FragmentActivity, loc: String) : FragmentStateAdapter(activity) {

    private val frag_items = 3
    private var weather: weatherClass? = null
    private var forecast: forecastClass? = null
    private var location = loc
    init {
        weather = loadWeatherData()
        forecast = loadForecastData()
    }
    override fun getItemCount(): Int {
        return frag_items
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> setBasicFrag()
            1 -> setAdditionalFrag()
            else -> setForecastFrag()
        }
    }

    private fun loadWeatherData(): weatherClass? {
        val sharedPreferences = activity.getSharedPreferences(location, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences?.getString("weather", "")
        return gson.fromJson(json, weatherClass::class.java)
    }

    private fun loadForecastData(): forecastClass? {
        val sharedPreferences = activity.getSharedPreferences(location, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences?.getString("forecast", "")
        return gson.fromJson(json, forecastClass::class.java)
    }

//    private fun addFragment(containerId: Int, fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .add(containerId, fragment)
//            .commit()
//    }

    private fun setBasicFrag () : BasicDataFragment {
        val basicDataFrag = BasicDataFragment()
        val bundle = Bundle()

        bundle.putString("city", weather?.name)
        bundle.putString("temperature", weather?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("status", weather?.weather?.get(0)?.description)
        bundle.putString("weatherID", weather?.weather?.get(0)?.id.toString())
        bundle.putString("time",
            weather?.dt?.let { timeStampConvert(it.toLong(), weather!!.timezone) })
        bundle.putString("minTemp", weather?.main?.let { temperatureConvert(it.temp_min) })
        bundle.putString("maxTemp", weather?.main?.let { temperatureConvert(it.temp_max) })
        bundle.putString("sunrise",
            weather?.sys?.sunrise?.let { shortTimeStamp(it.toLong(), weather!!.timezone) })
        bundle.putString("sunset",
            weather?.sys?.sunset?.let { shortTimeStamp(it.toLong(), weather!!.timezone) })

        basicDataFrag.arguments = bundle
//        addFragment(R.id.fragment_basic_weather, basicDataFrag)
        return basicDataFrag
    }

    private fun setAdditionalFrag() : AdditionalDataFragment {
        val additionalDataFrag = AdditionalDataFragment()
        val bundle = Bundle()
        Log.v("basic1", weather.toString())

        bundle.putString("pressure", weather?.main?.pressure.toString() + " hPa")
        bundle.putString("humidity", weather?.main?.humidity.toString() +  " %")
        bundle.putString("wind", weather?.wind?.speed.toString() + " m/s")
        bundle.putString("direction", weather?.wind?.deg.toString())
        bundle.putString("cloudiness", weather?.clouds?.all.toString() + " %")
        bundle.putString("visibility", weather?.visibility.toString() + " m")

        additionalDataFrag.arguments = bundle
//        addFragment(R.id.fragment_additional_data, additionalDataFrag)
        return additionalDataFrag
    }

    private fun setForecastFrag() : ForecastFragment {
        val forecastFrag = ForecastFragment()
        val bundle = Bundle()

        bundle.putString("tempFirstDay",
            forecast?.list?.get(4)?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("firstDayDate",
            forecast?.list?.get(4)?.dt?.let { dateTimeStamp(it.toLong(), forecast!!.city.timezone) })
        bundle.putString("firstWeatherID", forecast?.list?.get(4)?.weather?.get(0)?.id.toString())

        bundle.putString("tempSecondDay",
            forecast?.list?.get(12)?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("secondDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[12].dt.toLong(), it.timezone) })
        bundle.putString("secondWeatherID", forecast?.list?.get(12)?.weather?.get(0)?.id.toString())

        bundle.putString("tempThirdDay",
            forecast?.list?.get(20)?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("thirdDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[20].dt.toLong(), it.timezone) })
        bundle.putString("thirdWeatherID", forecast?.list?.get(20)?.weather?.get(0)?.id.toString())

        bundle.putString("tempFourthDay",
            forecast?.list?.get(28)?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("fourthDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[28].dt.toLong(), it.timezone) })
        bundle.putString("FourthWeatherID", forecast?.list?.get(28)?.weather?.get(0)?.id.toString())

        bundle.putString("tempFifthDay",
            forecast?.list?.get(36)?.main?.let { temperatureConvert(it.temp) })
        bundle.putString("fifthDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[36].dt.toLong(), it.timezone) })
        bundle.putString("fifthWeatherID", forecast?.list?.get(36)?.weather?.get(0)?.id.toString())

        forecastFrag.arguments = bundle
//        addFragment(R.id.fragment_forecast, forecastFrag)
        return forecastFrag
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

    private fun temperatureConvert(kelvin: Double) : String {
        var temp = kelvin.toInt()
        temp -= 273
        return "$temp°C"
    }

}