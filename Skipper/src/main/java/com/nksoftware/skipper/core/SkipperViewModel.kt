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
 * File: SkipperViewModel.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.skipper.core

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.nksoftware.library.anchor.AnchorAlarm
import com.nksoftware.library.astronavigation.AstroNavigation
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.grib.GribFile
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.location.GpsLocation
import com.nksoftware.library.location.TrackDatabase
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.moon.Moon
import com.nksoftware.library.route.Route
import com.nksoftware.library.saildocs.SailDocs
import com.nksoftware.library.sun.Sun
import com.nksoftware.library.track.Track
import com.nksoftware.library.weather.Weather
import com.nksoftware.skipper.coreui.ScreenMode


const val activeRouteKey = "activeRoute"


@Suppress("UNCHECKED_CAST")
class SkipperViewModelFactory(
   private val ctx: Context,
   private val dir: String,
   private val sharedPreferences: SharedPreferences
) : ViewModelProvider.NewInstanceFactory() {

   override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return SkipperViewModel(ctx, dir, sharedPreferences) as T
   }
}


class SkipperViewModel(
   private val ctx: Context,
   dir: String,
   private val sharedPreferences: SharedPreferences
) : ViewModel() {

   private var locService: LocationService.LocalBinder? = null
   val gpsLocation = GpsLocation(ctx, 0)

   val track = Track(ctx, ScreenMode.Navigation.ordinal)
   val route = Route(ctx, ScreenMode.Navigation.ordinal)
   val anchorAlarm = AnchorAlarm(ctx, ScreenMode.Anchor.ordinal)

   val weather = Weather(ctx, viewModelScope, "$dir/files/weather", ScreenMode.Weather.ordinal)
   val gribFile = GribFile(ctx, ScreenMode.Grib.ordinal)
   val sailDocs = SailDocs(ctx)

   val moon = Moon()
   val sun = Sun()
   val astroNav = AstroNavigation(ctx, sun, ScreenMode.AstroNavigation.ordinal)

   var message: (String) -> Unit = {  }


   init {
      Log.i(logTag, "ViewModel - loading shared preferences")
      loadSharedPreferences(preferences = sharedPreferences)

      Log.i(logTag, "ViewModel - loading database")
      loadDb()
   }


   fun setService(binder: LocationService.LocalBinder) {
      locService = binder

      track.setService(binder)
      anchorAlarm.setService(binder)
   }


   fun setLocation(loc: Location?, trackUpdate: Boolean = false) {
      gpsLocation.update(loc)

      if (trackUpdate)
         track.updateTrack()
   }


   private fun loadSharedPreferences(preferences: SharedPreferences) {
      ExtendedLocation.loadSharedPreferences(preferences)
      gpsLocation.loadSharedPreferences(preferences)

      DataModel.loadPreferences(preferences)
   }


   private fun storeSharedPreferences(edit: SharedPreferences.Editor) {
      ExtendedLocation.storeSharedPreferences(edit)
      gpsLocation.storeSharedPreferences(edit)

      DataModel.storePreferences(edit)
   }


   override fun onCleared() {
      super.onCleared()
      saveData()
   }


   fun saveData() {
      Log.i(logTag, "ViewModel - storing shared preferences")
      val edit = sharedPreferences.edit()
      storeSharedPreferences(edit)
      edit.apply()

      Log.i(logTag, "ViewModel - storing database")
      storeDb()
   }


   fun loadDb() {
      val trackDb = Room.databaseBuilder(ctx, TrackDatabase::class.java, "SkipperTracks")
          .allowMainThreadQueries()
          .build()

      val dao = trackDb.userDao()
      val rt = dao.findByName(activeRouteKey)

      route.loadRoutePoints(rt)
      Log.i(logTag, "active route loaded with ${rt.size} points")

      trackDb.close()
   }


   fun storeDb() {
      val trackDb = Room.databaseBuilder(ctx, TrackDatabase::class.java, "SkipperTracks")
          .allowMainThreadQueries()
          .build()

      val dao = trackDb.userDao()

      dao.delete(activeRouteKey)
      val rtPts = route.getRoutePoints(activeRouteKey)

      if (rtPts.isNotEmpty()) dao.insertAll(rtPts)
      Log.i(logTag, "Number of route points saved: ${rtPts.size}")

      trackDb.close()
   }
}