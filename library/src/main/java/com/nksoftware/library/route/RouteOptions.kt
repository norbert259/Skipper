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
 * File: RouteOptions.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.route

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkIconToggleButton
import com.nksoftware.library.composables.NkSlider


@Composable
fun RouteOptions(route: Route) {

   NkCardWithHeadline(
      headline = stringResource(R.string.supposed_tack_angle),
      headline2 = "%.0f °".format(route.tackAngle),
      icon = Outlined.Settings
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically
      ) {
         NkIconToggleButton(
            modifier = Modifier.padding(end = 10.dp),
            icon = Icons.Filled.Sailing,
            contentDescription = "Show tack lines",
            checked = route.sailingMode,
            onCheckedChange = { route.sailingMode = !route.sailingMode }
         )

         NkSlider(
            value = route.tackAngle,
            func = { v: Float -> route.tackAngle = v },
            min = 60.0f,
            max = 120.0f,
            steps = 11
         )
      }
   }
}