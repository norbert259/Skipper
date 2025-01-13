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
 * File: Weather.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.weather

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.nksoftware.library.R
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.map.NkMarker
import com.nksoftware.library.map.OsmMap
import com.nksoftware.library.utilities.downloadFile
import com.nksoftware.library.utilities.nkHandleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.io.File
import java.net.URL
import java.util.Calendar
import java.util.zip.ZipFile
import kotlin.collections.set

const val forecastPeriodKey = "WeatherForecastPeriod"
const val forecastPeriodIncrementKey = "WeatherForecastTimeIncrement"

const val logTag = "Weather"


data class DwdMosmixStation(
   val id: String = "",
   val icao: String = "",
   val name: String = "",
   val lat: Double = 0.0,
   val lon: Double = 0.0,
   val elevation: Int = 0
) {

   val loc: Location = ExtendedLocation(lat, lon)
   val gp: GeoPoint = GeoPoint(lat, lon)
}


class DwdMosmixStationList(val ctx: Context, scope: CoroutineScope, private val dir: String) {

   private val stationFileDownload =
      "https://www.dwd.de/DE/leistungen/met_verfahren_mosmix/mosmix_stationskatalog.cfg?view=nasPublication&nn=16102"

   private var stations by mutableStateOf<List<DwdMosmixStation>>(listOf())

   val size: Int
      get() = stations.size

   var selectedStationIndex by mutableIntStateOf(-1)

   val selectedStation: DwdMosmixStation?
      get() = stations.getOrNull(selectedStationIndex)

   init {
      val f = File(dir)
      if (!f.exists()) f.mkdirs()

      scope.launch(Dispatchers.Default) {
         val stationFileName = "$dir/mosmix_stationskatalog.txt"
         val stationFile = File(stationFileName)

         if (stationFile.lastModified() < (Calendar.getInstance().timeInMillis - 2592000000))
            stationFile.delete()

         if (!stationFile.exists()) {
            Log.i(logTag, "Downloading station file: $stationFileDownload")
            downloadFile(URL(stationFileDownload), stationFileName)
         }

         Log.i(logTag, "Scanning station file")
         val tmpStations = mutableListOf<DwdMosmixStation>()

         stationFile.readLines().forEachIndexed { i, line ->
            if (i > 1) {
               tmpStations.add(
                  DwdMosmixStation(
                     id = line.substring(0..4).trim(),
                     icao = line.substring(6..9).trim(),
                     name = line.substring(11..30).trim(),
                     lat = line.substring(32..34).trim().toDouble() +
                           line.substring(35..37).trim().toDouble() / .6,
                     lon = line.substring(39..42).trim().toDouble() +
                           line.substring(43..45).trim().toDouble() / .6,
                     elevation = line.substring(47..51).trim().toInt()
                  )
               )
            }
         }

         stations = tmpStations
      }
   }


   fun clear() {
      selectedStationIndex = -1
   }


   fun getClosestStationId(pos: Location): String {
      val distances = stations.map { s -> pos.distanceTo(s.loc) }
      selectedStationIndex = distances.indexOf(distances.min())

      return stations[selectedStationIndex].id
   }


   fun getStationsInWindow(box: BoundingBox): Map<Int, DwdMosmixStation> {
      val map = mutableMapOf<Int, DwdMosmixStation>()

      stations.forEachIndexed { i, s ->
         if (s.lat in box.latSouth..box.latNorth && s.lon in box.lonWest..box.lonEast)
            map[i] = s
      }

      return map
   }
}


