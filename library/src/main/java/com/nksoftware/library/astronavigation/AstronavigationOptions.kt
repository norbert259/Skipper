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
 * File: AstronavigationOptions.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.astronavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkIconButton
import com.nksoftware.library.composables.NkInputField
import com.nksoftware.library.composables.NkSingleSelect


@Composable
fun AstroNavigationOptions(astronav: AstroNavigation) {
   NkCardWithHeadline(headline = stringResource(R.string.fix_type_option), icon = Outlined.Settings) {
      NkSingleSelect<String>(item = astronav.fixType)
   }

   NkCardWithHeadline(headline = stringResource(R.string.sun_edge), icon = Outlined.Settings) {
      NkSingleSelect<String>(item = astronav.sunEdgeIndex)
   }

   NkCardWithHeadline(headline = stringResource(R.string.eye_level_index_correction), icon = Outlined.Settings) {
      Row(
         modifier = Modifier.fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.SpaceBetween
      ) {
         NkInputField(
            modifier = Modifier.width(80.dp),
            value = "%.1f".format(astronav.eyeLevel),
            onValueChange = { i -> astronav.eyeLevel = i.toFloat() },
            regex = "[0-9]+|[0-9]+[.][0-9]*",
            label = stringResource(R.string.eye_level_index_correction),
            dimension = "m"
         )

         Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            NkIconButton(
               onClick = { astronav.indexMinus = !astronav.indexMinus },
               icon = if (astronav.indexMinus) Outlined.Remove else Outlined.Add,
               contentDescription = "Set sign of index correction"
            )

            NkInputField(
               modifier = Modifier.width(50.dp),
               value = astronav.indexErrorDegree.toString(),
               onValueChange = { i -> astronav.indexErrorDegree = i.toInt() },
               regex = "[0-9]|[0-5][0-9]",
               label = "Error",
               dimension = "°"
            )
            NkInputField(
               modifier = Modifier.width(50.dp),
               value = astronav.indexErrorMinutes.toString(),
               onValueChange = { i -> astronav.indexErrorMinutes = i.toInt() },
               regex = "[0-9]|[0-5][0-9]",
               label = "Error",
               dimension = "'"
            )
            NkInputField(
               modifier = Modifier.width(50.dp),
               value = astronav.indexErrorSeconds.toString(),
               onValueChange = { i -> astronav.indexErrorSeconds = i.toInt() },
               regex = "[0-9]|[0-5][0-9]",
               label = "Error",
               dimension = "\""
            )
         }
      }
   }
}
