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
 * File: AstronavigationDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.astronavigation

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.moon.Moon
import com.nksoftware.library.moon.MoonDashBoard
import com.nksoftware.library.sun.Sun
import com.nksoftware.library.sun.SunDashboard

val astroTabIcons = listOf(Icons.Outlined.Architecture, Icons.Outlined.WbSunny, Icons.Outlined.ModeNight)

@Composable
fun AstronavigationDashboard(
   astroNavigation: AstroNavigation,
   sun: Sun,
   moon: Moon,
   loc: Location,
   snackBar: (str: String) -> Unit
) {

   var state by remember { mutableIntStateOf(0) }

   Column(modifier = Modifier.padding(bottom = 8.dp)) {
      NkTabRowIcon(tabIndex = state, icons = astroTabIcons, set = { s -> state = s })

      when (state) {
         0 -> { AstroDashboard(astroNavigation = astroNavigation, sun = sun, loc =  loc, snackBar) }
         1 -> {
            SunDashboard(sun, loc, astroNavigation)
         }
         2 -> {
            MoonDashBoard(moon, loc)
         }
      }
   }
}