class Weather(
   val ctx: Context,
   private val scope: CoroutineScope,
   private val dir: String,
   mapMode: Int
) : DataModel(mapMode) {

   val stations = DwdMosmixStationList(ctx, scope, dir)
   var forecast by mutableStateOf<Forecast?>(null)

   private val forecastDownload =
      "https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_L/single_stations/xxxxx/kml/MOSMIX_L_LATEST_xxxxx.kmz"
   private val tmpFileName = "$dir/weather.zip"

   private val stationIcon = ContextCompat.getDrawable(ctx, R.drawable.outline_partly_cloudy_day_24)
   private val selectedStationIcon = ContextCompat.getDrawable(ctx, R.drawable.outline_partly_cloudy_day_24_red)

   private val stationMarker: MutableMap<Int, NkMarker> = mutableMapOf()


   init {
      val f = File(dir)
      if (!f.exists()) f.mkdirs()
   }


   fun clear() {
      forecast = null
      stations.clear()
   }


   fun downloadNearestStation(pos: Location, msg: (String) -> Unit) {
      scope.launch(Dispatchers.Default) {
         try {
            val selectedStationId = stations.getClosestStationId(pos)

            val weatherFile = forecastDownload.replace("xxxxx", selectedStationId)
            Log.i(logTag, "downloading forecast for ${stations.selectedStation?.name} file $weatherFile")

            val tmpFile = File(tmpFileName)
            if (tmpFile.exists()) tmpFile.delete()

            downloadFile(URL(weatherFile), tmpFileName)
            File(dir).listFiles()?.filter { it.extension == "kml" }?.forEach { file -> file.delete() }

            if (tmpFile.exists()) {
               Log.i(logTag, "retrieving forecasts")
               var forecastFile = ""

               withContext(Dispatchers.IO) {
                  ZipFile(tmpFile).use { zip ->
                     zip.entries().asSequence().forEach { entry ->
                        forecastFile = "$dir/${entry.name}"

                        zip.getInputStream(entry).use { input ->
                           File(forecastFile).outputStream().use { output ->
                              input.copyTo(output)
                           }
                        }
                     }
                  }
               }

               Log.i(logTag, "Scanning forecast file")
               forecast = Forecast(forecastFile)
               Log.i(logTag, "Scanning finished")

            } else {
               msg("Error: Cannot download weather info from server")
            }
         }
         catch (e: Exception) {
            nkHandleException(logTag, ctx.getString(R.string.failure_getting_weather_dwd_data), e, msg)
         }
      }
   }

   override fun loadPreferences(preferences: SharedPreferences) {
      Forecast.loadSharedPreferences(preferences)
   }


   override fun storePreferences(edit: SharedPreferences.Editor) {
      Forecast.storeSharedPreferences(edit)
   }


   override fun updateMap(mapView: OsmMap, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {
      if (mapMode == mapModeToBeUpdated && stations.size > 0) {

         if (stationMarker.isEmpty() || mapView.outerBoundingBox == null || mapView.checkOutsideOfOuterBoundary()) {
            mapView.outerBoundingBox = mapView.boundingBox.increaseByScale(2.0f)

            if (stationMarker.isNotEmpty()) {
               stationMarker.forEach { entry -> mapView.overlays.remove(entry.value) }
               stationMarker.clear()
            }

            stations.getStationsInWindow(mapView.outerBoundingBox!!).forEach { entry ->
               val marker = NkMarker(
                  mapview = mapView,
                  clickFunc = { lat: Double, lon: Double ->
                     downloadNearestStation(ExtendedLocation(lat, lon), snackbar)
                  }
               ).apply {
                  icon = if (entry.key == stations.selectedStationIndex) selectedStationIcon else stationIcon
                  position = entry.value.gp
                  title = ctx.getString(
                     R.string.station_elevation,
                     entry.value.name,
                     entry.value.elevation,
                     ExtendedLocation.applyDistance(entry.value.loc.distanceTo(location))
                  )
               }

               stationMarker[entry.key] = marker
            }

         } else {
            stationMarker.forEach { entry ->
               entry.value.apply {
                  icon = stationIcon
                  isEnabled = true
               }
            }

            if (stationMarker.isNotEmpty() && stations.selectedStationIndex in stationMarker.keys)
               stationMarker[stations.selectedStationIndex]!!.icon = selectedStationIcon
         }

      } else {
         stationMarker.forEach { entry -> entry.value.remove(mapView) }
         stationMarker.clear()
      }
   }
}