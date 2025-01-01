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
 * File: GribFile.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.grib

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.nksoftware.library.utilities.nkHandleException
import com.nksoftware.library.location.ExtendedLocation
import org.osmdroid.util.BoundingBox
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Calendar
import kotlin.collections.get
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import com.nksoftware.library.R
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.map.NkMarker
import com.nksoftware.library.map.getWindSpeedIcon
import com.nksoftware.library.map.windIcons
import com.nksoftware.library.route.routeActiveKey
import com.nksoftware.library.route.sailingModeKey
import com.nksoftware.library.route.tackAngleKey
import com.nksoftware.library.utilities.convertUnits
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Date
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty


val logTag = "Grib Parser"


class GpsGridPoint(val lat: Float, val lon: Float, val value: Float, val value2: Float = 0f) {

   fun inBoundingBox(box: BoundingBox): Boolean {
      return box.contains(lat.toDouble(), lon.toDouble())
   }
}


abstract class GribFileEntry {

   abstract fun getParameter(): String
   abstract fun getReferenceTime(): Calendar
   abstract fun getGridInfo(type: GribFile.GridInfo): Float
   abstract fun getGridValues(): MutableList<GpsGridPoint>
}


class V1GribFileEntry(inp: InputStream) : GribFileEntry() {

   private var section0: V1Section0
   private var section1: V1Section1
   private var section2: V1Section2? = null
   private var section3: V1Section3? = null
   private var section4: V1Section4

   init {
      val data = ByteBuffer.allocate(8)
      inp.read(data.array())

      section0 = V1Section0(data)
      section1 = V1Section1(getNextSection(inp))

      if (section1.section2)
         section2 = V1Section2(getNextSection(inp))

      if (section1.section3)
         section3 = V1Section3(getNextSection(inp))

      section4 = V1Section4(getNextSection(inp))

      checkPattern(inp, "7777")
   }


   private fun getNextSection(inp: InputStream): ByteBuffer {
      val start = ByteBuffer.allocate(4)
      inp.read(start.array())

      val length = (start.getShort(0).toUShort().toInt() shl 8) + start.get(2).toUByte().toInt()
      val data = ByteBuffer.allocate(length)

      data.put(start.array(), 0, 4)
      inp.read(data.array(), 4, length - 4)

      return data
   }


   private fun checkPattern(input: InputStream, pattern: String) {
      val pbuf = ByteBuffer.allocate(pattern.length)

      if (input.read(pbuf.array()) > 0) {
         when (val str = String(pbuf.array())) {
            "GRIB" -> Log.d(logTag, "Grib entry found")
            "7777" -> Log.d(logTag, "End of GRIB file found")
            else   -> throw Exception("Wrong pattern: $str found - expected: $pattern")
         }
      }
   }


   override fun getParameter(): String {
      return section1.getParameter()
   }


   override fun getReferenceTime(): Calendar {
      return section1.referenceTime
   }


   override fun getGridInfo(type: GribFile.GridInfo): Float {
      return when (type) {
         GribFile.GridInfo.MinLat -> (section2!!.gridDefinition.firstLat) / 1000.0f
         GribFile.GridInfo.MinLon -> (section2!!.gridDefinition.firstLon) / 1000.0f
         GribFile.GridInfo.MaxLat -> (section2!!.gridDefinition.lastLat) / 1000.0f
         GribFile.GridInfo.MaxLon -> (section2!!.gridDefinition.lastLon) / 1000.0f
         GribFile.GridInfo.NoLat  -> (section2!!.gridDefinition.noLat).toFloat()
         GribFile.GridInfo.NoLon  -> (section2!!.gridDefinition.noLon).toFloat()
         GribFile.GridInfo.LatInc -> (section2!!.gridDefinition.latIncrement) / 1000.0f
         GribFile.GridInfo.LonInc -> (section2!!.gridDefinition.lonIncrement) / 1000.0f
      }
   }


   override fun getGridValues(): MutableList<GpsGridPoint> {
      return section2!!.getGridData(section4, section1.decimalScaleFactor)
   }
}


