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
1 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File: Track.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.track

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nksoftware.library.R
import com.nksoftware.library.composables.SingleSelectList
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.map.NkPolyline
import com.nksoftware.library.map.OsmMapView
import org.osmdroid.util.GeoPoint
import kotlin.time.DurationUnit
import kotlin.time.toDuration


const val trackActiveKey = "TrackActive"


class Track(mapMode: Int) : DataModel(mapMode) {

   private var locService: LocationService.LocalBinder? = null
   var saveTrack by mutableStateOf(false)

   var name by mutableStateOf("track.gpx")
   var track: List<Location> by mutableStateOf(listOf())

   val granularity: SingleSelectList<Int> = SingleSelectList(listOf(1, 5, 10, 20), 0)

   var size: Int = 0
      get() = track.size

   val isNotEmpty: Boolean
      get() = track.isNotEmpty()

   var startTrackTime: String by mutableStateOf("  ")
   var endTrackTime: String by mutableStateOf("  ")

   var timeSlots: List<Long> by mutableStateOf(listOf())

   var courses: List<Float> by mutableStateOf(listOf())
   var distances: List<Float> by mutableStateOf(listOf())
   var speeds: List<Float> by mutableStateOf(listOf())
   var elevations: List<Float> by mutableStateOf(listOf())

   private var trackLine: NkPolyline? = null


   fun setService(binder: LocationService.LocalBinder) {
      locService = binder
      locService?.setTracking(saveTrack)
   }

   fun toggleTracking() {
      saveTrack = !saveTrack
      locService?.setTracking(saveTrack)
   }

   fun clearTrack() {
      saveTrack = false

      locService?.setTracking(saveTrack)
      locService?.clearTrack()
   }

   fun updateTrack() {
      if (locService != null) {
         track = locService!!.getTrack()

         startTrackTime = if (track.isNotEmpty()) ExtendedLocation.getTimeStr(track.first().time, "EEE HH:mm") else "  "
         endTrackTime = if (track.isNotEmpty()) ExtendedLocation.getTimeStr(track.last().time, "EEE HH:mm") else "  "

         timeSlots = track
            .map { it.time }
            .chunked(granularity.value)
            .map { it.max() }

         if (track.size > 1) {
            val timeDifferences = track.map { it.time / 1000.0 }
               .windowed(granularity.value + 1, step = granularity.value, partialWindows = true)
               .map { it.max() - it.min() }

            courses = track.zipWithNext { x, y -> ExtendedLocation.applyCourse(x.bearingTo(y)) }
               .chunked(granularity.value)
               .map { it.average().toFloat() }

            distances = track.zipWithNext { x, y -> x.distanceTo(y) }
               .chunked(granularity.value)
               .map { it.sum() }

            speeds = distances.mapIndexed { i, x -> x / timeDifferences[i].toFloat() }

            elevations = track.drop(1).map { x -> if (x.hasAltitude()) x.altitude.toFloat() else Float.NaN }
               .chunked(granularity.value)
               .map { it.last() }

         } else {
            courses = listOf()
            distances = listOf()
            speeds = listOf()
            elevations = listOf()
         }
      }
   }

   fun getDescription(ctx: Context): String {
      return ctx.getString(R.string.track_description, startTrackTime, endTrackTime,
         ExtendedLocation.applyDistance(distances.sum()), ExtendedLocation.distanceDimension,
         if (elevations.isNotEmpty()) elevations.max() - elevations.min() else 0f,
         ExtendedLocation.applySpeed(speeds.average().toFloat()), ExtendedLocation.speedDimension
      )
   }

   fun getTotalSpeed(): Float? {
      return if (speeds.isNotEmpty()) ExtendedLocation.applySpeed(speeds.average().toFloat()) else null
   }

   fun getDuration(): String {
      return if (track.isNotEmpty()) {
         val timeDiff = (track.last().time - track.first().time)

         return (timeDiff / 1000)
            .toDuration(DurationUnit.SECONDS)
            .toComponents { hours, minutes, seconds, _ -> "%02d:%02d:%02d".format(hours, minutes, seconds) }
      } else ""
   }

   override fun loadPreferences(preferences: SharedPreferences) {
      saveTrack = preferences.getBoolean(trackActiveKey, false)
   }

   override fun storePreferences(edit: SharedPreferences.Editor) {
      edit.putBoolean(trackActiveKey, saveTrack)
   }

   override fun updateMap(mapView: OsmMapView, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {
      if (trackLine == null)
         trackLine = NkPolyline(mapView, width = 5.0f, color = Color.BLUE, disableInfoWindow = false)

      if ((mapMode == mapModeToBeUpdated)) {
         if (track.isNotEmpty()) {
            val pts = mutableListOf<GeoPoint>()

            track.forEach { t -> pts.add(GeoPoint(t.latitude, t.longitude))}
            pts.add(location.locGp)

            trackLine!!.apply {
               isEnabled = true
               setPoints(pts)
               setInfoWindow(getDescription(mapView.ctx))
            }
         } else {
            trackLine!!.apply { isEnabled = false }
         }
      } else
         trackLine!!.apply { isEnabled = false }
   }
}