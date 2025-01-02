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
 * File: Components.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.map

import android.graphics.Color
import com.nksoftware.library.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import kotlin.math.pow
import kotlin.math.sqrt


class NkMarker(
   mapview: MapView,
   private val dragFunc: ((Double, Double) -> Unit)? = null,
   var clickFunc: ((Double, Double) -> Unit)? = null
): Marker(mapview), Marker.OnMarkerDragListener, Marker.OnMarkerClickListener {

   init {
      setAnchor(ANCHOR_CENTER, ANCHOR_CENTER)

      textLabelFontSize = 28
      textLabelBackgroundColor = Color.WHITE

      if (!mapview.overlays.contains(this))
         mapview.overlays.add(this)

      if (dragFunc != null) {
         isDraggable = true
         setOnMarkerDragListener(this)
      }

      if (clickFunc != null) {
         setOnMarkerClickListener(this)
      }
   }

   override fun onMarkerDrag(marker: Marker?) {
      dragFunc?.let {
         if (marker != null) {
            it(marker.position.latitude, marker.position.longitude)
         }
      }
   }

   override fun onMarkerDragEnd(marker: Marker?) {
      dragFunc?.let {
         if (marker != null) {
            it(marker.position.latitude, marker.position.longitude)
         }
      }
   }

   override fun onMarkerDragStart(marker: Marker?) {}

   override fun onMarkerClick(
      marker: Marker?,
      mapView: MapView?
   ): Boolean {

      clickFunc?.let {
         if (marker != null)
            it(marker.position.latitude, marker.position.longitude)
      }

      onMarkerClickDefault(marker, mapView)
      return true
   }
}


class NkPolyline(mapview: MapView, width: Float, color: Int) : Polyline(mapview) {
   init {
      outlinePaint.strokeWidth = width
      outlinePaint.color = color
      infoWindow = null

      if (!mapview.overlays.contains(this))
         mapview.overlays.add(this)
   }

   fun setLineColor(c: Int) {
      mOutlinePaint.color = c
   }

   fun setCircle(pt: GeoPoint, diameter: Double, noPts: Int = 20) {
      val circle = mutableListOf<GeoPoint>()

      for (i in 0..360 step noPts) circle.add(pt.destinationPoint(diameter, i.toDouble()))

      apply {
         isEnabled = true
         setPoints(circle)
      }
   }

   fun setCircle2(center: GeoPoint, angle: Double) {
      val circle = mutableListOf<GeoPoint>()

      for (i in -100..100) {
         val lon = i.toDouble() / 100.0
         val lat = sqrt(1 - lon.pow(2))

         circle.add(GeoPoint(lat, lon))
      }

      for (i in 99 downTo -100 ) {
         val lon = i.toDouble() / 100.0
         val lat = -sqrt(1 - lon.pow(2))

         circle.add(GeoPoint(lat, lon))
      }

      val scaledCircle = circle.map {
         pt -> GeoPoint(pt.latitude * angle + center.latitude, pt.longitude * angle + center.longitude)
      }

      apply {
         isEnabled = true
         setPoints(scaledCircle)
      }
   }

}


class NkPolygon(mapview: MapView, color: Int) : Polygon(mapview) {

   init {
      if (!mapview.overlays.contains(this))
         mapview.overlays.add(this)

      super.mOutlinePaint.color = color
   }
}


val windIcons = listOf(
   R.drawable.symbol_wind_speed_01_neu,
   R.drawable.symbol_wind_speed_02_neu,
   R.drawable.symbol_wind_speed_03_neu,
   R.drawable.symbol_wind_speed_04_neu,
   R.drawable.symbol_wind_speed_05_neu,
   R.drawable.symbol_wind_speed_06_neu,
   R.drawable.symbol_wind_speed_07_neu,
   R.drawable.symbol_wind_speed_08_neu,
   R.drawable.symbol_wind_speed_09_neu,
   R.drawable.symbol_wind_speed_10_neu,
   R.drawable.symbol_wind_speed_11_neu,
   R.drawable.symbol_wind_speed_12_neu
)


fun getWindSpeedIcon(value: Float): Int {
   return when (value) {
      in 0f..9.9999f -> 0
      in 10f..19.9999f -> 1
      in 20f..28.9999f -> 2
      in 29f..37.9999f -> 3
      in 38f..46.9999f -> 4
      in 47f..56.9999f -> 5
      in 57f..65.9999f -> 6
      in 66f..74.9999f -> 7
      in 75f..83.9999f -> 8
      in 84f..93.9999f -> 9
      in 94f..102.9999f -> 10
      else -> 11
   }
}
