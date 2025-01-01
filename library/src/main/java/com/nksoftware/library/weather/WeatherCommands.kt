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
 * File: WeatherCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.weather

import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.location.ExtendedLocation


@Composable
fun WeatherCommands(weather: Weather, location: ExtendedLocation, snackBar: (str: String) -> Unit) {

   NkFloatingActionButton(
      onClick = { weather.downloadNearestStation(location, snackBar) },
      icon = Outlined.Download,
      contentDescription = "Download weather data from station"
   )

   if (weather.forecast != null) {
      NkFloatingActionButton(
         onClick = { weather.clear() },
         icon = Outlined.Delete,
         contentDescription = "Clear weather forecast"
      )
   }
}