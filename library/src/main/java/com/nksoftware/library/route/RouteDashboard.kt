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
 * File: RouteDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkTableCell
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation


@Composable
fun RouteDashboard(
   modifier: Modifier = Modifier,
   route: Route,
   location: ExtendedLocation
) {
   val redDot = ImageVector.vectorResource(R.drawable.circle_red)
   val yellowDot = ImageVector.vectorResource(R.drawable.circle_yellow)

   Column(
      modifier = modifier.padding(bottom = 5.dp),
   ) {
      NkCardWithHeadline(
         headline = stringResource(R.string.route),
         icon = Icons.Filled.Route
      ) {
         Row(
            modifier = Modifier.padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkInputField(
               modifier = Modifier.weight(1.5f),
               value = route.name,
               onValueChange = { n -> route.name = n },
               regex = "^[\\w,\\s-]+\\.[A-Za-z]{3}\$",
               label = stringResource(R.string.name),
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.points),
               value = route.size
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.total),
               dimension = ExtendedLocation.distanceDimension,
               value = ExtendedLocation.applyDistance(route.getLength()),
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.remaining),
               dimension = ExtendedLocation.distanceDimension,
               value = ExtendedLocation.applyDistance(
                  route.getRemainingLength(location)
               )
            )
         }
      }

      NkCardWithHeadline(
         headline = stringResource(R.string.waypoints),
         icon = Icons.Filled.Route
      ) {
         Row(
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkTableCell(
               modifier = Modifier.weight(0.6f),
               value = stringResource(R.string.no),
            )
            NkTableCell(
               modifier = Modifier.weight(1f),
               value = stringResource(R.string.latitude)
            )
            NkTableCell(
               modifier = Modifier.weight(1f),
               value = stringResource(R.string.longitude)
            )
            NkTableCell(
               modifier = Modifier.weight(1f),
               value = stringResource(R.string.dist),
            )
            NkTableCell(
               modifier = Modifier.weight(0.8f),
               value = stringResource(R.string.course),
            )
            NkTableCell(
               modifier = Modifier.weight(0.8f),
               value = stringResource(R.string.wpelevationdiff),
            )
         }

         HorizontalDivider()

         if (route.size > 0) {
            val distances = route.getDistances()
            val courses = route.getCourses()
            val elevations = route.getElevations()

            Column(
               modifier = Modifier
                  .background(Color.White)
                  .height(90.dp)
                  .fillMaxWidth()
            ) {
               LazyColumn {
                  items(route.size) { index ->
                     Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                     ) {
                        NkTableCell(
                           modifier = Modifier.weight(0.6f),
                           value = index + 1,
                           icon = if (index == route.selectedRoutePoint) yellowDot else redDot
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1f),
                           value = route.routePoints[index].latStr
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1f),
                           value = route.routePoints[index].lonStr
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1f),
                           value = if (index > 0) "%.1f %s".format(
                              distances[index - 1],
                              ExtendedLocation.distanceDimension
                           ) else "",
                           precision = 1
                        )
                        NkTableCell(
                           modifier = Modifier.weight(0.8f),
                           value = if (index > 0) "%.0f °".format(courses[index - 1]) else ""
                        )
                        NkTableCell(
                           modifier = Modifier.weight(0.8f),
                           value = if (index > 0) elevations[index - 1] else "",
                        )
                     }
                  }
               }
            }
         }
      }
   }
}
