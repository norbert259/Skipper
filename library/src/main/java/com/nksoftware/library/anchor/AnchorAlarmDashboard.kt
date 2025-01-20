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
 * File: AnchorAlarmDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.anchor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkSlider


@Composable
fun AnchorDashboard(
   anchorAlarm: AnchorAlarm,
   snackBar: (str: String) -> Unit
) {
   Column(modifier = Modifier.padding(bottom = 8.dp)) {

      NkCardWithHeadline(
         modifier = Modifier.fillMaxWidth(),
         headline = stringResource(R.string.anchor_alarm),
         icon = Icons.Outlined.Anchor
      ) {
         NkRowNValues(
            modifier = Modifier.padding(top = 8.dp)
         ) {
            Column {
               NkSlider(
                  text = stringResource(R.string.diameter_of_anchorage, anchorAlarm.anchorageDiameter),
                  value = anchorAlarm.anchorageDiameter,
                  func = { v: Float -> anchorAlarm.anchorageDiameter = v },
                  min = 50.0f,
                  max = 300.0f,
                  steps = 24
               )
            }
         }
      }
   }
}