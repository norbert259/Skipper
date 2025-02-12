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
 * File: Route.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.route

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.nksoftware.library.R
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.location.TrackPoint
import com.nksoftware.library.location.TrackPointDao
import com.nksoftware.library.map.NkMarker
import com.nksoftware.library.map.NkPolyline
import com.nksoftware.library.map.OsmMapView
import com.nksoftware.library.utilities.nkHandleException
import org.osmdroid.util.GeoPoint
import kotlin.math.abs


const val routeActiveKey = "RouteActive"
const val sailingModeKey = "SailingMode"
const val tackAngleKey = "TackAngle"

const val activeRouteKey = "activeRoute"
const val logTag = "Route"


class Route(mapMode: Int) : DataModel(mapMode) {

   var active by mutableStateOf(false)
   var sailingMode: Boolean by mutableStateOf(false)
   var tackAngle by mutableFloatStateOf(90.0f)

   var name by mutableStateOf("route.gpx")
   var routePoints = mutableStateListOf<ExtendedLocation>()
   var selectedRoutePoint by mutableIntStateOf(-1)

   val size: Int
      get() = routePoints.size

   val selectedPoint: ExtendedLocation?
      get() = if (selectedRoutePoint in routePoints.indices) routePoints[selectedRoutePoint] else null

   private var courseLine: NkPolyline? = null
   private var bearingLine: NkPolyline? = null
   private var tackLine: NkPolyline? = null
   private var routeLine: NkPolyline? = null

   private val routeMarker: MutableList<NkMarker> = mutableListOf()


   fun addPoint(ctx: Context, lat: Double, lon: Double, msg: ((String) -> Unit)? = null) {
      try {
         val loc = ExtendedLocation(lat, lon)

         if (routePoints.isEmpty()) {
            selectedRoutePoint = 0
            routePoints.add(loc)

         } else {
            val distances = routePoints.map { loc.distanceTo(it) }
            val minimumDistances = distances.withIndex().minBy { it.value }

            if (minimumDistances.index == routePoints.indices.last)
               routePoints.add(ExtendedLocation(loc))
            else
               routePoints.add(minimumDistances.index + 1, loc)
         }
      }
      catch (e: Exception) {
         nkHandleException(logTag, ctx.getString(R.string.cannot_add_routing_point), e, msg)
      }
   }

   fun insertPoint(ctx: Context, lat: Double, lon: Double, msg: ((String) -> Unit)? = null) {
      try {
         val loc = ExtendedLocation(lat, lon)

         if (routePoints.isEmpty()) {
            selectedRoutePoint = 0
            routePoints.add(loc)

         } else {
            val distances = routePoints.map { loc.distanceTo(it) }
            val minimumDistances = distances.withIndex().minBy { it.value }

            routePoints.add(minimumDistances.index, loc)
         }
      }
      catch (e: Exception) {
         nkHandleException(logTag, ctx.getString(R.string.cannot_insert_routing_point), e, msg)
      }
   }

   fun deleteAllPoints(msg: ((String) -> Unit)? = null) {
      routePoints.clear()
   }

   fun deletePoint(ctx: Context, lat: Double, lon: Double, msg: ((String) -> Unit)? = null) {
      try {
         val loc = ExtendedLocation(lat, lon)

         val distances = routePoints.map { loc.distanceTo(it) }
         val minimumDistances = distances.withIndex().minBy { it.value }

         routePoints.removeAt(minimumDistances.index)

         if (routePoints.size == 0)
            selectedRoutePoint = -1
      }
      catch (e: Exception) {
         nkHandleException(logTag, ctx.getString(R.string.cannot_delete_routing_point), e, msg)
      }
   }

   fun reverse() {
      val revList = routePoints.reversed()
      routePoints.clear()

      for (pt in revList) routePoints.add(pt)
   }

   fun getLength(): Float {
      val distances = routePoints.windowed(2, 1) { p -> p[0].distanceTo(p[1]) }
      return distances.sum()
   }

   fun getRemainingLength(loc: ExtendedLocation): Float {
      val list = mutableListOf(loc)
      list.addAll(routePoints.filterIndexed { index, _ -> index >= selectedRoutePoint })

      val distances = list.windowed(2, 1) { p -> p[0].distanceTo(p[1]) }
      return distances.sum()
   }

