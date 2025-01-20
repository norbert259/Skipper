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
 * File: WeatherDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.weather

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storm
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Storm
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Thunderstorm
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
import com.nksoftware.library.composables.NkDateFormatterZd
import com.nksoftware.library.composables.NkLineChartComponent
import com.nksoftware.library.composables.NkMatrixCell
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.theme.surfaceDimLight


val weatherTabIcons = listOf(
   Icons.Outlined.TableView, Icons.Outlined.Storm,
   Icons.Outlined.Thermostat, Icons.Outlined.Thunderstorm
)

@Composable
fun getSignificantWeatherIcon(i: Int): ImageVector {
   return when (i) {
      95, 96 -> ImageVector.vectorResource(R.drawable.thunderstorm)
      57, 67 -> ImageVector.vectorResource(R.drawable.freezingrain2)
      56 -> ImageVector.vectorResource(R.drawable.freezingdrizzle)
      66, 77 -> ImageVector.vectorResource(R.drawable.freezingrain)
      73, 75 -> ImageVector.vectorResource(R.drawable.snow)
      83, 84, 85 -> ImageVector.vectorResource(R.drawable.sleet)
      81, 82 -> ImageVector.vectorResource(R.drawable.rain)
      71, 86 -> ImageVector.vectorResource(R.drawable.heavy_snow)
      45, 48, 49 -> ImageVector.vectorResource(R.drawable.fog)
      55, 63, 65 -> ImageVector.vectorResource(R.drawable.heavy_rain)
      51, 61, 80 -> ImageVector.vectorResource(R.drawable.rain)
      53 -> ImageVector.vectorResource(R.drawable.drizzle)
      else -> Icons.Outlined.PriorityHigh
   }
}