class V2GribFileEntry(inp: InputStream) : GribFileEntry() {

   private var section0: V2Section0
   private var section1: V2Section1? = null
   private var section2: V2Section2? = null
   private var section3: V2Section3? = null
   private var section4: V2Section4? = null
   private var section5: V2Section5? = null
   private var section6: V2Section6? = null
   private var section7: V2Section7? = null

   init {
      val data = ByteBuffer.allocate(16)

      inp.read(data.array())

      section0 = V2Section0(data)
      var nextSection = getNextSection(inp)

      while (nextSection.array().size > 4) {
         when (nextSection.get(4).toInt()) {
            1    -> section1 = V2Section1(nextSection)
            2    -> section2 = V2Section2(nextSection)
            3    -> section3 = V2Section3(nextSection)
            4    -> section4 = V2Section4(nextSection)
            5    -> section5 = V2Section5(nextSection)
            6    -> section6 = V2Section6(nextSection)
            7    -> section7 = V2Section7(nextSection)
            else -> {}
         }
         nextSection = getNextSection(inp)
      }
   }

   private fun getNextSection(inp: InputStream): ByteBuffer {
      val start = ByteBuffer.allocate(4)
      inp.read(start.array())

      if (String(start.array()) in listOf("GRIB", "7777"))
         return start
      else {
         val length = start.getInt(0)
         val data = ByteBuffer.allocate(length)

         data.put(start.array(), 0, 4)
         inp.read(data.array(), 4, length - 4)

         return data
      }
   }


   override fun getParameter(): String {
      return section4!!.getParameter(section0.discipline)
   }

   override fun getReferenceTime(): Calendar {
      return section4!!.getTime(section1!!.referenceTime)
   }

   override fun getGridInfo(type: GribFile.GridInfo): Float {
      return section3!!.getGridInfo(type)
   }

   override fun getGridValues(): MutableList<GpsGridPoint> {
      return section3!!.getGridData(section5!!, section7!!)
   }
}


class GribFile(private val ctx: Context, val mapMode: Int) : DataModel(mapMode) {

   var showGrib by mutableStateOf(false)
   var gribFileName by mutableStateOf("")

   val gribFileLoaded: Boolean
      get() = gribFileName != ""

   private var gribEntries: MutableMap<Calendar, MutableMap<String, GribFileEntry>> = mutableMapOf()

   var minLat: Double? by mutableStateOf(null)
   var minLon: Double? by mutableStateOf(null)
   var maxLat: Double? by mutableStateOf(null)
   var maxLon: Double? by mutableStateOf(null)
   var latPts: Int? by mutableStateOf(null)
   var lonPts: Int? by mutableStateOf(null)

   private var gribValues: MutableMap<Calendar, MutableMap<String, MutableList<GpsGridPoint>?>> = mutableMapOf()

   var gribTimeslotList by mutableStateOf(listOf<Calendar>())
   var actualGribTimeslot: Calendar? by mutableStateOf(null)

   var gribParameterList by mutableStateOf(mutableListOf<String>())
   var actualGribParameter by mutableStateOf("")

   enum class GridInfo { MinLat, MinLon, MaxLat, MaxLon, NoLat, NoLon, LatInc, LonInc }

   private val gribMarker: MutableList<NkMarker> = mutableListOf()
   private var actualGribTimeSlot: Date? by mutableStateOf(null)


   fun delete() {
      showGrib = false
      gribFileName = ""

      gribParameterList = mutableListOf()
      actualGribParameter = ""

      gribTimeslotList = listOf()
      actualGribTimeslot = null

      gribEntries.clear()
      gribValues.clear()

      minLat = null
      maxLat = null
      minLon = null
      maxLon = null
      latPts = null
      lonPts = null
   }


   fun setParameter(ind: Int) {
      actualGribParameter = gribParameterList[ind]
   }


   fun setTimeslot(slot: Int) {
      actualGribTimeslot = gribTimeslotList[slot]
   }