   fun change(i: Int, lat: Double, lon: Double) {
      routePoints[i] = ExtendedLocation(lat, lon)
   }

   fun getNextWayPoint(loc: ExtendedLocation): ExtendedLocation? {
      val distances = routePoints.map { loc.distanceTo(it) }
      val minimumDistances = distances.withIndex().minBy { it.value }

      val distancesBetweenRoutingPts = routePoints.zipWithNext { x, y -> x.distanceTo(y) }

      when (routePoints.size) {
         0 -> {
            selectedRoutePoint = -1
            return null
         }

         1 -> {
            selectedRoutePoint = 0
            return routePoints[0]
         }

         else -> {
            if ((minimumDistances.index + 1 in routePoints.indices) &&
                (distances[minimumDistances.index + 1] < distancesBetweenRoutingPts[minimumDistances.index])
            ) {
               selectedRoutePoint = minimumDistances.index + 1
               return routePoints[minimumDistances.index + 1]

            } else {
               if ((minimumDistances.index == routePoints.indices.last) &&
                   abs(loc.getHeadingDifferenceFromBearing(routePoints[minimumDistances.index])) > 90f
               ) {
                  selectedRoutePoint = -1
                  return null
               } else {
                  selectedRoutePoint = minimumDistances.index
                  return routePoints[minimumDistances.index]
               }
            }
         }
      }
   }

   fun getDistances(): List<Float> {
      return routePoints.zipWithNext { a, b -> a.getAppliedDistance(b) }
   }

   fun getCourses(): List<Float> {
      return routePoints.zipWithNext { a, b -> a.getAppliedBearing(b) }
   }

   fun getElevations(): List<Float?> {
      return routePoints.zipWithNext { a, b ->
         if (a.hasAltitude() && b.hasAltitude())
            (b.altitude - a.altitude).toFloat()
         else
            null
      }
   }

   override fun loadPreferences(preferences: SharedPreferences) {
      active = preferences.getBoolean(routeActiveKey, false)
      sailingMode = preferences.getBoolean(sailingModeKey, false)
      tackAngle = preferences.getFloat(tackAngleKey, 90f)
   }

   override fun storePreferences(edit: SharedPreferences.Editor) {
      edit.putBoolean(sailingModeKey, sailingMode)
      edit.putBoolean(routeActiveKey, active)
      edit.putFloat(tackAngleKey, tackAngle)
   }

   fun loadRoute(dao: TrackPointDao) {
      routePoints.clear()
      val rt = dao.findByName(activeRouteKey)

      rt.forEach { pt ->
         val loc = ExtendedLocation(pt.locLat.toDouble(), pt.locLon.toDouble())
         loc.time = pt.time ?: 0

         routePoints.add(loc)
      }

      Log.i(logTag, "active route loaded with ${rt.size} points")
   }

   fun storeRoute(dao: TrackPointDao) {
      dao.delete(activeRouteKey)

      val rtPts = List(size, init = { i ->
         TrackPoint(
            name = activeRouteKey,
            locLat = routePoints[i].latitude.toFloat(),
            locLon = routePoints[i].longitude.toFloat()
         )
      })

      if (rtPts.isNotEmpty()) dao.insertAll(rtPts)
      Log.i(logTag, "Number of route points saved: ${rtPts.size}")
   }

   fun deleteRouteMarkers(mapView: OsmMapView) {
      if (routeMarker.isNotEmpty()) {
         for (marker in routeMarker) marker.remove(mapView)
         routeMarker.clear()
      }
   }

