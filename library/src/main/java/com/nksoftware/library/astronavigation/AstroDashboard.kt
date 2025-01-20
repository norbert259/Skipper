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
 * File: AstroDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.astronavigation

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkIconButton
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.sun.Sun


@Composable
fun AstroDashboard(astroNavigation: AstroNavigation, sun: Sun, loc: Location, snackBar: (str: String) -> Unit) {
   NkCardWithHeadline(
      headline = stringResource(R.string.sun_edge, astroNavigation.sunEdge),
      headline2 = stringResource(
         R.string.noon,
         sun.getSunNoonStr(loc = loc, eyeLevel = astroNavigation.eyeLevel.toDouble())
      ),
      icon = Icons.Outlined.Architecture
   ) {
      Row(
         modifier = Modifier.fillMaxWidth(),
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically
      ) {
         Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
         ) {
            NkIconButton(
               onClick = { astroNavigation.addFix(snackBar) },
               icon = Icons.Outlined.Timer,
               contentDescription = "Set time of measurement",
            )
            NkIconButton(
               onClick = { astroNavigation.previousFix() },
               enabled = astroNavigation.fixes.size > 0,
               icon = Icons.Outlined.ArrowCircleLeft,
               contentDescription = "Previous fix"
            )
            NkIconButton(
               onClick = { astroNavigation.deleteFix() },
               enabled = astroNavigation.fixes.size > 0,
               icon = Icons.Outlined.Delete,
               contentDescription = "Delete fix"
            )
            NkIconButton(
               onClick = { astroNavigation.nextFix() },
               enabled = astroNavigation.fixes.size > 0,
               icon = Icons.Outlined.ArrowCircleRight,
               contentDescription = "Next fix"
            )
         }

         Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkInputField(
               modifier = Modifier.width(45.dp),
               value = astroNavigation.measurementDegree.toString(),
               onValueChange = { i -> astroNavigation.measurementDegree = i.toInt() },
               regex = "[0-9]|[0-9][0-9]|[1][0-2][0-9]",
               label = "Fix",
               dimension = "°"
            )
            NkInputField(
               modifier = Modifier.width(45.dp),
               value = astroNavigation.measurementMinutes.toString(),
               onValueChange = { i -> astroNavigation.measurementMinutes = i.toInt() },
               regex = "[0-9]|[0-5][0-9]",
               label = "Fix",
               dimension = "'"
            )
            NkInputField(
               modifier = Modifier.width(45.dp),
               value = astroNavigation.measurementSeconds.toString(),
               onValueChange = { i -> astroNavigation.measurementSeconds = i.toInt() },
               regex = "[0-9]|[0-5][0-9]",
               label = "Fix",
               dimension = "\""
            )
         }
      }
   }

   if (astroNavigation.fixes.isNotEmpty()) {
      val fix = astroNavigation.fixes[astroNavigation.selectedFix]

      NkCardWithHeadline(
         headline = "${stringResource(R.string.fix)}(${astroNavigation.selectedFix + 1}): " +
                    astroNavigation.fixType.value,
         headline2 = "${stringResource(R.string.time)}: ${fix.timeStr}",
         icon = Icons.Outlined.Timer
      ) {
         Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.measure),
               value = fix.heightStr
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.corr_meas),
               value = fix.finalHeightStr
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.grt),
               value = fix.grtStr
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.declination),
               value = fix.dStr
            )
         }

         if (astroNavigation.fixType.index == 1) {
            NkRowNValues(
               modifier = Modifier.padding(top = 8.dp),
               arrangement = Arrangement.spacedBy(10.dp)
            ) {
               NkInputField(
                  modifier = Modifier.width(80.dp),
                  value = fix.course.toString(),
                  onValueChange = { i -> fix.course = i.toInt() },
                  regex = "[0-9]|[0-9][0-9]|[0-3][0-6][0-9]",
                  label = stringResource(R.string.course),
                  dimension = "°"
               )
               NkInputField(
                  modifier = Modifier.width(80.dp),
                  value = fix.speed.toString(),
                  onValueChange = { i -> fix.speed = i.toFloat() },
                  regex = "[0-9]|[0-9][0-9]|[0-9].[0-9]",
                  label = stringResource(R.string.speed),
                  dimension = "kn"
               )
            }
         }
      }
   }

   if (astroNavigation.fixes.isNotEmpty() && astroNavigation.fixType.index == FixType.FixAtNoon.ordinal) {
      val fix = astroNavigation.fixes[astroNavigation.selectedFix]

      NkCardWithHeadline(
         headline = "${stringResource(R.string.location)} ${astroNavigation.fixType.value}",
         headline2 = stringResource(R.string.time_str, fix.timeStr),
         icon = Icons.Outlined.LocationOn
      ) {
         NkRowNValues(
            arrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
         ) {
            NkValueField(
               modifier = Modifier.width(110.dp),
               label = stringResource(R.string.latitude),
               value = fix.finalLatitudeStr
            )
            NkValueField(
               modifier = Modifier.width(110.dp),
               label = stringResource(R.string.longitude),
               value = fix.grtLongitudeStr
            )
         }
      }
   }

   if (astroNavigation.fixes.size > 1 && astroNavigation.fixType.index == FixType.TwoPtFix.ordinal) {

      NkCardWithHeadline(
         headline = "${stringResource(R.string.location)}: ${astroNavigation.fixType.value}",
         icon = Icons.Outlined.LocationOn
      ) {
         NkRowNValues(
            arrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 5.dp)
         ) {
            NkValueField(
               modifier = Modifier.width(110.dp),
               label = stringResource(R.string.latitude),
               value = ExtendedLocation.convertCoordinate(astroNavigation.latitude, vertical = true)
            )
            NkValueField(
               modifier = Modifier.width(110.dp),
               label = stringResource(R.string.longitude),
               value = ExtendedLocation.convertCoordinate(astroNavigation.longitude, vertical = false)
            )
         }
      }
   }
}
