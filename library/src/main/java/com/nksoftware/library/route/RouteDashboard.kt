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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkMatrixCell
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation


@Composable
fun RouteDashboard(
   modifier: Modifier = Modifier,
   route: Route,
   location: ExtendedLocation
) {
   Column(
      modifier = modifier.padding(bottom = 8.dp),
   ) {
      NkCardWithHeadline(
         headline = stringResource(R.string.route),
         icon = Icons.Filled.Route
      ) {
         NkRowNValues(
            modifier = Modifier.padding(top = 5.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkInputField(
               modifier = Modifier.width(100.dp),
               value = route.name,
               onValueChange = { n -> route.name = n },
               regex = "^[\\w,\\s-]+\\.[A-Za-z]{3}\$",
               label = stringResource(R.string.name),
            )
            NkValueField(
               modifier = Modifier.width(60.dp),
               label = stringResource(R.string.points),
               value = route.size
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.total),
               dimension = ExtendedLocation.distanceDimension,
               value = ExtendedLocation.applyDistance(route.getLength())
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.remaining),
               dimension = ExtendedLocation.distanceDimension,
               value = ExtendedLocation.applyDistance(route.getRemainingLength(location)
               )
            )
         }
      }

      if (route.size > 2) {

         val distances = route.getDistances()
         val courses = route.getCourses()
         val elevations = route.getElevations()

         NkCardWithHeadline(
            headline = stringResource(R.string.routesegements),
            icon = Icons.Filled.Route
         ) {
            Row(modifier = Modifier.padding(top = 5.dp)) {
               Column(
                  modifier = Modifier
                     .padding(end = 5.dp)
                     .background(color = Color.LightGray)
               ) {
                  NkMatrixCell(
                     modifier = Modifier
                        .width(100.dp)
                        .padding(start = 5.dp, end = 5.dp),
                     value = stringResource(R.string.no),
                     alignment = Alignment.BottomCenter
                  )
                  NkMatrixCell(
                     modifier = Modifier
                        .width(100.dp)
                        .padding(start = 5.dp, end = 5.dp),
                     value = stringResource(R.string.dist),
                     subHeader = ExtendedLocation.distanceDimension,
                     alignment = Alignment.BottomCenter
                  )
                  NkMatrixCell(
                     modifier = Modifier
                        .width(100.dp)
                        .padding(start = 5.dp, end = 5.dp),
                     value = stringResource(R.string.course),
                     subHeader = "°",
                     alignment = Alignment.BottomCenter
                  )

                  if (elevations.any { it != null }) {
                     NkMatrixCell(
                        modifier = Modifier
                           .width(100.dp)
                           .padding(start = 5.dp, end = 5.dp),
                        value = stringResource(R.string.wpelevationdiff),
                        subHeader = "m",
                        alignment = Alignment.BottomCenter
                     )
                  }
               }

               LazyRow(
                  modifier = Modifier
                     .background(Color.White)
                     .fillMaxWidth()
               ) {
                  items(if (route.size > 1) route.size - 1 else 0)
                  { index ->
                     Column(
                        modifier = Modifier
                           .fillMaxWidth()
                           .padding(end = 5.dp)
                     ) {
                        NkMatrixCell(
                           value = index + 1
                        )
                        NkMatrixCell(
                           value = distances[index],
                           min = distances.min(),
                           max = distances.max(),
                           precision = 1
                        )
                        NkMatrixCell(
                           value = courses[index]
                        )

                        if (elevations.all { it != null }) {
                           NkMatrixCell(
                              value = elevations[index],
                              min = elevations.filterNotNull().min(),
                              max = elevations.filterNotNull().max()
                           )
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
