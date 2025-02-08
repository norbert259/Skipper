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
 * File: RouteCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.DirectionsOff
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.gpx.Gpx
import org.osmdroid.views.MapView

@Composable
fun RouteCommands(
   route: Route,
   mapView: MapView,
   snackBar: (str: String) -> Unit
) {

   val ctx = LocalContext.current
   val gpx = Gpx(ctx)

   val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      if (uri != null)
         gpx.importRoute(uri, route.routePoints)
      else
         snackBar(ctx.getString(R.string.error_cannot_import_gpx_file))
   }

   val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
      uri: Uri? ->
         if (uri != null)
            gpx.exportRoute(uri, route.routePoints, route.name)
         else
            snackBar(ctx.getString(R.string.error_cannot_export_gpx_file))
   }


   NkFloatingActionButton(
      onClick = { route.active = !route.active },
      icon = if (route.active) Outlined.Directions else Outlined.DirectionsOff,
      contentDescription = "Routing Mode"
   )

   if (route.active) {
      if (route.size > 0) {
         NkFloatingActionButton(
            onClick = {
               route.deleteAllPoints(snackBar)
            },
            icon = Icons.Filled.Delete,
            contentDescription = "Delete all route points"
         )
         NkFloatingActionButton(
            onClick = {
               route.deletePoint(
                  ctx,
                  mapView.mapCenter.latitude,
                  mapView.mapCenter.longitude,
                  snackBar
               )
            },
            icon = Outlined.Delete,
            contentDescription = "Delete single route point"
         )
      }

      if (route.size > 1) {
         NkFloatingActionButton(
            onClick = {
               route.reverse()
            },
            icon = Outlined.SwapHoriz,
            contentDescription = "reverse route"
         )
      }

      NkFloatingActionButton(
         onClick = {
            route.addPoint(
               ctx,
               mapView.mapCenter.latitude,
               mapView.mapCenter.longitude,
               snackBar
            )
         },
         icon = Icons.Filled.Add,
         contentDescription = "Add routing point"
      )

      if (route.size > 0) {
         NkFloatingActionButton(
            onClick = {
               route.insertPoint(
                  ctx,
                  mapView.mapCenter.latitude,
                  mapView.mapCenter.longitude,
                  snackBar
               )
            },
            icon = Icons.Filled.AddCircle,
            contentDescription = "Insert routing point"
         )
      }

      NkFloatingActionButton(
         onClick = { importLauncher.launch("*/*") },
         icon = Outlined.Download,
         contentDescription = "Import GPX",
      )

      if (route.size > 0) {
         NkFloatingActionButton(
            onClick = { exportLauncher.launch(route.name) },
            icon = Outlined.Upload,
            contentDescription = "Export GPX"
         )
      }
   }
}