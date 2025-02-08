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
 * File: OsmMap.kt
 * Last modified: 07.02.25, 21:44
 *
 */

package com.nksoftware.library.map

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.location.ExtendedLocation
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

const val chartKey = "Chart"
const val openSeaMapKey = "OpenSeaMap"


class OsmMap: DataModel() {

    var chart by mutableIntStateOf(0)
    var actualChart by mutableIntStateOf(chart)

    var openSeaMap by mutableStateOf(false)

    var chartTypes = mutableMapOf<String, OnlineTileSourceBase>(
        "Mapnik" to TileSourceFactory.MAPNIK,
        "TopoMap" to TileSourceFactory.OpenTopo
    )

    override fun loadPreferences(preferences: SharedPreferences) {
        chart = preferences.getInt(chartKey, 0)
        openSeaMap = preferences.getBoolean(openSeaMapKey, false)
    }

    override fun storePreferences(edit: SharedPreferences.Editor) {
        edit.putInt(chartKey, chart)
        edit.putBoolean(openSeaMapKey, openSeaMap)
    }

    override fun updateMap(
        mapView: OsmMapView,
        mapMode: Int,
        location: ExtendedLocation,
        snackbar: (String) -> Unit
    ) {}
}