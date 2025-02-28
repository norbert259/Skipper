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
 * File: AnchorAlarm.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.anchor

import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.nksoftware.library.R
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.map.NkMarker
import com.nksoftware.library.map.NkPolyline
import com.nksoftware.library.map.OsmMapView
import org.osmdroid.util.GeoPoint

const val anchorSetKey = "AnchorSet"
const val anchorDiameterKey = "AnchorDiameter"
const val anchorPointLatKey = "AnchorPointLat"
const val anchorPointLonKey = "AchorPointLon"


class AnchorAlarm(val mapMode: Int) : DataModel(mapMode) {

   private var locService: LocationService.LocalBinder? = null
   var anchorAlarmSet by mutableStateOf(false)

   var anchorageDiameter by mutableFloatStateOf(100.0f)
   var anchorPoint: Location by mutableStateOf(Location(""))

   private var anchorMarker: NkMarker? by mutableStateOf(null)
   private var anchorCircle: NkPolyline? = null

   val anchorGp: GeoPoint
      get() = anchorPoint.let { GeoPoint(it.latitude, it.longitude) }

   fun setService(binder: LocationService.LocalBinder) {
      locService = binder
   }

   fun toggleAlarm() {
      anchorAlarmSet = !anchorAlarmSet
      locService?.setAnchorAlarm(anchorAlarmSet, anchorPoint, anchorageDiameter)
   }

   override fun loadPreferences(preferences: SharedPreferences) {
      anchorAlarmSet = preferences.getBoolean(anchorSetKey, false)
      anchorageDiameter = preferences.getFloat(anchorDiameterKey, 100f)

      val lat = preferences.getFloat(anchorPointLatKey, 0f).toDouble()
      val lon = preferences.getFloat(anchorPointLonKey, 0f).toDouble()
      anchorPoint = ExtendedLocation(lat, lon)
   }

   override fun storePreferences(edit: SharedPreferences.Editor) {
      edit.putBoolean(anchorSetKey, anchorAlarmSet)
      edit.putFloat(anchorDiameterKey, anchorageDiameter)
      edit.putFloat(anchorPointLatKey, anchorPoint.latitude.toFloat())
      edit.putFloat(anchorPointLonKey, anchorPoint.longitude.toFloat())
   }

   override fun updateMap(
      mapView: OsmMapView,
      mapMode: Int,
      location: ExtendedLocation,
      snackbar: (String) -> Unit
   ) {

      val anchorIconRed = ContextCompat.getDrawable(mapView.ctx, R.drawable.anchor_red)
      val anchorIconBlack = ContextCompat.getDrawable(mapView.ctx, R.drawable.anchor_black)

      if (anchorMarker == null) {

         anchorMarker = NkMarker(
            mapView,
            dragFunc = { lat, lon ->
               anchorPoint = ExtendedLocation(lat, lon)
               anchorCircle!!.setCircle(anchorGp, anchorageDiameter.toDouble())
            }
         )

         anchorMarker!!.apply {
            isEnabled = true
            isDraggable = !anchorAlarmSet
            position = anchorGp
            icon = if (anchorAlarmSet) anchorIconRed else anchorIconBlack
         }

         anchorCircle = NkPolyline(mapView, width = 5.0f, Color.RED)

      }

      if (mapMode == mapModeToBeUpdated) {
         anchorMarker!!.apply {
            isEnabled = true
            isDraggable = !anchorAlarmSet
            position = anchorGp
            icon = if (anchorAlarmSet) anchorIconRed else anchorIconBlack
         }

         anchorCircle!!.setCircle(anchorGp, anchorageDiameter.toDouble())

         anchorCircle!!.setLineColor(
            if (anchorAlarmSet) Color.RED else Color.BLACK
         )

      } else {
         anchorMarker!!.isEnabled = false
         anchorCircle!!.apply { isEnabled = false }
      }
   }
}

