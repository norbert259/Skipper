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
 * File: Sun.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.sun

import android.location.Location
import com.nksoftware.library.astronavigation.SunEdge
import com.nksoftware.library.location.ExtendedLocation
import org.shredzone.commons.suncalc.SunTimes
import org.shredzone.commons.suncalc.SunPosition
import java.time.ZonedDateTime
import java.util.Date
import kotlin.text.format


class Sun {

   private val sunCorrection = mapOf(
      SunEdge.Bottom to -16.0 / 60.0,
      SunEdge.Middle to 0.0,
      SunEdge.Top to +16.0 / 60.0
   )

   private val sunTimes: SunTimes.Parameters = SunTimes.compute().today()
   private val sunPosition: SunPosition.Parameters = SunPosition.compute().today()


   fun getSunRise(
      loc: Location,
      eyeLevel: Double = 0.0,
      twilight: SunTimes.Twilight = SunTimes.Twilight.HORIZON
   ): ZonedDateTime? {
      return sunTimes.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .twilight(twilight)
         .execute()
         .rise
   }

   fun getSunRiseStr(
      loc: Location,
      eyeLevel: Double,
      twilight: SunTimes.Twilight = SunTimes.Twilight.HORIZON
   ): String {
      return ExtendedLocation.getTimeStr(getSunRise(loc, eyeLevel, twilight))
   }

   fun getSunSet(
      loc: Location,
      eyeLevel: Double = 0.0,
      twilight: SunTimes.Twilight = SunTimes.Twilight.HORIZON
      ): ZonedDateTime? {
      return sunTimes.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .twilight(twilight)
         .execute()
         .set
   }

   fun getSunSetStr(
      loc: Location,
      eyeLevel: Double = 0.0,
      twilight: SunTimes.Twilight = SunTimes.Twilight.HORIZON
      ): String {
      return ExtendedLocation.getTimeStr(getSunSet(loc, eyeLevel, twilight))
   }

   fun getSunNoon(
      loc: Location,
      eyeLevel: Double = 0.0): ZonedDateTime? {
      return sunTimes.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute()
         .noon
   }

   fun getSunNoonStr(
      loc: Location,
      eyeLevel: Double
   ): String {
      return ExtendedLocation.getTimeStr(getSunNoon(loc, eyeLevel))
   }

   fun getSunNadir(
      loc: Location,
      eyeLevel: Double = 0.0
   ): ZonedDateTime? {
      return sunTimes.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute()
         .nadir
   }

   fun getSunNadirStr(
      loc: Location,
      eyeLevel: Double = 0.0
   ): String {
      return ExtendedLocation.getTimeStr(getSunNadir(loc, eyeLevel), "HH:mm:ss")
   }

   fun getSunAzimuth(
      loc: Location,
      now: Date,
      eyeLevel: Double
   ): String {
      return "%.2f".format(
         sunPosition.at(loc.latitude, loc.longitude)
            .elevation(eyeLevel)
            .on(now)
            .execute()
            .azimuth
      )
   }

   fun getSunAltitude(
      loc: Location,
      now: Date,
      eyeLevel: Double = 0.0,
      sunEdge: SunEdge
   ): Double {
      var altitude = sunPosition
         .on(now)
         .at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute()
         .altitude

      altitude += sunCorrection.getOrDefault(sunEdge, 0.0)
      return altitude
   }

   fun getSunAltitudeStr(
      loc: Location,
      now: Date,
      eyeLevel: Double = 0.0,
      sunEdge: SunEdge
   ): String {
      return ExtendedLocation.convertCoordinate(getSunAltitude(loc, now, eyeLevel, sunEdge), diff = true)
   }
}