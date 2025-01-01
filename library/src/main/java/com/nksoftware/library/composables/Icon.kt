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
 * File: Icon.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun NkIcon(
   modifier: Modifier = Modifier,
   icon: ImageVector,
   contentDescription: String? = null,
   tint: Color = LocalContentColor.current
) {
   Icon(
      modifier = modifier,
      imageVector = icon,
      contentDescription = contentDescription,
      tint = tint
   )
}

@Composable
fun NkTextWithIcon(
   modifier: Modifier = Modifier,
   icon: ImageVector,
   text: String
) {
   Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically
   ) {
      NkIcon(icon = icon, modifier = Modifier.size(18.dp))
      NkText(text = text)
   }
}
