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
 * File: GribCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.grib

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.saildocs.SailDocs
import org.osmdroid.views.MapView
import com.nksoftware.library.R


@Composable
fun GribCommands(
   gribFile: GribFile,
   saildocs: SailDocs,
   mapView: MapView,
   location: ExtendedLocation,
   snackBar: (str: String) -> Unit
) {

   val errStr = stringResource(R.string.error_cannot_load_grib_file)

   val gribLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      if (uri != null)
         gribFile.scan(uri, location, snackBar)
      else
         snackBar(errStr)
   }

   NkFloatingActionButton(
      onClick = { saildocs.sendSailDocsRequest(mapView.getBoundingBox(), snackBar) },
      icon = Outlined.Mail,
      contentDescription = "Saildocs"
   )

   NkFloatingActionButton(
      onClick = { gribLauncher.launch("*/*") },
      icon = Outlined.FileCopy,
      contentDescription = "Load file"
   )

   if (gribFile.gribFileLoaded) {
      NkFloatingActionButton(
         onClick = { gribFile.delete() },
         icon = Outlined.Delete,
         contentDescription = "Delete loaded GRIB file"
      )
   }
}