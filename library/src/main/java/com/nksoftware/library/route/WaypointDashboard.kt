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
 * File: WaypointDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation


@Composable
fun WaypointDashboard(location: ExtendedLocation, route: Route) {

   val wp = route.selectedPoint

   if (wp != null) {
      NkCardWithHeadline(
         headline = stringResource(R.string.waypoint),
         headline2 = "${wp.latStr}    ${wp.lonStr}",
         icon = Icons.Outlined.Directions
      ) {
         NkRowNValues(
            modifier = Modifier.padding(top = 5.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkValueField(
               modifier = Modifier.width(70.dp),
               label = "VMG",
               dimension = ExtendedLocation.speedDimension,
               value = location.getAppliedVelocityMadeGood(wp)
            )

            NkValueField(
               modifier = Modifier.width(60.dp),
               label = stringResource(R.string.bearing),
               dimension = "°",
               value = location.getAppliedBearing(wp),
            )

            NkValueField(
               modifier = Modifier.width(60.dp),
               label = stringResource(R.string.dist),
               value = location.getAppliedDistance(wp),
               dimension = ExtendedLocation.distanceDimension
            )

            NkValueField(
               modifier = Modifier.width(140.dp),
               label = "ETA",
               value = location.getEta(wp)
            )
         }
      }
   }
}