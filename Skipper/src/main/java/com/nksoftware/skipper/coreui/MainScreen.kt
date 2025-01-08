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
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.skipper.coreui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.anchor.AnchorDashboard
import com.nksoftware.library.astronavigation.AstroNavigationOptions
import com.nksoftware.library.astronavigation.AstronavigationDashboard
import com.nksoftware.library.composables.NkAppBarButton
import com.nksoftware.library.composables.NkIconButton
import com.nksoftware.library.composables.NkRowNValues
import com.nksoftware.library.composables.NkTabRowText
import com.nksoftware.library.composables.NkText
import com.nksoftware.library.grib.GribDashboard
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.location.ExtendedLocationOptions
import com.nksoftware.library.map.OsmMap
import com.nksoftware.library.map.OsmMapOptions
import com.nksoftware.library.route.RouteOptions
import com.nksoftware.library.saildocs.SaildocsOptions
import com.nksoftware.library.theme.SkipperTheme
import com.nksoftware.library.weather.WeatherDashboard
import com.nksoftware.library.weather.WeatherOption
import com.nksoftware.skipper.R
import com.nksoftware.skipper.core.SkipperViewModel
import com.nksoftware.skipper.core.SkipperViewModelOptions
import com.nksoftware.skipper.map.OsmMapScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class ScreenMode { Navigation, Anchor, Weather, Grib, AstroNavigation }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
   ctx: Context,
   sharedPreferences: SharedPreferences,
   vm: SkipperViewModel,
   dir: String?,
   exit: () -> Unit
) {
   val scaffoldState = rememberBottomSheetScaffoldState()
   val snackbarHostState = remember { SnackbarHostState() }

   val drawerState = rememberDrawerState(DrawerValue.Closed)
   var drawerMode by rememberSaveable { mutableIntStateOf(0) }

   var mode by rememberSaveable { mutableStateOf(ScreenMode.Navigation) }
   val coroutineScope: CoroutineScope = rememberCoroutineScope()

   val mapView by remember { mutableStateOf(OsmMap(ctx, sharedPreferences, dir,
      { lat, lon -> vm.setLocation(loc = ExtendedLocation(lat, lon), trackUpdate = false) })) }


   fun mySnackBar(message: String) {
      coroutineScope.launch {
         snackbarHostState.showSnackbar(
            message, duration = SnackbarDuration.Short
         )
      }
   }


   SkipperTheme(dynamicColor = false) {
      Surface(modifier = Modifier.fillMaxSize()) {

         ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            scrimColor = Color.White,
            drawerContent = {
               ModalDrawerSheet {
                  Column(modifier = Modifier.padding(horizontal = 0.dp)) {

                     NkRowNValues(
                        modifier = Modifier
                           .padding(5.dp)
                           .fillMaxWidth(),
                        arrangement = Arrangement.SpaceBetween,
                     ) {
                        NkIconButton(
                           onClick = {
                              coroutineScope.launch {
                                 vm.saveData()
                                 drawerState.close()
                              }
                           },
                           icon = Icons.AutoMirrored.Outlined.ArrowBack,
                           contentDescription = "Exit Menu"
                        )

                        NkText(text = stringResource(R.string.options), size = 16)

                        NkIconButton(
                           onClick = { coroutineScope.launch { exit() } },
                           icon = Icons.AutoMirrored.Outlined.ExitToApp,
                           contentDescription = "Close App"
                        )
                     }

                     HorizontalDivider()

                     NkTabRowText(
                        tabIndex = drawerMode,
                        titles = listOf(
                           stringResource(R.string.option_general),
                           stringResource(R.string.option_saildocs),
                           stringResource(R.string.option_sextant)
                        ),
                        set = { s -> drawerMode = s }
                     )

                     when (drawerMode) {
                        0 -> {
                           Column(
                              modifier = Modifier
                                 .fillMaxSize()
                                 .verticalScroll(rememberScrollState())
                           ) {
                              ExtendedLocationOptions()
                              SkipperViewModelOptions(vm)
                              RouteOptions(vm.route)
                              OsmMapOptions(mapView, ::mySnackBar)
                              WeatherOption()
                           }
                        }

                        1 -> {
                           SaildocsOptions(vm.sailDocs)
                        }

                        2 -> {
                           AstroNavigationOptions(vm.astroNav)
                        }
                     }
                  }
               }
            }) {

            BottomSheetScaffold(
               scaffoldState = scaffoldState,

               topBar = {
                  TopAppBar(
                     title = { Text(text = stringResource(R.string.app_name)) },

                     navigationIcon = {
                        NkAppBarButton(
                           onClick = { coroutineScope.launch { drawerState.open() } },
                           icon = Icons.Filled.Menu,
                           contentDescription = "Main Menu"
                        )
                     },

                     actions = {
                        PrimaryTabRow(
                           modifier = Modifier
                              .width(250.dp)
                              .padding(0.dp),
                           selectedTabIndex = mode.ordinal,
                           divider = {  }
                        ) {
                           NkAppBarButton(
                              onClick = { mode = ScreenMode.Navigation },
                              icon = Outlined.Directions,
                              contentDescription = "Navigation Mode"
                           )
                           NkAppBarButton(
                              onClick = { mode = ScreenMode.Anchor },
                              icon = Outlined.Anchor,
                              contentDescription = "Anchor Mode"
                           )
                           NkAppBarButton(
                              onClick = { mode = ScreenMode.Weather },
                              icon = Outlined.Cloud,
                              contentDescription = "Weather Mode"
                           )
                           NkAppBarButton(
                              onClick = { mode = ScreenMode.Grib },
                              icon = Outlined.GridOn,
                              contentDescription = "Weather on Sea Mode"
                           )
                           NkAppBarButton(
                              onClick = { mode = ScreenMode.AstroNavigation },
                              icon = Outlined.Architecture,
                              contentDescription = "Astro Navigation"
                           )
                        }
                     })
               },

               snackbarHost = { SnackbarHost(snackbarHostState) },

               sheetContent = {

                  vm.message = ::mySnackBar

                  Column(
                     modifier = Modifier.fillMaxWidth()
                  ) {
                     when (mode) {
                        ScreenMode.Navigation      -> NavigationDashboard(vm, ::mySnackBar)
                        ScreenMode.Anchor          -> AnchorDashboard(vm.anchorAlarm, ::mySnackBar)
                        ScreenMode.Weather         -> WeatherDashboard(vm.weather, vm.gpsLocation.location)
                        ScreenMode.Grib            -> GribDashboard(vm.gribFile, ::mySnackBar)
                        ScreenMode.AstroNavigation -> AstronavigationDashboard(vm.astroNav, vm.sun, vm.moon,
                           vm.gpsLocation.location, ::mySnackBar)
                     }
                  }
               },

               content = { padding ->
                  Column(
                     modifier = Modifier.padding(padding)
                  ) {
                     OsmMapScreen(
                        vm = vm,
                        mode = mode,
                        mapView = mapView,
                        ::mySnackBar
                     )
                  }
               }
            )
         }
      }
   }
}