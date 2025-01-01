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
 * File: Moon.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.moon

import android.location.Location
import com.nksoftware.library.location.ExtendedLocation
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import java.time.ZonedDateTime
import kotlin.text.format

class Moon {

   private val moonTime: MoonTimes.Parameters = MoonTimes.compute().today()
   private val moonPosition: MoonPosition.Parameters = MoonPosition.compute().today()
   private val moonIllumination: MoonIllumination.Parameters = MoonIllumination.compute().today()

   fun getMoonRise(
      loc: Location,
      eyeLevel: Double = 0.0
      ): ZonedDateTime? {
      return moonTime.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute().rise
   }

   fun getMoonRiseStr(
      loc: Location,
      eyeLevel: Double = 0.0
   ): String {
      return ExtendedLocation.getTimeStr(getMoonRise(loc, eyeLevel))
   }

   fun getMoonSet(
      loc: Location,
      eyeLevel: Double = 0.0
   ): ZonedDateTime? {
      return moonTime.at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute().set
   }

   fun getMoonSetStr(
      loc: Location,
      eyeLevel: Double = 0.0
   ): String {
      return ExtendedLocation.getTimeStr(getMoonSet(loc, eyeLevel))
   }

   fun getMoonPhase(loc: Location): MoonPhase.Phase {
      return moonIllumination.at(loc.latitude, loc.longitude).execute().closestPhase
   }

   fun getMoonPhaseStr(loc: Location): String {
      return moonIllumination.at(loc.latitude, loc.longitude).execute().closestPhase.toString()
   }

   fun getMoonAlwaysUp(loc: Location): Boolean {
      return moonTime.at(loc.latitude, loc.longitude).execute().isAlwaysUp
   }

   fun getMoonAlwaysDown(loc: Location): Boolean {
      return moonTime.at(loc.latitude, loc.longitude).execute().isAlwaysDown
   }

   fun getMoonAzimuthStr(
      loc: Location,
      eyeLevel: Double
   ): String {
      return "%.2f".format(
         return "%.2f".format(
            moonPosition.at(loc.latitude, loc.longitude)
               .elevation(eyeLevel)
               .now()
               .execute()
               .azimuth
         )
      )
   }

   fun getMoonAltitude(
      loc: Location,
      eyeLevel: Double = 0.0
   ): Double {
      return MoonPosition.compute()
         .now()
         .at(loc.latitude, loc.longitude)
         .elevation(eyeLevel)
         .execute()
         .altitude
   }

   fun getMoonAltitudeStr(
      loc: Location,
      eyeLevel: Double = 0.0
   ): String {
      return ExtendedLocation.convertCoordinate(getMoonAltitude(loc, eyeLevel), diff = true)
   }
}