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
 * File: ExtendedLocationDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkValueField


@Composable
fun ExtendedLocationDashboard(location: ExtendedLocation) {

   NkCardWithHeadline(
      modifier = Modifier.padding(bottom = 5.dp),
      headline = stringResource(R.string.location),
      headline2 = "${location.latStr}    ${location.lonStr}",
      icon = Icons.Outlined.GpsFixed
   ) {
      NkRowNValues(
         modifier = Modifier.padding(top = 5.dp),
         arrangement = Arrangement.SpaceBetween
      ) {
         NkValueField(
            modifier = Modifier.width(70.dp),
            label = stringResource(R.string.speed),
            dimension = ExtendedLocation.speedDimension,
            value = location.appliedSpeed
         )
         NkValueField(
            modifier = Modifier.width(60.dp),
            label = stringResource(R.string.heading),
            dimension = "°",
            value = location.getHeading(),
         )
         NkValueField(
            modifier = Modifier.width(60.dp),
            label = stringResource(R.string.altitude),
            dimension = "m",
            value = location.altitude,
         )
         NkValueField(
            modifier = Modifier.width(140.dp),
            label = stringResource(R.string.time),
            value = location.timeStr
         )
      }
   }

}