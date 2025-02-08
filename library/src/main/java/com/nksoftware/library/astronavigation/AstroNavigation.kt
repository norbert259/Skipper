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
 * File: AstroNavigation.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.astronavigation

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.nksoftware.library.R
import com.nksoftware.library.composables.SingleSelectList
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.map.NkMarker
import com.nksoftware.library.map.NkPolyline
import com.nksoftware.library.map.OsmMapView
import com.nksoftware.library.sun.Sun
import org.osmdroid.util.GeoPoint
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan


const val logTag = "Astronavigation"

const val EarthRadius = 6371000.0
const val MilliSecondsPerDay = 86400000.0
const val EccentricityPerDay = 0.000000042 / 365.25
const val EclipticPerDay = 0.4682 / (3600.0 * 365.25)

const val astronavFixTypeKey = "AstronavFixTypeKey"
const val astronavEyeLevelKey = "AstronavEyeLevel"
const val astronavSunEdgeKey = "AstronavSunEdge"
const val astronavIndexMinusKey = "AstronavIndexMinus"
const val astronavIndexCorrectionKey = "AstronavIndexCorrection"

enum class SunEdge { Top, Middle, Bottom }
enum class FixType { FixAtNoon, TwoPtFix }

class Almanac(
   val grt: Double,
   val delta: Double,
)

class Fix(
   val date: Calendar,
   val measuredHeight: Float,
   val eyeLevel: Float,
   val indexError: Float,
   val sunEdge: Int,
   val grt: Double,
   val delta: Double
) {

   var course: Int by mutableIntStateOf(0)
   var speed: Float by mutableFloatStateOf(0f)

   val sunCorrection = mapOf(
      SunEdge.Bottom to 16.0 / 60.0,
      SunEdge.Middle to 0.0,
      SunEdge.Top to -16.0 / 60.0
   )

   private val addOnCorrection = listOf(0.3, 0.2, 0.1, 0.0, -0.2, -0.2, -0.2, -0.2, -0.1, 0.1, 0.2, 0.3)

   val timeStr: String
      get() = ExtendedLocation.getTimeStr(date.time.time)

   val heightStr: String
      get() = ExtendedLocation.convertCoordinate(measuredHeight.toDouble(), diff = true)

   private val eyeLevelCorrection: Double
      //      get() = 1.777 * sqrt(height) / 60.0
      get() = (12.0 / 13.0) * Math.toDegrees(acos(EarthRadius / (EarthRadius + eyeLevel)))

   private val refractionCorrection: Double
      get() = if (measuredHeight > 5.0)
         (55.0 * tan(Math.toRadians(90.0 - measuredHeight)) -
          0.055 * tan(Math.toRadians(90.0 - measuredHeight)).pow(3)) / 3600.0 else 10.0 / 3600.0

   private val sunEdgeCorrection: Double
      get() = sunCorrection[SunEdge.entries.toList()[sunEdge]] ?: 0.0

   private val totalCorrection: Double
      get() = sunEdgeCorrection - refractionCorrection - eyeLevelCorrection +
              (addOnCorrection[date.get(Calendar.MONTH)] / 60.0)

   val finalHeight: Double
      get() = measuredHeight + totalCorrection + indexError

   val finalHeightRad: Double
      get() = Math.toRadians(finalHeight)

   val finalHeightStr: String
      get() = ExtendedLocation.convertCoordinate(finalHeight, diff = true)

   val finalLatitude: Double
      get() = 90.0 - finalHeight - delta

   val finalLatitudeStr: String
      get() = ExtendedLocation.convertCoordinate(finalLatitude)

   val grtLongitude: Double
      get() = if (grt >= 0 && grt < 180.0) -grt else (360f - grt)

   val grtLongitudeStr: String
      get() = ExtendedLocation.convertCoordinate(grtLongitude, vertical = false)

   val grtStr: String
      get() = ExtendedLocation.convertCoordinate(grt, diff = true)

   val dStr: String
      get() = ExtendedLocation.convertCoordinate(delta, diff = true)

   val geoPoint: GeoPoint
      get() = GeoPoint(delta, grtLongitude)

   fun sailingOffsetGp(at: Calendar?): GeoPoint {
      if (at == null) {
         return geoPoint
      } else {
         val timeDifference = (at.time.time - date.time.time) / 1000
         val distance = timeDifference * (speed * 1.852) / 3.6

         return geoPoint.destinationPoint(distance, course.toDouble())
      }
   }
}


