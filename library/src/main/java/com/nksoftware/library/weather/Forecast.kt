/*
 * Copyright (c) Norbert Kraft 2025. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File: Forecast.kt
 * Last modified: 01/01/2025, 14:06
 *
 */

package com.nksoftware.library.weather

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nksoftware.library.R
import com.nksoftware.library.composables.SingleSelectList
import com.nksoftware.library.location.ExtendedLocation
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.Locale
import javax.xml.parsers.SAXParserFactory


class Forecast(forecastFile: String): DefaultHandler() {

   companion object Parameters {

      val forecastPeriod = SingleSelectList<Int>(listOf(1, 3, 5, 10), 0)
      val forecastStep = SingleSelectList(listOf(1, 3, 6, 12), 0)

      fun loadSharedPreferences(preferences: SharedPreferences) {
         forecastPeriod.index = preferences.getInt(forecastPeriodKey, 0)
         forecastStep.index = preferences.getInt(forecastPeriodIncrementKey, 0)
      }

      fun storeSharedPreferences(edit: SharedPreferences.Editor) {
         edit.putInt(forecastPeriodKey, forecastPeriod.index)
         edit.putInt(forecastPeriodIncrementKey, forecastStep.index)
      }

      data class WeatherValueAttribute(val id: Int, val unit: String = "")

      val WeatherValueAttributes = mapOf(
         WeatherValues.Pressure to WeatherValueAttribute(R.string.pressure, "mbar"),
         WeatherValues.Temperature to WeatherValueAttribute(R.string.temperature, "°C"),
         WeatherValues.Dewpoint to WeatherValueAttribute(R.string.dewpoint, "°C"),
         WeatherValues.WindDirection to WeatherValueAttribute(R.string.winddirection),
         WeatherValues.WindSpeed to WeatherValueAttribute(R.string.windspeed, "km/h"),
         WeatherValues.WindGust to WeatherValueAttribute(R.string.windgust, "km/h"),
         WeatherValues.EffCloudCover to WeatherValueAttribute(R.string.effcloudcover, "%"),
         WeatherValues.TotCloudCover to WeatherValueAttribute(R.string.totcloudcover, "%"),
         WeatherValues.SignificantWeather to WeatherValueAttribute(R.string.significantweather),
         WeatherValues.Precipitation to WeatherValueAttribute(R.string.precipitation, "l/m2"),
         WeatherValues.Visibility to WeatherValueAttribute(R.string.visibility, "km")
      )
   }

   private var undefSign = "-"
   private var issuer = ""
   private var productId = ""
   private var generatingProcess = ""
   private var issueTime: ZonedDateTime? = null

   private var forecastType: WeatherValues? = WeatherValues.Pressure
   private var currentValue = ""
   private var currentElement = false
   var parsingCompleted = false

   private var timeslots: MutableList<ZonedDateTime> = mutableListOf()
   var aggregatedTimeslots: List<ZonedDateTime>? by mutableStateOf(null)

   enum class WeatherValues { SignificantWeather, Pressure, Temperature, Dewpoint, WindSpeed, WindGust, WindDirection,
      EffCloudCover, TotCloudCover, Precipitation, Visibility
   }

   private val fcDict = mapOf(
      "PPPP" to WeatherValues.Pressure,
      "TTT" to WeatherValues.Temperature,
      "Td" to WeatherValues.Dewpoint,
      "DD" to WeatherValues.WindDirection,
      "FF" to WeatherValues.WindSpeed,
      "FX1" to WeatherValues.WindGust,
      "Neff" to WeatherValues.EffCloudCover,
      "N" to WeatherValues.TotCloudCover,
      "ww" to WeatherValues.SignificantWeather,
      "RR1c" to WeatherValues.Precipitation,
      "VV" to WeatherValues.Visibility
   )

   private val values: MutableMap<WeatherValues, List<Float>> = mutableMapOf()
   var aggregatedValues by mutableStateOf<Map<WeatherValues, List<Float>>>(mutableMapOf())


   init {
      scanForecast(forecastFile)
   }


