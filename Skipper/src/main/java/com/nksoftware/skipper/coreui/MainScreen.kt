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
 * File: MainScreen.kt
 * Last modified: 06.02.25, 10:55
 *
 */

package com.nksoftware.skipper.coreui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MultilineChart
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nksoftware.library.anchor.AnchorAlarmCommands
import com.nksoftware.library.anchor.AnchorDashboard
import com.nksoftware.library.astronavigation.AstroNavigationCommands
import com.nksoftware.library.astronavigation.AstroNavigationOptions
import com.nksoftware.library.astronavigation.AstronavigationDashboard
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.composables.NkScaffold
import com.nksoftware.library.composables.NkTabRowIcon
import com.nksoftware.library.grib.GribCommands
import com.nksoftware.library.grib.GribDashboard
import com.nksoftware.library.location.CompassDashboard
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.location.ExtendedLocationDashboard
import com.nksoftware.library.location.ExtendedLocationOptions
import com.nksoftware.library.map.OsmMapOptions
import com.nksoftware.library.map.OsmMapView
import com.nksoftware.library.route.RouteCommands
import com.nksoftware.library.route.RouteDashboard
import com.nksoftware.library.route.RouteOptions
import com.nksoftware.library.route.WaypointDashboard
import com.nksoftware.library.saildocs.SaildocsOptions
import com.nksoftware.library.track.TrackCommands
import com.nksoftware.library.track.TrackDashboard
import com.nksoftware.library.weather.WeatherCommands
import com.nksoftware.library.weather.WeatherDashboard
import com.nksoftware.library.weather.WeatherOption
import com.nksoftware.skipper.core.SkipperViewModel
import com.nksoftware.skipper.core.SkipperViewModelOptions


enum class ScreenMode { Navigation, Anchor, Weather, Grib, AstroNavigation }

@Composable
fun MainScreen(viewModel: SkipperViewModel, finish: () -> Unit) {

    var mode by rememberSaveable { mutableStateOf(ScreenMode.Navigation) }

    val ctx = LocalContext.current
    val mapView by remember { mutableStateOf(
        OsmMapView(ctx, viewModel.map, ctx.applicationInfo.dataDir,
            viewModel.gpsLocation.location,
            { lat, lon -> viewModel.gpsLocation.update(loc = ExtendedLocation(lat, lon)) }
        )
    ) }


    NkScaffold(
        "Skipper",

        topButtons = {
            TopAppBar(
                mode,
                { m -> mode = m },
                listOf(
                    ScreenMode.Navigation, ScreenMode.Anchor, ScreenMode.Weather,
                    ScreenMode.Grib, ScreenMode.AstroNavigation
                )
            )
        },

        optionContent = { snackBar ->
            when (mode) {
                ScreenMode.Navigation -> {
                    ExtendedLocationOptions()
                    SkipperViewModelOptions(viewModel)
                    RouteOptions(viewModel.route)
                    OsmMapOptions(mapView, viewModel.map, snackBar)
                }

                ScreenMode.Anchor -> {
                    AnchorDashboard(viewModel.anchorAlarm, snackBar)
                }

                ScreenMode.Weather -> {
                    WeatherOption()
                }

                ScreenMode.Grib -> {
                    SaildocsOptions(viewModel.sailDocs)
                }

                ScreenMode.AstroNavigation -> {
                    AstroNavigationOptions(viewModel.astroNav)
                }
            }
        },

        content = { snackBar ->
            OsmMapScreen(
                vm = viewModel,
                mapView = mapView,
                mode = mode,
                topCommands = {
                    when (mode) {
                        ScreenMode.Navigation -> {
                            TrackCommands(viewModel.track, snackBar)
                        }

                        ScreenMode.Anchor -> {
                            AnchorAlarmCommands(
                                viewModel.anchorAlarm,
                                viewModel.gpsLocation.location,
                                snackBar
                            )
                        }

                        ScreenMode.Weather -> {
                            WeatherCommands(
                                viewModel.weather,
                                viewModel.gpsLocation.location,
                                snackBar
                            )
                        }

                        ScreenMode.Grib -> {
                                GribCommands(
                                    viewModel.gribFile,
                                    viewModel.sailDocs,
                                    mapView,
                                    viewModel.gpsLocation.location,
                                    snackBar
                                )
                        }

                        ScreenMode.AstroNavigation -> {
                            AstroNavigationCommands(
                                viewModel.astroNav,
                                viewModel.gpsLocation.location,
                                snackBar
                            )
                        }
                    }
                },
                rightCommands = {
                    if (mode == ScreenMode.Navigation) {
                        RouteCommands(viewModel.route, mapView, snackBar)
                    }

                    if (mode in listOf(
                            ScreenMode.Navigation,
                            ScreenMode.Grib
                        ) && viewModel.gribFile.gribFileLoaded
                    ) {
                        NkFloatingActionButton(
                            onClick = {
                                viewModel.gribFile.showGrib = !viewModel.gribFile.showGrib
                            },
                            icon = if (viewModel.gribFile.showGrib) Icons.Filled.GridOn else Icons.Filled.GridOff,
                            contentDescription = "Show Grib Info"
                        )
                    }
                },
                snackBar
            )
        },

        bottomSheeetContent = { snackBar ->
            when (mode) {
                ScreenMode.Navigation -> {
                    val navigationTabIcons = listOf(
                        Icons.Outlined.Directions, Icons.Outlined.CompassCalibration,
                        Icons.AutoMirrored.Outlined.MultilineChart, Icons.Outlined.Timeline
                    )

                    var state by remember { mutableIntStateOf(0) }

                    Column {
                        NkTabRowIcon(
                            tabIndex = state,
                            icons = navigationTabIcons,
                            set = { s -> state = s }
                        )

                        when (state) {
                            0 -> {
                                if (viewModel.route.active) WaypointDashboard(
                                    viewModel.gpsLocation.location,
                                    viewModel.route
                                )
                                ExtendedLocationDashboard(viewModel.gpsLocation.location)
                            }

                            1 -> {
                                CompassDashboard(
                                    viewModel.gpsLocation.location,
                                    viewModel.route
                                )
                            }

                            2 -> {
                                TrackDashboard(track = viewModel.track)
                            }

                            3 -> {
                                RouteDashboard(
                                    route = viewModel.route,
                                    location = viewModel.gpsLocation.location
                                )
                            }
                        }
                    }
                }

                ScreenMode.Anchor -> {}

                ScreenMode.Weather -> {
                    WeatherDashboard(viewModel.weather, viewModel.gpsLocation.location)
                }

                ScreenMode.Grib -> {
                    GribDashboard(viewModel.gribFile, snackBar)
                }

                ScreenMode.AstroNavigation -> {
                    AstronavigationDashboard(
                        viewModel.astroNav, viewModel.sun,
                        viewModel.moon, viewModel.gpsLocation.location, snackBar
                    )
                }
            }
        },

        finish = finish
    )
}
