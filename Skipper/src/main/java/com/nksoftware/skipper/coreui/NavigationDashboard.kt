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
 * File: NavigationDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.skipper.coreui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MultilineChart
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.location.CompassDashboard
import com.nksoftware.library.location.ExtendedLocationDashboard
import com.nksoftware.library.route.RouteDashboard
import com.nksoftware.library.route.WaypointDashboard
import com.nksoftware.library.track.TrackDashboard
import com.nksoftware.skipper.core.SkipperViewModel


val navigationTabIcons = listOf(
    Icons.Outlined.Directions, Icons.Outlined.CompassCalibration,
    Icons.AutoMirrored.Outlined.MultilineChart, Icons.Outlined.Timeline
)


@Composable
fun NavigationDashboard(
    vm: SkipperViewModel,
    snackBar: (str: String) -> Unit
) {

    var state by remember { mutableIntStateOf(0) }

    Column {
        NkTabRowIcon(
            tabIndex = state,
            icons = navigationTabIcons,
            set = { s -> state = s }
        )

        when (state) {
            0 -> {
                if (vm.route.active) WaypointDashboard(vm.gpsLocation.location, vm.route)
                ExtendedLocationDashboard(vm.gpsLocation.location)
            }

            1 -> {
                CompassDashboard(vm.gpsLocation.location, vm.route)
            }

            2 -> {
                TrackDashboard(track = vm.track)
            }

            3 -> {
                RouteDashboard(route = vm.route, location = vm.gpsLocation.location)
            }
        }
    }
}