   override fun updateMap(mapView: OsmMapView, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {

      val routeIconYellow = ContextCompat.getDrawable(mapView.ctx, R.drawable.circle_yellow)
      val routeIconRed = ContextCompat.getDrawable(mapView.ctx, R.drawable.circle_red)

      if (routeLine == null)
         routeLine = NkPolyline(mapView, width = 5.0f, Color.RED)

      if (bearingLine == null)
         bearingLine = NkPolyline(mapView, width = 5.0f, color = Color.DKGRAY, disableInfoWindow = false)

      if (tackLine == null)
         tackLine = NkPolyline(mapView, width = 5.0f, Color.GRAY, disableInfoWindow = false)

      if (courseLine == null)
         courseLine = NkPolyline(mapView, width = 5.0f, Color.CYAN)

      if (mapMode == mapModeToBeUpdated) {
         val locGp = location.locGp

         if (!active || routePoints.isEmpty()) {
            if (location.hasBearing() && location.hasSpeed() && location.speed > 0.1) {
               courseLine?.apply {
                  val bearingPoint = location.locGp.destinationPoint(5000.0, location.bearing.toDouble())
                  setPoints(listOf(location.locGp, bearingPoint))

                  isEnabled = true
               }
            } else {
               courseLine?.apply { isEnabled = false }
            }

            routeMarker.forEach { it.apply { isEnabled = false } }

            bearingLine?.apply { isEnabled = false }
            tackLine?.apply { isEnabled = false }
            routeLine?.apply { isEnabled = false }

         } else {
            val wp = getNextWayPoint(location)

            if (wp != null) {
               courseLine?.apply {
                  isEnabled = true
                  setPoints(listOf(locGp, location.getHeadingPoint(wp)))
               }

               val wpGP = wp.locGp

               bearingLine?.apply {
                  isEnabled = true
                  setPoints(listOf(locGp, wpGP))
                  setInfoWindow(
                     mapView.ctx.getString(
                        R.string.bearingline_description,
                        location.bearingTo(wp),
                        ExtendedLocation.applyDistance(location.distanceTo(wp)),
                        ExtendedLocation.distanceDimension
                     )
                  )
               }

               val courseDeviation = location.getHeadingDeviation(wp)
               if (sailingMode && (abs(courseDeviation) < tackAngle)) {

                  val headingCourse = location.appliedHeading ?: 0f
                  var tackCourse: Float

                  if (courseDeviation > 0) {
                     tackCourse = headingCourse + tackAngle
                     tackLine?.apply { setLineColor(Color.GREEN) }
                  } else {
                     tackCourse = headingCourse - tackAngle
                     tackLine?.apply { setLineColor(Color.RED) }
                  }

                  if (headingCourse > 360) tackCourse -= 360
                  if (headingCourse < 360) tackCourse += 360

                  val tlGP = locGp.destinationPoint(
                     location.distanceTo(wp).toDouble(),
                     ExtendedLocation.applyCourse(tackCourse).toDouble()
                  )

                  tackLine?.apply {
                     isEnabled = true
                     setPoints(listOf(locGp, tlGP))
                     setInfoWindow(
                        mapView.ctx.getString(
                           R.string.tackline_description,
                           ExtendedLocation.applyCourse(tackCourse)
                        )
                     )
                  }

               } else
                  tackLine?.apply { isEnabled = false }
            }

            if (routeMarker.size != routePoints.size) {
               deleteRouteMarkers(mapView)

               for ((index, routePt) in routePoints.withIndex()) {
                  val rMarker = NkMarker(
                     mapView,
                     dragFunc = { lat, lon ->
                        change(index, lat, lon)
                        routeLine?.apply { setPoints(List(routeMarker.size) { routeMarker[it].position }) }
                     }
                  ).apply {
                     isEnabled = true
                     icon = if (index == selectedRoutePoint) routeIconYellow else routeIconRed
                     position = GeoPoint(routePt.latitude, routePt.longitude)
                     title = mapView.ctx.getString(R.string.routing_point).format(
                        index + 1,
                        routePoints[index].latStr,
                        routePoints[index].lonStr
                     )
                  }

                  routeMarker.add(rMarker)
               }

               routeLine?.apply {
                  isEnabled = true
                  setPoints(List(routeMarker.size) { routeMarker[it].position })
               }

            } else {
               for ((index, marker) in routeMarker.withIndex()) {
                  marker.apply {
                     isEnabled = true
                     icon = if (index == selectedRoutePoint) routeIconYellow else routeIconRed
                     position = GeoPoint(routePoints[index].latitude, routePoints[index].longitude)
                     title = mapView.ctx.getString(R.string.routing_point).format(
                        index + 1,
                        routePoints[index].latStr,
                        routePoints[index].lonStr
                     )
                  }
               }
               routeLine?.apply { isEnabled = true }
            }
         }
      } else {
         deleteRouteMarkers(mapView)

         routeLine?.apply { isEnabled = false }
         bearingLine?.apply { isEnabled = false }
         tackLine?.apply { isEnabled = false }
         courseLine?.apply { isEnabled = false }
      }
   }
}
