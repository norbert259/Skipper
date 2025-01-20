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
 * File: TrackDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LineAxis
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkCombinedChartComponent
import com.nksoftware.library.composables.NkDateFormatterEpoch
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkLineChartComponent
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.composables.NkTableCell
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation


val trackTabIcons = listOf(Icons.Outlined.TableView, Icons.Outlined.LineAxis, Icons.Outlined.Landscape)

@Composable
fun TrackDashboard(
   modifier: Modifier = Modifier,
   track: Track
) {

   val blueDot = ImageVector.vectorResource(R.drawable.circle_blue)

   Column(
      modifier = Modifier.padding(bottom = 5.dp)
   ) {
      NkCardWithHeadline(
         headline = stringResource(R.string.track),
         headline2 = stringResource(R.string.start) + ": " + track.startTrackTime + "    " +
                     stringResource(R.string.end) + ": " + track.endTrackTime,
         icon = Icons.Outlined.Timeline
      ) {
         Row(
            modifier = Modifier.padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkInputField(
               modifier = Modifier.weight(1.5f),
               value = track.name,
               onValueChange = { s -> track.name = s },
               regex = "^[\\w,\\s-]+\\.[A-Za-z]{3}\$",
               label = stringResource(R.string.name)
            )
            NkValueField(
               modifier = Modifier.weight(1.0f),
               label = stringResource(R.string.duration),
               value = track.getDuration(),
            )
            NkValueField(
               modifier = Modifier.weight(1.0f),
               label = stringResource(R.string.dist),
               dimension = ExtendedLocation.distanceDimension,
               value = ExtendedLocation.applyDistance(track.distances.sum()),
            )
            NkValueField(
               modifier = Modifier.weight(1.0f),
               label = stringResource(R.string.speed),
               dimension = ExtendedLocation.speedDimension,
               value = track.getTotalSpeed()
            )
         }
      }

      if (track.isNotEmpty) {
         var state by remember { mutableIntStateOf(0) }

         Column(modifier = modifier) {
            NkTabRowIcon(
               tabIndex = state,
               icons = trackTabIcons,
               set = { s -> state = s }
            )

            when (state) {
               0 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.trackpoints, track.size),
                     icon = Icons.Filled.TableView
                  ) {
                     Row(
                        modifier = Modifier.padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                     ) {
                        NkTableCell(
                           modifier = Modifier.weight(0.8f),
                           value = stringResource(R.string.no),
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1.0f),
                           value = stringResource(R.string.time),
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1.0f),
                           value = stringResource(R.string.dist),
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1.0f),
                           value = stringResource(R.string.speed),
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1.0f),
                           value = stringResource(R.string.course),
                        )
                        NkTableCell(
                           modifier = Modifier.weight(1.0f),
                           value = stringResource(R.string.elev),
                        )
                     }

                     HorizontalDivider()

                     Column(
                        modifier = Modifier
                           .background(Color.White)
                           .height(90.dp)
                           .fillMaxWidth()
                     ) {
                        LazyColumn(
                           modifier = Modifier
                              .background(Color.White)
                              .fillMaxWidth()
                        ) {
                           items(track.timeSlots.size) { index ->
                              Row(
                                 modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 3.dp),
                                 verticalAlignment = Alignment.CenterVertically,
                                 horizontalArrangement = Arrangement.spacedBy(5.dp)
                              ) {
                                 NkTableCell(
                                    modifier = Modifier.weight(0.8f),
                                    value = index + 1,
                                    icon = blueDot
                                 )
                                 NkTableCell(
                                    modifier = Modifier.weight(1.0f),
                                    value = if (index > 0)
                                       ExtendedLocation.getTimeStr(track.timeSlots[index], "EEE HH:mm")
                                    else
                                       ExtendedLocation.getTimeStr(track.track[index].time, "EEE HH:mm")
                                 )
                                 NkTableCell(
                                    modifier = Modifier.weight(1.0f),
                                    value = if (index > 0)
                                       "%.1f %s".format(
                                          ExtendedLocation.applyDistance(track.distances[index - 1]),
                                          ExtendedLocation.distanceDimension
                                       ) else "",
                                 )
                                 NkTableCell(
                                    modifier = Modifier.weight(1.0f),
                                    value = if (index > 0)
                                       "%.1f %s".format(
                                          ExtendedLocation.applySpeed(track.speeds[index - 1]),
                                          ExtendedLocation.speedDimension
                                       ) else "",
                                 )
                                 NkTableCell(
                                    modifier = Modifier.weight(1.0f),
                                    value = if (index > 0) "%.0f °".format(track.courses[index - 1]) else "",
                                 )
                                 NkTableCell(
                                    modifier = Modifier.weight(1.0f),
                                    value = if (index > 0) "%.0f m".format(track.elevations[index - 1]) else "",
                                 )
                              }
                           }
                        }
                     }
                  }
               }

               1 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.track_distances),
                     icon = Icons.Outlined.Timeline
                  ) {
                     NkCombinedChartComponent(
                        modifier = Modifier
                           .height(300.dp)
                           .padding(top = 5.dp),
                        xData = List(track.distances.size) { index -> index.toFloat() },
                        xFormatter = NkDateFormatterEpoch(timeSlots = track.timeSlots),
                        yLineData = track.distances.map { ExtendedLocation.applyDistance(it)!! },
                        lineLabel = stringResource(R.string.dist),
                        yBarData = track.speeds.map { ExtendedLocation.applySpeed(it) },
                        barLabel = stringResource(R.string.speed)
                     )
                  }
               }

               2 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.track_elevation),
                     icon = Icons.Outlined.Timeline
                  ) {
                     NkLineChartComponent(
                        modifier = Modifier
                           .height(300.dp)
                           .padding(top = 5.dp),
                        xData = List(track.elevations.size) { index -> index.toFloat() },
                        xFormatter = NkDateFormatterEpoch(timeSlots = track.timeSlots),
                        yData1 = track.elevations,
                        dataLabel1 = stringResource(R.string.elev)
                     )
                  }
               }
            }
         }
      }
   }
}
