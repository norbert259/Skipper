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
 * File: GpsLocation.kt
 * Last modified: 07/01/2025, 17:49
 *
 */

package com.nksoftware.library.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nksoftware.library.composables.SingleSelectList
import com.nksoftware.library.locationservice.SkipperLocTag


const val gpsProviderKey = "gpsProvider"
const val gpsEnabledKey = "gpsEnabled"


@SuppressLint("MissingPermission")
class GpsLocation(ctx: Context, provider: Int = 0): LocationListener {

   val mgr: LocationManager = ctx.getSystemService(LOCATION_SERVICE) as LocationManager

   val gpsProviders = mgr.getProviders(true)
   val gpsProvider = SingleSelectList(listOf(LocationManager.FUSED_PROVIDER, LocationManager.GPS_PROVIDER), provider)

   var location by mutableStateOf(ExtendedLocation(Location("")))

   var gps by mutableStateOf(true)
   var updateCounter by mutableIntStateOf(0)

   init {
      val loc = mgr.getLastKnownLocation(gpsProvider.value)
      loc?.let { location = ExtendedLocation(it) }
   }


   fun loadSharedPreferences(preferences: SharedPreferences) {
      gpsProvider.index = preferences.getInt(gpsProviderKey, 0)
      gps = preferences.getBoolean(gpsEnabledKey, true)
   }


   fun storeSharedPreferences(edit: SharedPreferences.Editor) {
      edit.putInt(gpsProviderKey, gpsProvider.index)
      edit.putBoolean(gpsEnabledKey, gps)
   }


   fun activate() {
      mgr.removeUpdates(this)

      if (gpsProvider.value in gpsProviders)
         mgr.requestLocationUpdates(gpsProvider.value, 3000, 0f, this)
   }


   fun update(loc: Location?) {
      if (loc != null) {
         location = ExtendedLocation(loc)
         updateCounter++
      }
   }


   override fun onLocationChanged(p0: Location) {
      update(p0)
   }


   override fun onProviderDisabled(provider: String) {
      Log.i(SkipperLocTag, "Provider $provider disabled")
   }


   override fun onProviderEnabled(provider: String) {
      Log.i(SkipperLocTag, "Provider $provider enabled")
   }
}