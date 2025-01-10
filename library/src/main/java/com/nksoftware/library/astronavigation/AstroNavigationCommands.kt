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
 * File: AstroNavigationCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.astronavigation

import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HdrAuto
import androidx.compose.runtime.Composable
import com.nksoftware.library.composables.NkFloatingActionButton


@Composable
fun AstroNavigationCommands(astronav: AstroNavigation, loc: Location, snackBar: (String) -> Unit) {
   NkFloatingActionButton(
      onClick = { astronav.addAutoFix(loc, snackBar) },
      icon = Icons.Outlined.HdrAuto,
      contentDescription = "Previous fix"
   )
}
