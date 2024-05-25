package com.example.weatherapp.tabletView

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min

class WeatherViewTablet : AppCompatActivity() {

    private var location : String = ""
    private var weather: weatherClass? = null
    private var forecast: forecastClass? = null
    private var tempUnit: Temperatures = Temperatures.CELSIUS
    private var distUnit: Distance = Distance.METERS
    val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        location = intent.getStringExtra("location").toString()

        var check = intent.getStringExtra("tempUnit").toString()
        tempUnit = Temperatures.valueOf(check)
        check = intent.getStringExtra("distUnit").toString()
        distUnit = Distance.valueOf(check)

        weather = loadWeatherData()
        forecast = loadForecastData()
        setTimer()

        setBasicFrag()
        setAdditionalFrag()
        setForecastFrag()
    }

    private fun isTablet(): Boolean {
        val metrics = resources.displayMetrics
        val dpWidth = metrics.widthPixels / metrics.density
        val dpHeight = metrics.heightPixels / metrics.density
        val smallestWidth = min(dpWidth, dpHeight)
        return smallestWidth >= 600
    }
    override fun onPause() {
        super.onPause()
//        this.finish()
        timer.cancel()
    }

    private fun addFragment(containerId: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(containerId, fragment)
            .commit()
    }

    private fun loadWeatherData(): weatherClass? {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences?.getString("weather", "")
        return gson.fromJson(json, weatherClass::class.java)
    }

    private fun loadForecastData(): forecastClass? {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences?.getString("forecast", "")
        return gson.fromJson(json, forecastClass::class.java)
    }
    private fun setBasicFrag () : BasicDataFragment {
        val basicDataFrag = BasicDataFragment()
        val bundle = Bundle()

        bundle.putString("city", location)
        bundle.putString("temperature", weather?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("status", weather?.weather?.get(0)?.description)
        bundle.putString("weatherID", weather?.weather?.get(0)?.id.toString())
        bundle.putString("time",
            weather?.dt?.let { timeStampConvert(it.toLong(), weather!!.timezone) })
        bundle.putString("minTemp", weather?.main?.let { temperatureConvert(it.temp_min, tempUnit) })
        bundle.putString("maxTemp", weather?.main?.let { temperatureConvert(it.temp_max, tempUnit) })
        bundle.putString("sunrise",
            weather?.sys?.sunrise?.let { shortTimeStamp(it.toLong(), weather!!.timezone) })
        bundle.putString("sunset",
            weather?.sys?.sunset?.let { shortTimeStamp(it.toLong(), weather!!.timezone) })
        bundle.putString("feelLike",
            weather?.main?.feels_like?.let { temperatureConvert(it, tempUnit) })

        basicDataFrag.arguments = bundle
        addFragment(R.id.fragment_basic_weather, basicDataFrag)
        return basicDataFrag
    }

    private fun setAdditionalFrag() : AdditionalDataFragment {
        val additionalDataFrag = AdditionalDataFragment()
        val bundle = Bundle()
        Log.v("basic1", weather.toString())

        bundle.putString("pressure", weather?.main?.pressure.toString() + " hPa")
        bundle.putString("humidity", weather?.main?.humidity.toString() +  " %")
        bundle.putString("wind", weather?.wind?.speed.toString().format("%.1f") + distanceConvert(distUnit) + "/s")
        bundle.putString("direction", weather?.wind?.deg.toString())
        bundle.putString("cloudiness", weather?.clouds?.all.toString() + " %")
        bundle.putString("visibility", visibilityConvert(weather?.visibility.toString(), distUnit))

        additionalDataFrag.arguments = bundle
        addFragment(R.id.fragment_additional_data, additionalDataFrag)
        return additionalDataFrag
    }

    private fun setForecastFrag() : ForecastFragment {
        val forecastFrag = ForecastFragment()
        val bundle = Bundle()

        bundle.putString("tempFirstDay",
            forecast?.list?.get(4)?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("firstDayDate",
            forecast?.list?.get(4)?.dt?.let { dateTimeStamp(it.toLong(), forecast!!.city.timezone) })
        bundle.putString("firstWeatherID", forecast?.list?.get(4)?.weather?.get(0)?.id.toString())

        bundle.putString("tempSecondDay",
            forecast?.list?.get(12)?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("secondDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[12].dt.toLong(), it.timezone) })
        bundle.putString("secondWeatherID", forecast?.list?.get(12)?.weather?.get(0)?.id.toString())

        bundle.putString("tempThirdDay",
            forecast?.list?.get(20)?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("thirdDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[20].dt.toLong(), it.timezone) })
        bundle.putString("thirdWeatherID", forecast?.list?.get(20)?.weather?.get(0)?.id.toString())

        bundle.putString("tempFourthDay",
            forecast?.list?.get(28)?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("fourthDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[28].dt.toLong(), it.timezone) })
        bundle.putString("FourthWeatherID", forecast?.list?.get(28)?.weather?.get(0)?.id.toString())

        bundle.putString("tempFifthDay",
            forecast?.list?.get(36)?.main?.let { temperatureConvert(it.temp, tempUnit) })
        bundle.putString("fifthDayDate",
            forecast?.city?.let { dateTimeStamp(forecast!!.list[36].dt.toLong(), it.timezone) })
        bundle.putString("fifthWeatherID", forecast?.list?.get(36)?.weather?.get(0)?.id.toString())

        forecastFrag.arguments = bundle
        addFragment(R.id.fragment_forecast, forecastFrag)
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

    private fun distanceConvert(dist: Distance) : String {
        when(dist){
            Distance.METERS -> return "m"
            Distance.MILES -> return "mi"
        }
    }

    private fun visibilityConvert(distance: String, unit: Distance) : String {
        when(unit){
            Distance.METERS -> return (distance.toInt() / 1000).toString().format("%.1f") + " km"
            Distance.MILES -> return (distance.toInt() * 0.0006214).toString().format("%.1f") + " mil"
        }
    }

    private fun setTimer() {
//        timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                loadForecastData()
                loadWeatherData()
                Log.v("TIMER: aktualizacja dla: ", location)
            }
        },0,  1000
        )
    }


}