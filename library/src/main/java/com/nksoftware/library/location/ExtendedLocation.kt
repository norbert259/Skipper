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
 * File: ExtendedLocation.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.location

import android.content.SharedPreferences
import android.hardware.GeomagneticField
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nksoftware.library.composables.SingleSelectList
//import com.nksoftware.skipper.core.TrackPoint
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


const val selectedTimeZone = "SelectedTimeZone"
const val selectedDimensionKey = "SelectedDimension"
const val selectedCoordinateFormatKey = "SelectedCoordinateFormat"
const val magneticHeadingKey = "MagneticHeading"


class ExtendedLocation(currLoc: Location) : Location(currLoc) {

   companion object Converters {

      val timezone = SingleSelectList<String>(listOf("local", "UTC"), 0)
      val dimensions = SingleSelectList<String>(listOf("metric", "nautical"), 0)

      val speedDimension: String
         get() = if (dimensions.index == 1) "kn" else "km/h"

      val distanceDimension: String
         get() = if (dimensions.index == 1) "sm" else "km"

      val coordinatesFormat = SingleSelectList(listOf("DD.ddd", "DD MM.mm", "DD MM SS"), 2)

      var magneticDeviation by mutableStateOf(GeomagneticField(0.0f, 0.0f, 0.0f, 0))
      var magneticHeading by mutableStateOf(false)

      val declination: Float
         get() = magneticDeviation.declination


      fun loadSharedPreferences(preferences: SharedPreferences) {
         timezone.index = preferences.getInt(selectedTimeZone, 1)
         dimensions.index = preferences.getInt(selectedDimensionKey, 1)
         coordinatesFormat.index = preferences.getInt(selectedCoordinateFormatKey, 0)
         magneticHeading = preferences.getBoolean(magneticHeadingKey, false)
      }

      fun storeSharedPreferences(edit: SharedPreferences.Editor) {
         edit.putInt(selectedTimeZone, timezone.index)
         edit.putInt(selectedDimensionKey, dimensions.index)
         edit.putInt(selectedCoordinateFormatKey, coordinatesFormat.index)
         edit.putBoolean(magneticHeadingKey, magneticHeading)
      }


      fun getTimeStr(time: Long?, pattern: String = "HH:mm:ss z"): String {
         val dateFormatter = SimpleDateFormat(pattern, Locale.getDefault())

         dateFormatter.timeZone = when (timezone.index) {
            1    -> TimeZone.getTimeZone("UTC")
            else -> TimeZone.getDefault()
         }

         return if (time != null) dateFormatter.format(time) else ""
      }

      fun getTimeStr(d: ZonedDateTime?, pattern: String = "HH:mm:ss z"): String {
         val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

         return when (timezone.index) {
            1    -> d?.withZoneSameInstant(ZoneId.of("UTC"))?.format(formatter) ?: ""
            else -> d?.withZoneSameInstant(ZoneId.systemDefault())?.format(formatter) ?: ""
         }
      }

      fun getIsoTimeStr(time: Long): String {
         val formatter = DateTimeFormatter.ISO_INSTANT
         return formatter.format(Instant.ofEpochMilli(time))
      }


      fun convertCoordinate(
         c: Double?,
         vertical: Boolean = true,
         diff: Boolean = false
      ): String {

         if (c == null) {
            return ""

         } else {
            if (coordinatesFormat.index == 0) {
               return "%.3f°".format(c)

            } else {
               var sign = ""
               var qStr = ""

               val value = abs(c)
               val degrees = floor(value)
               val minutes = (value - degrees) * 60f
               val seconds = floor((minutes - floor(minutes)) * 60f)

               if (diff) {
                  sign = if (c < 0.0) "-" else " "
               } else {
                  qStr = if (vertical)
                     if (c >= 0) "N" else "S"
                  else
                     if (c >= 0) "E" else "W"
               }

               return when (coordinatesFormat.index) {
                  1    -> "%s%.0f°%05.2f'$qStr".format(sign, degrees, minutes)
                  2    -> "%s%.0f°%02.0f'%02.0f''$qStr".format(sign, degrees, floor(minutes), floor(seconds))
                  else -> ""
               }
            }
         }
      }

      fun applyDistance(distance: Float?): Float? {
         return if (dimensions.index == 0) distance?.div(1000f) else distance?.div(1852f)
      }

      fun applySpeed(speed: Float): Float {
         return if (dimensions.index == 0) speed * 3.6f else speed * 3.6f / 1.852f
      }

      fun northBasedToDegree(course: Float): Float {
         return if (course < 0f) 360f + course else course
      }

      fun applyCourse(course: Float): Float {
         var c = if (magneticHeading) (course - declination) else course

         if (c < 0) c += 360.0f
         if (c > 360f) c -= 360f

         return c
      }
   }

