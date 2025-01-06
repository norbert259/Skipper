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
 * File: Track.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.locationservice

import android.location.Location
import android.util.Log
import kotlin.math.abs

class Track {

   var trackingEnabled = false
   val trackingStr: String
      get() = if (trackingEnabled) "on" else "off"

   private val track = mutableListOf<Location>()

   fun setTracking(tr: Boolean) { trackingEnabled = tr }

   fun clearTrack() { track.clear() }

   fun getTrack(): List<Location> = track.toList()


   fun addLocation(loc: Location): Boolean {
      var updated = false

      if (trackingEnabled) {
         if (track.isEmpty()) {
            Log.d(SkipperLocTag, "Tracking: adding first location")

            track.add(loc)
            updated = true

         } else {
            val lastLoc = track.last()
            val distance = lastLoc.distanceTo(loc)

            if (distance > 10f) {
               val timeDifference = abs((loc.time - lastLoc.time) / 1000f)
               val speed = distance / timeDifference

               updated = when (speed.toInt()) {

                  in 0..<2     -> if (timeDifference > 20f) true else false
                  in 2..<10    -> if (timeDifference > 30f) true else false
                  in 10..<50   -> if (timeDifference > 45f) true else false
                  in 50..<1000 -> if (timeDifference > 60f) true else false

                  else         -> {
                     Log.w(SkipperLocTag, "Cannot classify speed range")
                     true
                  }
               }

               if (updated) {
                  Log.d(
                     SkipperLocTag,
                     "Track point added: speed(%.1f m/s) distance(%d m) time difference: %.1f s"
                        .format(speed, distance.toInt(), timeDifference)
                  )
                  track.add(loc)
               }
            }
         }
      }

      return updated
   }
}