   private fun average(values: List<Float?>): Float {
      val sum = values.fold(0f, operation = { acc, v -> acc + (v ?: 0f) })
      val cnt = values.fold(0f, operation = { acc, v -> acc + if (v != null) 1 else 0 })

      return sum / cnt
   }


   private fun sum(values: List<Float?>): Float {
      return values.fold(0f, operation = { acc, v -> acc + (v ?: 0f) })
   }


   private fun maximum(values: List<Float?>): Float {
      return values.maxOf { it ?: 0f }
   }


   private fun scanForecast(file: String) {

      val inp: InputStream = File(file).inputStream()

      val parserFactory = SAXParserFactory.newInstance()
      val parser = parserFactory.newSAXParser()

      parser.parse(inp, this)
   }

   @Throws(SAXException::class)
   override fun startElement(
      uri: String,
      localName: String,
      qName: String,
      attributes: Attributes
   ) {
      currentElement = true
      currentValue = ""

      if (localName.equals("ForecastTimeSteps", ignoreCase = true))
         timeslots = mutableListOf()

      if (localName.equals("Forecast", ignoreCase = true)) {
         val fType = attributes.getValue("elementName")
         forecastType = if (fcDict.containsKey(fType)) fcDict[fType] else null
      }
   }

   @Throws(SAXException::class)
   override fun endElement(
      uri: String,
      localName: String,
      qName: String
   ) {
      when (localName.lowercase(Locale.getDefault())) {
         "issuer"            -> issuer = currentValue
         "productid"         -> productId = currentValue
         "generatingprocess" -> generatingProcess = currentValue
         "issuetime"         -> issueTime = ZonedDateTime.parse(currentValue)
         "defaultundefsign"  -> undefSign = currentValue
         "timestep"          -> ZonedDateTime.parse(currentValue)?.let { timeslots.add(it) }

         "forecasttimesteps" -> {
            aggregatedTimeslots = timeslots.chunked(forecastStep.value).map { it.min() }

            val maxTime = aggregatedTimeslots!![0].plusDays(forecastPeriod.value.toLong())
            aggregatedTimeslots = aggregatedTimeslots!!.filter { it < maxTime }
         }

         "value"             -> {
            if (forecastType != null) {
               var numbers = currentValue.trim().replace("\\s+".toRegex(), ",").split(",").map {
                  it.toFloatOrNull() ?: Float.NaN
               }

               numbers = when (forecastType) {
                  WeatherValues.Pressure -> numbers.chunked(forecastStep.value).map { average(it) / 100f }

                  WeatherValues.Temperature, WeatherValues.Dewpoint ->
                     numbers.chunked(forecastStep.value).map { average(it) - 273.15f }

                  WeatherValues.WindSpeed, WeatherValues.WindGust ->
                     numbers.chunked(forecastStep.value).map { average(it) * 3.6f }

                  WeatherValues.SignificantWeather -> numbers.chunked(forecastStep.value).map { maximum(it) }
                  WeatherValues.Precipitation -> numbers.chunked(forecastStep.value).map { sum(it) }
                  WeatherValues.Visibility -> numbers.chunked(forecastStep.value).map { average(it) / 1000f }

                  else -> numbers.chunked(forecastStep.value).map { it.first() }
               }

               numbers = numbers.filterIndexed { i, _ -> i < aggregatedTimeslots!!.size }
               values[forecastType!!] = numbers
            }
         }

         else -> {}
      }

      currentElement = false
   }

   @Throws(SAXException::class)
   override fun characters(
      ch: CharArray,
      start: Int,
      length: Int
   ) {
      if (currentElement) {
         currentValue += String(ch, start, length)
      }
   }

   @Throws(SAXException::class)
   override fun endDocument() {
      super.endDocument()

      aggregatedValues = values
      parsingCompleted = true
   }


   fun getIssueTimeStr(): String {
      return ExtendedLocation.getTimeStr(issueTime)
   }


   fun getStartingTimeSlots(): Int {
      val slots = timeslots.chunked(forecastStep.value).map { it.min() }

      val now = ZonedDateTime.now()
      var index = slots.indexOfFirst { it > now }
      if (index > 0) index--

      return index
   }
}
