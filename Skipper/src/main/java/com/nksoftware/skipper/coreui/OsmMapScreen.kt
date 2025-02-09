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
 * File: OsmMapScreen.kt
 * Last modified: 06.02.25, 10:39
 *
 */

package com.nksoftware.skipper.coreui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.composables.NkIcon
import com.nksoftware.library.composables.NkText
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.map.OsmMapView
import com.nksoftware.skipper.R
import com.nksoftware.skipper.core.SkipperViewModel


@SuppressLint("MutableCollectionMutableState")
@Composable
fun OsmMapScreen(
    vm: SkipperViewModel,
    mapView: OsmMapView,
    mode: ScreenMode,
    topCommands: @Composable () -> Unit,
    rightCommands: @Composable () -> Unit,
    snackBar: (str: String) -> Unit
) {

    var mapRotation by rememberSaveable { mutableStateOf(false) }
    var centerMap by rememberSaveable { mutableStateOf(true) }



    Column {
        Box(modifier = Modifier.weight(2f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),

                factory = { context -> mapView },

                update = { view: OsmMapView ->
                    try {
                        view.update(
                            vm.gpsLocation.gps,
                            vm.gpsLocation.location,
                            centerMap,
                            snackBar
                        )
                        DataModel.updateMap(view, mode.ordinal, vm.gpsLocation.location, snackBar)
                    } catch (e: Exception) {
                        snackBar("Error updating map $e")
                    }
                }
            )

            if (mode == ScreenMode.Navigation && vm.route.active)
                NkIcon(
                    icon = Outlined.Add,
                    modifier = Modifier.align(Alignment.Center)
                )

            NkText(
                modifier = Modifier
                   .align(Alignment.BottomStart)
                   .padding(start = 10.dp, bottom = 10.dp),
                text = stringResource(R.string.zoom_level, mapView.zoomLevel)
            )

            Column(
                modifier = Modifier
                   .align(Alignment.TopStart)
                   .padding(start = 10.dp)
                   .offset(y = 80.dp)
            ) {
                NkFloatingActionButton(onClick = { vm.gpsLocation.toggleState() }) {
                    Icon(
                        imageVector = if (vm.gpsLocation.gps) Outlined.GpsFixed else Outlined.GpsOff,
                        contentDescription = "De-/Activate GPS"
                    )
                    CircularProgressIndicator(
                        progress = { (vm.gpsLocation.updateCounter % 10) / 10f },
                        gapSize = 5.dp
                    )
                }
                NkFloatingActionButton(
                    onClick = { mapView.controller?.zoomIn() },
                    icon = Outlined.ZoomIn,
                    contentDescription = "Zoomin"
                )
                NkFloatingActionButton(
                    onClick = { mapView.controller?.setCenter(vm.gpsLocation.location.locGp) },
                    icon = Outlined.LocationOn,
                    contentDescription = "Set Map Center"
                )
                NkFloatingActionButton(
                    onClick = { centerMap = !centerMap },
                    icon = if (centerMap) Outlined.CenterFocusStrong else Outlined.Fullscreen,
                    contentDescription = "Set Map Center"
                )
                NkFloatingActionButton(
                    onClick = { mapView.controller?.zoomOut() },
                    icon = Outlined.ZoomOut,
                    contentDescription = "Zoomout"
                )
                NkFloatingActionButton(
                    onClick = {
                        mapView.toggleMapRotation()
                        mapRotation = !mapRotation
                    },
                ) {
                    NkIcon(icon = if (mapRotation) Outlined.Refresh else Outlined.North)
                }
            }

            Row(
                modifier = Modifier
                   .align(Alignment.TopCenter)
                   .padding(top = 10.dp)
            ) {
                topCommands()
            }

            Column(
                modifier = Modifier
                   .align(Alignment.TopEnd)
                   .padding(end = 10.dp)
                   .offset(y = 80.dp)
            ) {
                rightCommands()
            }
        }
    }
}
