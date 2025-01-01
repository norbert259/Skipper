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
 * File: GribDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.grib

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LastPage
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FirstPage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkIconButton
import com.nksoftware.library.composables.NkPopupMenu
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation
import kotlin.math.max
import kotlin.math.min
import com.nksoftware.library.R


@Composable
fun GribParameter(
   modifier: Modifier = Modifier,
   gribFile: GribFile,
) {
   var gribTimeslotIndex by rememberSaveable { mutableIntStateOf(0) }

   NkCardWithHeadline(
      modifier = modifier,
      headline = "Grib Parameter: ${gribFile.actualGribParameter}",
      icon = Icons.Filled.Settings
   ) {
      NkRowNValues(
         arrangement = Arrangement.spacedBy(5.dp)
      ) {
         NkPopupMenu(
            icon = Icons.Outlined.Settings,
            list = gribFile.gribParameterList,
            set = { v: Int -> gribFile.setParameter(v) }
         )
         NkIconButton(
            onClick = {
               gribTimeslotIndex = 0
               gribFile.setTimeslot(gribTimeslotIndex)
            },
            icon = Icons.Outlined.FirstPage
         )
         NkIconButton(
            onClick = {
               gribTimeslotIndex = max(gribTimeslotIndex - 1, 0)
               gribFile.setTimeslot(gribTimeslotIndex)
            },
            icon = Icons.Outlined.ChevronLeft,
            contentDescription = "Previous time slot"
         )
         NkValueField(
            modifier = Modifier.width(100.dp),
            label = stringResource(R.string.time),
            value = gribFile.getTimeslotStr()
         )
         NkIconButton(
            onClick = {
               gribTimeslotIndex = min(gribTimeslotIndex + 1, gribFile.gribTimeslotList.size - 1)
               gribFile.setTimeslot(gribTimeslotIndex)
            },
            icon = Icons.Outlined.ChevronRight,
            contentDescription = "Next time slot"
         )
         NkIconButton(
            onClick = { gribTimeslotIndex = gribFile.gribTimeslotList.size - 1
               gribFile.setTimeslot(gribTimeslotIndex)
            },
            icon = Icons.AutoMirrored.Outlined.LastPage,
            contentDescription = "Previous time slot"
         )
      }
   }
}


@Composable
fun GribDashboard(
   gribFile: GribFile,
   snackBar: (str: String) -> Unit
) {
   Column(modifier = Modifier.padding(bottom = 8.dp)) {

      NkCardWithHeadline(
         headline = stringResource(R.string.grib_file, gribFile.gribFileName),
         icon = Icons.Filled.GridOn
      ) {
         NkRowNValues(
            modifier = Modifier.padding(top = 5.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkValueField(
               modifier = Modifier.width(140.dp),
               label = stringResource(R.string.start),
               value = gribFile.getFirstTime(),
            )
            NkValueField(
               modifier = Modifier.width(140.dp),
               label = stringResource(R.string.end),
               value = gribFile.getLastTime()
            )
         }

         NkRowNValues(
            modifier = Modifier.padding(top = 5.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkValueField(
               modifier = Modifier.width(120.dp),
               label = "Min. " + stringResource(R.string.lat),
               value = ExtendedLocation.convertCoordinate(gribFile.minLat),
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.no),
               value = gribFile.latPts,
            )
            NkValueField(
               modifier = Modifier.width(120.dp),
               label = "Max. " + stringResource(R.string.lat),
               value = ExtendedLocation.convertCoordinate(gribFile.maxLat),
            )
         }

         NkRowNValues(
            modifier = Modifier.padding(top = 5.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkValueField(
               modifier = Modifier.width(120.dp),
               label = "Min. " + stringResource(R.string.lon),
               value = ExtendedLocation.convertCoordinate(gribFile.minLon, vertical = false),
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.no),
               value = gribFile.lonPts,
            )
            NkValueField(
               modifier = Modifier.width(120.dp),
               label = "Max. " + stringResource(R.string.lon),
               value = ExtendedLocation.convertCoordinate(gribFile.maxLon, vertical = false),
            )
         }
      }

      if (gribFile.gribFileLoaded)
         GribParameter(gribFile = gribFile)
   }
}