class AstroNavigation(
   val sun: Sun,
   mapMode: Int
) : DataModel(mapMode) {

   val fixType = SingleSelectList(FixType.entries.map { it.toString() }, 0)

   var eyeLevel by mutableFloatStateOf(2f)

   val sunEdgeIndex = SingleSelectList(SunEdge.entries.map { it.toString() }, 2)
   val sunEdge: SunEdge
      get() = SunEdge.entries.toList()[sunEdgeIndex.index]

   var indexMinus by mutableStateOf(false)
   val indexSign: Int
      get() = if (indexMinus) -1 else 1

   var indexErrorDegree by mutableIntStateOf(0)
   var indexErrorMinutes by mutableIntStateOf(0)
   var indexErrorSeconds by mutableIntStateOf(0)

   val indexError: Float
      get() = indexSign * ((abs(indexErrorDegree.toFloat()) +
                            indexErrorMinutes.toFloat() / 60f +
                            indexErrorSeconds.toFloat() / 3600f))

   var measurementDegree by mutableIntStateOf(0)
   var measurementMinutes by mutableIntStateOf(0)
   var measurementSeconds by mutableIntStateOf(0)

   val measurement: Float
      get() = measurementDegree.toFloat() +
              measurementMinutes.toFloat() / 60f +
              measurementSeconds.toFloat() / 3600f

   private val refDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

   val fixes = mutableStateListOf<Fix>()
   var selectedFix by mutableIntStateOf(0)

   var latitude: Double? = null
   var longitude: Double? = null

   val positionGp: GeoPoint?
      get() = if (latitude != null && longitude != null) GeoPoint(latitude!!, longitude!!) else null

   private var sunPositionMarker: NkMarker? = null
   private var computedPositionMarker: NkMarker? = null

   private val astronavMarkers: MutableList<NkMarker> = mutableListOf()
   private val astronavCircles: MutableList<NkPolyline> = mutableListOf()


   init {
      refDate.timeZone = TimeZone.getTimeZone("UTC")
      refDate.set(2000, 0, 1, 12, 0, 0)
   }


   override fun loadPreferences(preferences: SharedPreferences) {
      fixType.index = preferences.getInt(astronavFixTypeKey, 0)
      sunEdgeIndex.index = preferences.getInt(astronavSunEdgeKey, 2)
      eyeLevel = preferences.getFloat(astronavEyeLevelKey, 2.0f)
      indexMinus = preferences.getBoolean(astronavIndexMinusKey, false)

      val indexError = preferences.getFloat(astronavIndexCorrectionKey, 0.0f)
      indexErrorDegree = abs(indexError.toInt())
      indexErrorMinutes = ((abs(indexError) - indexErrorDegree) * 60).toInt()
      indexErrorSeconds = ((((abs(indexError) - indexErrorDegree) * 60)
                            - indexErrorMinutes) * 60).toInt()
   }


   override fun storePreferences(edit: SharedPreferences.Editor) {
      edit.putInt(astronavFixTypeKey, fixType.index)
      edit.putInt(astronavSunEdgeKey, sunEdgeIndex.index)
      edit.putFloat(astronavEyeLevelKey, eyeLevel)
      edit.putBoolean(astronavIndexMinusKey, indexMinus)
      edit.putFloat(astronavIndexCorrectionKey, indexError)
   }


   override fun updateMap(
      mapView: OsmMapView,
      mapMode: Int,
      location: ExtendedLocation,
      snackbar: (String) -> Unit
   ) {
      val sunFixIcon = ContextCompat.getDrawable(mapView.ctx, R.drawable.baseline_sunny_24)
      val sunIcon = ContextCompat.getDrawable(mapView.ctx, R.drawable.sun_black)
      val redSunIcon = ContextCompat.getDrawable(mapView.ctx, R.drawable.sun_red)

      if (sunPositionMarker == null) {
         sunPositionMarker = NkMarker(mapView).apply {
            isEnabled = false
            icon = sunIcon
         }

         computedPositionMarker = NkMarker(mapView).apply {
            isEnabled = false
            icon = redSunIcon
         }
      }

      if (mapMode == mapModeToBeUpdated) {
         val pt = getCurrentSunPosition()

         sunPositionMarker!!.apply {
            isEnabled = true
            position = pt
            title = "${mapView.ctx.getString(R.string.lat, ExtendedLocation.convertCoordinate(pt.latitude))}\n" +
                    mapView.ctx.getString(R.string.lon, ExtendedLocation.convertCoordinate(pt.longitude, vertical = false))
         }

         if (fixes.size != astronavMarkers.size) {
            for (marker in astronavMarkers) mapView.overlays.remove(marker)
            for (circle in astronavCircles) mapView.overlays.remove(circle)

            astronavMarkers.clear()
            astronavCircles.clear()

            fixes.forEach { _ ->
               astronavMarkers.add(NkMarker(mapView).apply {
                  isEnabled = true
                  icon = sunFixIcon
               })

               astronavCircles.add(NkPolyline(mapView, color = Color.YELLOW, width = 5.0f))
            }
         }

         fixes.forEachIndexed(action = { i, fix ->
            val gp = fix.sailingOffsetGp(if (i == 0) null else fixes.first().date)

            astronavMarkers[i].apply {
               isEnabled = true
               position = gp
               title =
                  "${mapView.ctx.getString(R.string.lat, ExtendedLocation.convertCoordinate(gp.latitude))}\n" +
                          mapView.ctx.getString(R.string.lon, ExtendedLocation.convertCoordinate(gp.longitude, vertical = false))
            }

            if (fix.measuredHeight > 0.0)
               astronavCircles[i].setCircle(
                  GeoPoint(gp.latitude, gp.longitude), Math.toRadians(90.0 - fix.finalHeight) * 6378000, 1
               )
         })

         if (fixType.index == FixType.TwoPtFix.ordinal && fixes.size > 1) {
            computedPositionMarker!!.apply {
               isEnabled = true
               position = positionGp
            }
         } else {
            computedPositionMarker!!.apply { isEnabled = false }
         }

      } else {
         sunPositionMarker!!.apply {
            isEnabled = false
         }

         computedPositionMarker!!.apply {
            isEnabled = false
         }

         astronavMarkers.forEach { it.apply { isEnabled = false } }
         astronavCircles.forEach { it.apply { isEnabled = false } }
      }
   }


   fun getAlmanac(now: Calendar): Almanac {
      val daysToRefDate = (now.time.time / MilliSecondsPerDay) - (refDate.time.time / MilliSecondsPerDay)
      val e = 0.016709 - EccentricityPerDay * daysToRefDate

      val ecliptic = 23.4391667 - EclipticPerDay * daysToRefDate
      val eclipticrad = Math.toRadians(ecliptic)

      val perihelion = 282.9400 + 0.017192 * daysToRefDate / 365.25
      val perihelionRad = Math.toRadians(perihelion)

      val longitudeFromPerihel = 280.4656 + 360.00769 * daysToRefDate / 365.25
      val longitudeFromPerielRad = Math.toRadians(longitudeFromPerihel)

      val M = longitudeFromPerielRad - perihelionRad
      val C = (2 * e - e.pow(3) / 4.0) * sin(M) + 1.25 * e.pow(2) * sin(2 * M) +
              13.0 / 12.0 * e.pow(3) * sin(3 * M)
      val trueLength = longitudeFromPerielRad + C

      val g = atan(
         (tan(longitudeFromPerielRad) - tan(trueLength) * cos(eclipticrad)) /
         (1.0 + tan(longitudeFromPerielRad) * tan(trueLength) * cos(eclipticrad))
      )

      val ot = (now.get(Calendar.HOUR_OF_DAY) * 3600.0 + now.get(Calendar.MINUTE) * 60.0 +
                now.get(Calendar.SECOND)) / (24.0 * 3600.0)

      val grtRad = if (ot * 24 + 12 > 24)
         g + Math.toRadians(15.0) * (ot * 24 - 12)
      else
         g + Math.toRadians(15.0) * (ot * 24 + 12)
      val deltaRad = asin(sin(trueLength) * sin(eclipticrad))

      return Almanac(grt = Math.toDegrees(grtRad), delta = Math.toDegrees(deltaRad))
   }


   fun previousFix() {
      selectedFix = max(selectedFix - 1, 0)
   }


   fun nextFix() {
      selectedFix = min(selectedFix + 1, fixes.size - 1)
   }

   fun deleteFix() {
      fixes.removeAt(selectedFix)
      previousFix()
   }


   fun addFix(ctx: Context, msg: (String) -> Unit) {
      if (measurement < 5.0) {
         msg(ctx.getString(R.string.please_enter_angle_5))

      } else {
         val referenceTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
         val grtDelta = getAlmanac(referenceTime)

         fixes.add(
            Fix(
               date = referenceTime,
               measuredHeight = measurement,
               eyeLevel = eyeLevel,
               indexError = indexError,
               sunEdge = sunEdgeIndex.index,
               grt = grtDelta.grt,
               delta = grtDelta.delta
            )
         )

         if (fixType.index == FixType.TwoPtFix.ordinal && fixes.size == 2)
            compute2Fixes(fix1 = fixes[0], fix2 = fixes[1])
      }
   }


   fun addAutoFix(loc: Location, snackBar: (String) -> Unit, ) {
      val referenceTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))

      val grtDelta = getAlmanac(referenceTime)
      val ms = sun.getSunAltitude(loc, referenceTime.time, eyeLevel.toDouble(), sunEdge).toFloat()

      if (ms > 5.0f) {
         fixes.add(
            Fix(
               date = referenceTime,
               measuredHeight = ms,
               eyeLevel = eyeLevel,
               indexError = indexError,
               sunEdge = sunEdgeIndex.index,
               grt = grtDelta.grt,
               delta = grtDelta.delta
            )
         )
      } else
         snackBar("Error: Sun altitude < 5° - no fix added")

      if (fixType.index == FixType.TwoPtFix.ordinal && fixes.size == 2)
         compute2Fixes(fix1 = fixes[0], fix2 = fixes[1])
   }


   fun getCurrentSunPosition(): GeoPoint {
      val referenceTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
      val almanac = getAlmanac(referenceTime)

      val longitude = if (almanac.grt >= 0 && almanac.grt < 180.0) -almanac.grt else (360f - almanac.grt)
      return GeoPoint(almanac.delta, longitude)
   }


   fun compute2Fixes(fix1: Fix, fix2: Fix) {
      val delta1 = Math.toRadians(fix1.delta)
      val delta2 = Math.toRadians(fix2.delta)

      val theta = Math.toRadians(fix2.grt - fix1.grt)
      Log.i(logTag, "compute2Fixes theta: %.4f".format(theta))

      val F = atan(tan(delta2) / cos(theta))
      Log.i(logTag, "compute2Fixes F: %.4f".format(F))

      var V = atan(cos(F) * tan(theta) / sin(F - delta1))
      if ((F - delta1) <= 0) V += Math.PI
      Log.i(logTag, "compute2Fixes V: %.4f".format(V))

      val W = acos(
         (cos(V) * tan(fix1.finalHeightRad) / tan(F - delta1)) *
         (((sin(fix2.finalHeightRad) * sin(F)) / (sin(fix1.finalHeightRad) * sin(delta2) * cos(F - delta1))) - 1)
      )
      Log.i(logTag, "compute2Fixes W: %.4f".format(W))

      val G = atan(tan(fix1.finalHeightRad) / cos(V - W))
      Log.i(logTag, "compute2Fixes G: %.4f".format(G))

      var tau = atan((cos(G) * tan(V - W)) / sin(G - delta1))
      if ((G - delta1) < 0) tau -= Math.PI
      Log.i(logTag, "compute2Fixes tau: %.4f".format(tau))

      val phi = atan(cos(tau) * cos(G - delta1) / sin(G - delta1))
      Log.i(logTag, "compute2Fixes phi: %.4f".format(phi))

//      val q = acos(
//         sin(delta1) * sin(delta2) +
//         cos(delta1) * cos(delta2) * cos(theta)
//      )
//
//      val sigma = acos(
//         (sin(delta2) - sin(delta1) * cos(q)) /
//         (cos(delta1) * sin(q))
//      )
//
//      val zeta = acos(
//         (sin(fix2.finalHeightRad) - sin(fix1.finalHeightRad) * cos(q)) /
//         (cos(fix1.finalHeightRad * sin(q)))
//      )
//
//      val phi = asin(
//         sin(delta1) * sin(fix1.finalHeightRad) +
//         cos(delta1) * cos(fix1.finalHeightRad) * cos(sigma - zeta)
//      )
//
//      val tau = acos(
//         (sin(fix1.finalHeightRad) - sin(delta1) * sin(phi)) /
//         (cos(delta1) * cos(phi))
//      )

      latitude = Math.toDegrees(phi)

      val lambda = Math.toRadians(fix1.grt) + tau
      Log.i(logTag, "compute2Fixes lambda: %.4f".format(lambda))

      var lha = Math.toRadians(fix1.grt) - lambda
      if (lha < 0) lha += 2 * Math.PI
      if (lha > 2 * Math.PI) lha -= 2 * Math.PI
      Log.i(logTag, "compute2Fixes lha: %.4f".format(lha))

      val timeDiff = fix2.date.time.time - fix1.date.time.time
      val dmg = fix1.speed * timeDiff / 3600000.0
      Log.i(logTag, "dmg: %.4f".format(dmg))

      var z = acos((sin(delta1) - sin(fix1.finalHeightRad) * sin(phi)) / (cos(fix1.finalHeightRad) * cos(phi)))
      if (lha < 2 * Math.PI) z = 2 * Math.PI - z
      Log.i(logTag, "compute2Fixes z: %.4f".format(z))

      val hs = Math.toRadians(dmg / 60.0) * cos(z - Math.toRadians(fix1.course.toDouble())) + fix1.finalHeightRad
      Log.i(logTag, "compute2Fixes hs: %.4f".format(hs))

      val Ws = acos(
         (cos(V) * tan(hs) / tan(F - delta1)) *
         (((sin(fix2.finalHeightRad) * sin(F)) / (sin(hs) * sin(delta2) * cos(F - delta1))) - 1)
      )
      Log.i(logTag, "compute2Fixes Ws: %.4f".format(Ws))

      val Gs = atan(tan(hs) / cos(V - Ws))
      Log.i(logTag, "compute2Fixes Gs: %.4f".format(Gs))

      var taus = atan((cos(Gs) * tan(V - Ws)) / sin(Gs - delta1))
      if ((Gs - delta1) < 0) taus -= Math.PI
      Log.i(logTag, "compute2Fixes taus: %.4f".format(taus))

      val phis = atan(cos(taus) * cos(Gs - delta1) / sin(Gs - delta1))
      Log.i(logTag, "compute2Fixes phis: %.4f".format(phis))

      val lambdas = Math.toRadians(fix1.grt) + taus
      Log.i(logTag, "compute2Fixes lambdas: %.4f".format(lambdas))

      val lambdaFinal = if (lambdas < Math.PI) -lambdas else 2 * Math.PI - lambdas
      longitude = Math.toDegrees(lambdaFinal)
   }
}