   val timeStr: String
      get() = getTimeStr(time)
   val latStr: String
      get() = convertCoordinate(latitude)
   val lonStr: String
      get() = convertCoordinate(longitude, vertical = false)
   val locGp: GeoPoint
      get() = GeoPoint(latitude, longitude)

   val speedKmh: Float
      get() = speed * 3.6f
   val appliedSpeed: Float
      get() = applySpeed(speed)


  fun restoreValues(prevLoc: Location) {
      if (!hasSpeed() && prevLoc.hasSpeed()) speed = prevLoc.speed
      if (!hasBearing() && prevLoc.hasBearing()) bearing = prevLoc.bearing
      if (!hasAltitude() && prevLoc.hasAltitude()) altitude = prevLoc.altitude
      if (!hasAccuracy() && prevLoc.hasAccuracy()) accuracy = prevLoc.accuracy

      magneticDeviation = GeomagneticField(
         latitude.toFloat(),
         longitude.toFloat(),
         altitude.toFloat(),
         time
      )
   }

   fun getHeading(): Float {
      return applyCourse(bearing)
   }

   fun getHeadingDifferenceFromBearing(to: Location): Float {
      var diff = this.bearingTo(to) - this.bearing

      if (diff >= 180f)
         diff -= 360f

      if (diff <= -180f)
         diff += 360f

      return diff
   }

   fun description(): String {
      return "Lat: %s\nLon: %s\nSpeed: %.2f %s\nHeading: %.0f °"
         .format(latStr, lonStr, appliedSpeed, speedDimension, getHeading())
   }

   fun getAppliedDistance(location: Location): Float {
      return applyDistance(distanceTo(location))!!
   }

   fun getVelocityMadeGood(loc: Location): Float {
      return speed * cos(Math.toRadians(bearingTo(loc).toDouble() - bearing)).toFloat()
   }


   fun getAppliedVelocityMadeGood(loc: Location): Float {
      return applySpeed(getVelocityMadeGood(loc))
   }

   fun getHeadingPoint(loc: Location): GeoPoint {
      val distance = distanceTo(loc) / (1852 * 60)

      val directionLat = latitude + distance * cos(Math.toRadians(getHeading().toDouble()))
      val directionLon = longitude + distance * sin(Math.toRadians(getHeading().toDouble())) /
                         cos(Math.toRadians(latitude))

      return GeoPoint(directionLat, directionLon)
   }

   fun getAppliedBearing(location: Location): Float {
      return applyCourse(northBasedToDegree(bearingTo(location)))
   }

   fun getHeadingDeviation(wp: Location): Float {
      var diff = bearingTo(wp) - bearing

      if (diff < 180) diff += 360
      if (diff > 180) diff -= 360

      return diff
   }

   fun getEta(loc: Location): String {
      var eta = ""
      val distance = distanceTo(loc)

      if (speed > 0.01f) {
         val noSeconds: Int = (distance / speed).toInt()
         val duration: Duration = noSeconds.seconds

         eta = duration.toComponents { days, hours, minutes, seconds, _ -> "${days}d ${hours}h ${minutes}m" }
      }

      return eta
   }

//   fun getTrackPoint(name: String): TrackPoint {
//      return TrackPoint(
//         name = name,
//         time = time,
//         locLat = latitude.toFloat(),
//         locLon = longitude.toFloat()
//      )
//   }
}