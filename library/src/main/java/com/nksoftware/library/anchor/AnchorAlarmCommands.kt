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
 * File: AnchorAlarmCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.anchor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.theme.primaryLight


@Composable
fun AnchorAlarmCommands(anchorAlarm: AnchorAlarm, location: ExtendedLocation) {

   NkFloatingActionButton(
      onClick = { anchorAlarm.anchorPoint = location },
      icon = Icons.Outlined.LocationOn,
      contentDescription = "Set anchor point to current location"
   )

   NkFloatingActionButton(onClick = { anchorAlarm.toggleAlarm() }) {
      Icon(
         imageVector = Icons.Filled.Anchor,
         contentDescription = "Set anchor alarm",
         tint = if (anchorAlarm.anchorAlarmSet) Color.Red else primaryLight
      )
   }

}