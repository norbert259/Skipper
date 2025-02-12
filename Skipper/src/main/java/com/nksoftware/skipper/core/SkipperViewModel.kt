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

import android.content.SharedPreferences
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nksoftware.library.anchor.AnchorAlarm
import com.nksoftware.library.astronavigation.AstroNavigation
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.grib.GribFile
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.location.GpsLocation
import com.nksoftware.library.location.TrackDatabase
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.map.OsmMap
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
    private val mgr: LocationManager,
    private val dir: String,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SkipperViewModel(mgr, dir) as T
    }
}

class SkipperViewModel(
    mgr: LocationManager,
    dir: String,
) : ViewModel() {

    val gpsLocation = GpsLocation(mgr, 0)
    val map = OsmMap()

    val track = Track(ScreenMode.Navigation.ordinal)
    val route = Route(ScreenMode.Navigation.ordinal)

    val anchorAlarm = AnchorAlarm(ScreenMode.Anchor.ordinal)

    val weather = Weather(viewModelScope, "$dir/files/weather", ScreenMode.Weather.ordinal)
    val gribFile = GribFile(ScreenMode.Grib.ordinal)
    val sailDocs = SailDocs()

    val moon = Moon()
    val sun = Sun()
    val astroNav = AstroNavigation(sun, ScreenMode.AstroNavigation.ordinal)


    fun setService(binder: LocationService.LocalBinder) {
        track.setService(binder)
        anchorAlarm.setService(binder)
    }

    fun load(preferences: SharedPreferences, trackDb: TrackDatabase) {
        ExtendedLocation.loadSharedPreferences(preferences)
        DataModel.loadPreferences(preferences)

        val dao = trackDb.userDao()
        route.loadRoute(dao)
        track.loadTrack(dao)
    }

    fun store(preferences: SharedPreferences, trackDb: TrackDatabase) {
        val edit = preferences.edit()
        ExtendedLocation.storeSharedPreferences(edit)
        DataModel.storePreferences(edit)
        edit.apply()

        val dao = trackDb.userDao()
        route.storeRoute(dao)
        track.storeTrack(dao)
    }
}