   fun getSize(): Int? {
      return gribValues[actualGribTimeslot]?.get(actualGribParameter)?.size
   }


   fun getTimeslotStr(): String {
      if (actualGribTimeslot == null)
         return ""
      else {
         return ExtendedLocation.getTimeStr(actualGribTimeslot!!.time.time, "dd.MM. HH:mm")
      }
   }


   fun getFirstTime(): String {
      return if (gribValues.keys.size > 0)
         ExtendedLocation.getTimeStr(gribValues.keys.min().time.time, "dd.MM.yyyy HH:mm")
      else ""
   }


   fun getLastTime(): String {
      return if (gribValues.keys.size > 0)
         ExtendedLocation.getTimeStr(gribValues.keys.max().time.time, "dd.MM.yyyy HH:mm")
      else ""
   }


   fun getGridInfo(type: GridInfo): Float? {
      return gribEntries[actualGribTimeslot]?.get(actualGribParameter)?.getGridInfo(type)
   }


   fun getGridValues(): List<GpsGridPoint>? {
      return gribValues[actualGribTimeslot]?.get(actualGribParameter)
   }


   private fun scanV1(uri: Uri) {
      val inp: InputStream? = ctx.contentResolver.openInputStream(uri)

      if (inp != null) {
         while (inp.available() >= 8) {
            val entry = V1GribFileEntry(inp)

            val parameter = entry.getParameter()
            val time = entry.getReferenceTime()

            if (!gribEntries.containsKey(time))
               gribEntries[time] = mutableMapOf()

            val parameterEntry = gribEntries[time]

            if (parameterEntry!!.contains(parameter))
               Log.w(logTag, "Duplicate parameter entry in grid data")
            else
               parameterEntry[parameter] = entry
         }
      }

      inp?.close()
   }


   private fun scanV2(uri: Uri) {

      val inp: InputStream? = ctx.contentResolver.openInputStream(uri)

      if (inp != null) {
         while (inp.available() >= 8) {
            val entry = V2GribFileEntry(inp)

            val parameter = entry.getParameter()
            val time = entry.getReferenceTime()

            if (!gribEntries.containsKey(time))
               gribEntries[time] = mutableMapOf()

            val parameterEntry = gribEntries[time]

            if (parameterEntry!!.contains(parameter))
               Log.w(logTag, "Duplicate parameter entry in grid data")
            else
               parameterEntry[parameter] = entry
         }
      }

      inp?.close()
   }


