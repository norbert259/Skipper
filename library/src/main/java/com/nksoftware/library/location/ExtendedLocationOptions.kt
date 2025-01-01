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
 * File: ExtendedLocationOptions.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.location

import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkCheckBoxItem
import com.nksoftware.library.composables.NkSingleSelect
import com.nksoftware.library.R


@Composable
fun ExtendedLocationOptions() {

   NkCardWithHeadline(headline = stringResource(R.string.time_zone), icon = Outlined.Settings) {
      NkSingleSelect(item = ExtendedLocation.timezone)
   }

   NkCardWithHeadline(headline = stringResource(R.string.dimensions), icon = Outlined.Settings) {
      NkSingleSelect(item = ExtendedLocation.dimensions)
   }

   NkCardWithHeadline(headline = stringResource(R.string.format_for_gps_coordinates), icon = Outlined.Settings) {
      NkSingleSelect(item = ExtendedLocation.coordinatesFormat)
   }

   NkCardWithHeadline(
      headline = stringResource(R.string.magnetic_deviation),
      headline2 = "%.1f %s".format(
         ExtendedLocation.declination,
         if (ExtendedLocation.declination > 0) "E" else "W"
      ),
      icon = Outlined.Settings
   ) {
      NkCheckBoxItem(
         item = stringResource(R.string.show_magnetic_heading_bearing),
         selected = ExtendedLocation.magneticHeading,
         set = { ExtendedLocation.magneticHeading = !ExtendedLocation.magneticHeading }
      )
   }
}