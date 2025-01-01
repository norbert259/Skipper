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
 * File: Alarm.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.locationservice

import android.content.Context
import android.location.Location
import android.media.MediaPlayer


class Alarm(
   ctx: Context
) {
   var anchorAlarmSet = false
   private var anchorageDiameter = 100.0f
   private var anchorPoint: Location? = null


   fun checkForAnchorDrift(loc: Location, mediaPlayer: MediaPlayer) {

      if (anchorAlarmSet && (anchorPoint != null)) {
         if (loc.distanceTo(anchorPoint!!) > anchorageDiameter) {
            mediaPlayer.start()
         }

      } else {
         if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
         }
      }
   }


   fun setAnchorAlarm(active: Boolean = false, loc: Location? = null, diameter: Float = 100.0f) {
      anchorAlarmSet = active
      anchorPoint = loc
      anchorageDiameter = diameter
   }

}