   fun scan(uri: Uri, loc: Location, message: (str: String) -> Unit) {
      var version = 0

      val inp: InputStream? = ctx.contentResolver.openInputStream(uri)
      val header = ByteBuffer.allocate(4)
      val specification = ByteBuffer.allocate(4)

      if (inp?.read(header.array())!! > 0) {
         if (String(header.array()) == "GRIB") {
            inp.read(specification.array())
            version = specification.get(3).toInt()
         } else {
            message(ctx.getString(R.string.doesn_t_look_like_a_valid_grib_file))
         }
      }

      inp.close()
      gribEntries.clear()
      gribValues.clear()

      when (version) {
         1    -> scanV1(uri)
         2    -> scanV2(uri)
         else -> {
            message(ctx.getString(R.string.grib_file_version_not_supported))
         }
      }

      try {
         gribTimeslotList = gribEntries.keys.toList()
         gribParameterList = gribEntries.map { e -> e.value.keys.toList() }.flatten().toSet().toMutableList()

         val windPar = listOf("wind (u)", "wind (v)")
         if (gribParameterList.containsAll(windPar)) {
            gribParameterList.removeAll(windPar)
            gribParameterList.add("wind")
         }

         for ((time, parValue) in gribEntries) {
            for ((parameter, entry) in parValue) {
               try {
                  if (time !in gribValues.keys)
                     gribValues[time] = mutableMapOf()

                  Log.i(logTag, "Compute grid values for parameter($parameter) at time(${time.time})")
                  gribValues[time]!![parameter] = entry.getGridValues()
               }
               catch (e: Exception) {
                  nkHandleException(
                     logTag, ctx.getString(
                        R.string.exception_scanning_grib_v2_file
                     ), e
                  )
               }
            }

            if ("wind (u)" in parValue.keys && "wind (v)" in parValue.keys) {
               val uVal = gribValues[time]?.get("wind (u)")
               val vVal = gribValues[time]?.get("wind (v)")
               val windList = mutableListOf<GpsGridPoint>()

               if (uVal != null) {
                  uVal.forEachIndexed { i, uv ->
                     val speed = 3.6f * sqrt(vVal!![i].value.pow(2) + uVal[i].value.pow(2))
                     val direction =
                        (180.0 + (atan2(uVal[i].value.toDouble(), vVal[i].value.toDouble()) * (180.0 / PI))) % 360.0

                     windList.add(GpsGridPoint(vVal[i].lat, vVal[i].lon, speed, direction.toFloat()))
                  }
               }

               gribValues[time]?.set("wind", windList)
            }
         }

         setTimeslot(0)
         setParameter(0)

         minLat = getGridInfo(GridInfo.MinLat)?.toDouble()
         maxLat = getGridInfo(GridInfo.MaxLat)?.toDouble()
         minLon = getGridInfo(GridInfo.MinLon)?.toDouble()
         maxLon = getGridInfo(GridInfo.MaxLon)?.toDouble()
         latPts = getGridInfo(GridInfo.NoLat)?.toInt()
         lonPts = getGridInfo(GridInfo.NoLon)?.toInt()

         if (Calendar.getInstance().time > gribValues.keys.max().time)
            message(ctx.getString(R.string.warning_current_time_ist_outside_of_grib_time_window))

         if (!((loc.latitude in minLat!!..maxLat!!) && (loc.longitude in minLon!!..maxLon!!)))
            message(ctx.getString(R.string.warning_current_location_outside_of_grib_window))

         val documentFile = DocumentFile.fromSingleUri(ctx, uri)
         gribFileName = documentFile?.name.toString()

      }
      catch (e: Exception) {
         nkHandleException(
            logTag, ctx.getString(
               R.string.exception_scanning_grib_file
            ), e
         )
      }

      message(ctx.getString(R.string.grib_file_successfully_scanned))
   }


   override fun updateMap(mapView: MapView, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {
      if (mapMode == mapModeToBeUpdated && showGrib) {
         val gribValues = getGridValues()

         if (gribValues != null) {
            if (gribValues.isNotEmpty() && ((actualGribParameter != actualGribParameter) ||
                                            (actualGribTimeSlot != actualGribTimeslot))
            ) {
               if (gribValues.size != gribMarker.size) {
                  for (marker in gribMarker) marker.remove(mapView)
                  gribMarker.clear()

                  gribValues.forEach { _ -> gribMarker.add(NkMarker(mapView).apply { isEnabled = true }) }
               }

               if (actualGribParameter == "wind") {

                  gribValues.forEachIndexed { i, value ->
                     gribMarker[i].apply {
                        closeInfoWindow()
                        position = GeoPoint(value.lat.toDouble(), value.lon.toDouble())
                        title = ctx.getString(R.string.wind_speed_1f_km_h_wind_dir_0f, value.value, value.value2)
                        icon = ContextCompat.getDrawable(ctx, windIcons[getWindSpeedIcon(value.value)])
                        rotation = -90f - value.value2
                        isEnabled = true
                     }
                  }

               } else {

                  gribValues.forEachIndexed { i, gp ->
                     gribMarker[i].apply {
                        isEnabled = true
                        position = GeoPoint(gp.lat.toDouble(), gp.lon.toDouble())
                        rotation = 0f
                        setTextIcon("%.1f".format(convertUnits(actualGribParameter, gp.value)))
                     }
                  }
               }

               actualGribParameter = actualGribParameter
               actualGribTimeSlot = actualGribTimeslot?.time
            }
         }

      } else
         gribMarker.forEach { it.apply { isEnabled = false } }
   }
}