@Composable
fun WeatherDashboard(
   weather: Weather,
   location: ExtendedLocation
) {
   var state by remember { mutableIntStateOf(0) }

   Column(
      modifier = Modifier.padding(bottom = 8.dp)
   ) {
      NkCardWithHeadline(
         headline = stringResource(R.string.station_s, weather.stations.selectedStation?.name ?: ""),
         headline2 = stringResource(R.string.issued, weather.forecast?.getIssueTimeStr() ?: ""),
         icon = Icons.Filled.Cloud
      ) {
         Row(
            modifier = Modifier.padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
         ) {
            NkValueField(
               modifier = Modifier.weight(1.2f),
               label = stringResource(R.string.latitude),
               value = ExtendedLocation.convertCoordinate(weather.stations.selectedStation?.lat),
            )
            NkValueField(
               modifier = Modifier.weight(1.2f),
               label = stringResource(R.string.longitude),
               value = ExtendedLocation.convertCoordinate(weather.stations.selectedStation?.lon, vertical = true),
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.elev),
               value = weather.stations.selectedStation?.elevation,
               dimension = "m"
            )
            NkValueField(
               modifier = Modifier.weight(1f),
               label = stringResource(R.string.dist),
               value = ExtendedLocation.applyDistance(
                  weather.stations.selectedStation?.loc?.distanceTo(location)
               ),
               dimension = ExtendedLocation.distanceDimension
            )
         }
      }

      if (weather.forecast?.parsingCompleted == true && weather.stations.selectedStation != null) {

         val start = weather.forecast!!.getStartingTimeSlots()
         val timeSlots = weather.forecast!!.aggregatedTimeslots!!.filterIndexed { i, _ -> i >= start }
         val values = weather.forecast!!.aggregatedValues

         Column {
            NkTabRowIcon(
               tabIndex = state,
               icons = weatherTabIcons,
               set = { s -> state = s }
            )

            when (state) {
               0 -> {
                  val weatherParameters = Forecast.WeatherValues.entries.filter {
                     it !in listOf(Forecast.WeatherValues.EffCloudCover)
                  }

                  NkCardWithHeadline(
                     headline = stringResource(R.string.forecast_table),
                     icon = Icons.Filled.Cloud
                  ) {
                     Row(modifier = Modifier.padding(top = 5.dp)) {
                        Column(
                           modifier = Modifier
                              .padding(end = 5.dp)
                              .background(color = surfaceDimLight)
                        ) {
                           NkMatrixCell(
                              modifier = Modifier
                                 .width(100.dp)
                                 .padding(start = 5.dp, end = 5.dp),
                              value = stringResource(R.string.time),
                              alignment = Alignment.BottomCenter
                           )

                           for (s in weatherParameters) {
                              val attributes = Forecast.Parameters.WeatherValueAttributes[s]

                              NkMatrixCell(
                                 modifier = Modifier
                                    .width(100.dp)
                                    .padding(start = 5.dp, end = 5.dp),
                                 value = stringResource(attributes!!.id),
                                 subHeader = attributes.unit,
                                 alignment = Alignment.BottomCenter
                              )
                           }
                        }

                        LazyRow(
                           modifier = Modifier
                              .background(Color.White)
                              .fillMaxWidth()
                        ) {
                           items(timeSlots.size) { index ->
                              Column(
                                 modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 5.dp)
                              ) {
                                 NkMatrixCell(
                                    alignment = Alignment.BottomCenter,
                                    header = ExtendedLocation.Converters.getTimeStr(timeSlots[index], "dd."),
                                    subHeader = ExtendedLocation.Converters.getTimeStr(timeSlots[index], "EEE"),
                                    value = ExtendedLocation.Converters.getTimeStr(timeSlots[index], "HH:ss")
                                 )

                                 for (pwt in weatherParameters) {
                                    val data = values[pwt]?.filterIndexed { index, _ -> index >= start }

                                    if (pwt == Forecast.WeatherValues.SignificantWeather) {
                                       val significantWeather = data?.get(index)?.toInt() ?: 0

                                       val value = when (significantWeather) {
                                          0, 1, 2, 3 -> ""
                                          else       -> getSignificantWeatherIcon(i = significantWeather)
                                       }

                                       NkMatrixCell(value = value, color = Color.Red)
                                    } else {
                                       NkMatrixCell(
                                          value = data?.get(index),
                                          min = data?.min(),
                                          max = data?.max()
                                       )
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               1 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.windforecast),
                     icon = Icons.Filled.Storm
                  ) {
                     if (weather.forecast?.parsingCompleted == true && weather.stations.selectedStation != null) {

                        val windSpeed = values[Forecast.WeatherValues.WindSpeed]?.filterIndexed { i, _ -> i >= start }
                        val windGusts = values[Forecast.WeatherValues.WindGust]?.filterIndexed { i, _ -> i >= start }
                        val windDirection = values[Forecast.WeatherValues.WindDirection]?.filterIndexed { i, _ -> i >= start }

                        NkCombinedChartComponent(
                           modifier = Modifier
                              .height(300.dp)
                              .padding(top = 5.dp),
                           xData = List(timeSlots.size) { index -> index.toFloat() },
                           xFormatter = NkDateFormatterZd(timeSlots = timeSlots, format = "dd. HH:"),
                           yLineData = windSpeed,
                           lineLabel = stringResource(R.string.windspeed),
                           yBarData = windGusts,
                           barLabel = stringResource(R.string.windgust),
                           secondAxis = false,
                           iconResource = R.drawable.sharp_arrow_upward_alt_24,
                           rotationData = windDirection
                        )
                     }
                  }
               }

               2 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.temperaturetrend),
                     icon = Icons.Filled.Thermostat
                  ) {
                     if (weather.forecast?.parsingCompleted == true && weather.stations.selectedStation != null) {

                        val temperature = values[Forecast.WeatherValues.Temperature]?.filterIndexed { i, _ -> i >= start }
                        val dewpoint = values[Forecast.WeatherValues.Dewpoint]?.filterIndexed { i, _ -> i >= start }

                        NkLineChartComponent(
                           modifier = Modifier
                              .height(300.dp)
                              .padding(top = 5.dp),
                           xData = List(timeSlots.size) { index -> index.toFloat() },
                           xFormatter = NkDateFormatterZd(timeSlots = timeSlots, format = "dd. HH:"),
                           yData1 = dewpoint,
                           dataLabel1 = stringResource(R.string.dewpoint),
                           yData2 = temperature,
                           dataLabel2 = stringResource(R.string.temperature),
                           secondAxis = false
                        )
                     }
                  }
               }

               3 -> {
                  NkCardWithHeadline(
                     headline = stringResource(R.string.forecastrain),
                     icon = Icons.Filled.Thunderstorm
                  ) {
                     if (weather.forecast?.parsingCompleted == true && weather.stations.selectedStation != null) {

                        val cloudCover = values[Forecast.WeatherValues.TotCloudCover]?.filterIndexed { i, _ ->
                           i >= start
                        }
                        val precipitation = values[Forecast.WeatherValues.Precipitation]?.filterIndexed { i, _ ->
                           i >= start
                        }
                        NkCombinedChartComponent(
                           modifier = Modifier
                              .height(300.dp)
                              .padding(top = 5.dp),
                           xData = List(timeSlots.size) { index -> index.toFloat() },
                           xFormatter = NkDateFormatterZd(timeSlots = timeSlots, format = "dd. HH:"),
                           yLineData = cloudCover,
                           lineLabel = stringResource(R.string.totcloudcover),
                           yBarData = precipitation,
                           barLabel = stringResource(R.string.precipitation)
                        )
                     }
                  }
               }

            }
         }
      }
   }
}
