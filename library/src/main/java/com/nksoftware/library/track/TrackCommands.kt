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
 * File: TrackCommands.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.track

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkFloatingActionButton
import com.nksoftware.library.composables.NkSingleSelectMenu
import com.nksoftware.library.gpx.Gpx


@Composable
fun TrackCommands(
   track: Track,
   snackBar: (str: String) -> Unit
) {

   val gpx = Gpx(LocalContext.current)

   val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
         uri: Uri? ->
      if (uri != null)
         gpx.exportTrack(uri, track.track, name = track.name)
      else
         snackBar(R.string.error_cannot_export_gpx_file.toString())
   }

   NkFloatingActionButton(
      onClick = { track.toggleTracking() },
      icon = if (track.saveTrack) Outlined.Route else Outlined.LinkOff,
      contentDescription = "De-/Activate tracking"
   )
   if (track.size > 0) {
      NkFloatingActionButton(
         onClick = { track.clearTrack() },
         icon = Outlined.Delete,
         contentDescription = "Clear list of track points"
      )
      NkFloatingActionButton(
         onClick = { exportLauncher.launch(track.name) },
         icon = Outlined.Upload,
         contentDescription = "Export GPX"
      )
      NkSingleSelectMenu(
         icon = Icons.Outlined.Summarize,
         item = track.granularity,
         action = { track.updateTrack() }
      )
   }
}