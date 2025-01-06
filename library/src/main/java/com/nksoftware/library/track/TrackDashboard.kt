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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LineAxis
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkCombinedChartComponent
import com.nksoftware.library.composables.NkDateFormatterEpoch
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkLineChartComponent
import com.nksoftware.library.composables.NkMatrixCell
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkSingleSelectMenu
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation


val trackTabIcons = listOf(Icons.Outlined.TableView, Icons.Outlined.LineAxis, Icons.Outlined.Landscape)

@Composable
fun TrackDashboard(
   modifier: Modifier = Modifier,
   track: Track
) {
   Column(
      modifier = Modifier.padding(bottom = 8.dp)
   ) {
      NkCardWithHeadline(
         headline = stringResource(R.string.track),
         headline2 = stringResource(R.string.start) + ": " + track.startTrackTime + "    " +
                     stringResource(R.string.end) + ": " + track.endTrackTime,
         icon = Icons.Outlined.Timeline
      ) {
         NkRowNValues(
            modifier = Modifier
               .fillMaxWidth()
               .padding(top = 8.dp),
            arrangement = Arrangement.SpaceBetween
         ) {
            NkInputField(
               modifier = Modifier.width(100.dp),
               value = track.name,
               onValueChange = { s -> track.name = s },
               regex = "^[\\w,\\s-]+\\.[A-Za-z]{3}\$",
               label = stringResource(R.string.name),
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.dist),
               dimension = ExtendedLocation.distanceDimension,
               value = track.appliedDistances.sum(),
            )
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.speed),
               dimension = ExtendedLocation.speedDimension,
               value = track.getTotalSpeed(),
            )
            NkSingleSelectMenu(
               icon = Icons.Outlined.Summarize,
               item = track.granularity,
               action = { track.updateTrack() }
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
                     headline = stringResource(R.string.tracksegments),
                     icon = Icons.Filled.TableView
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
                              value = stringResource(R.string.time),
                              alignment = Alignment.BottomCenter
                           )
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
                              value = stringResource(R.string.speed),
                              subHeader = ExtendedLocation.speedDimension,
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
                           NkMatrixCell(
                              modifier = Modifier
                                 .width(100.dp)
                                 .padding(start = 5.dp, end = 5.dp),
                              value = stringResource(R.string.elev),
                              subHeader = "m",
                              alignment = Alignment.BottomCenter
                           )
                        }

                        LazyRow(
                           modifier = Modifier
                              .background(Color.White)
                              .fillMaxWidth()
                        ) {
                           items(track.timeSlots.size) { index ->
                              Column(
                                 modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 5.dp)
                              ) {
                                 NkMatrixCell(
                                    alignment = Alignment.BottomCenter,
                                    header = ExtendedLocation.getTimeStr(track.timeSlots[index], "dd"),
                                    subHeader = ExtendedLocation.getTimeStr(track.timeSlots[index], "EEE"),
                                    value = ExtendedLocation.getTimeStr(track.timeSlots[index], "HH:mm")
                                 )
                                 NkMatrixCell(
                                    value = index + 1
                                 )
                                 NkMatrixCell(
                                    value = track.appliedDistances[index],
                                    min = track.appliedDistances.min(),
                                    max = track.appliedDistances.max(),
                                    precision = 2
                                 )
                                 NkMatrixCell(
                                    value = track.appliedSpeeds[index],
                                    min = track.appliedSpeeds.min(),
                                    max = track.appliedSpeeds.max(),
                                    precision = 1
                                 )
                                 NkMatrixCell(
                                    value = track.courses[index],
                                    precision = 0
                                 )
                                 NkMatrixCell(
                                    value = track.elevations[index],
                                    min = track.elevations.min(),
                                    max = track.elevations.max(),
                                    precision = 0
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
                        yLineData = track.appliedDistances,
                        lineLabel = stringResource(R.string.dist),
                        yBarData = track.appliedSpeeds,
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
