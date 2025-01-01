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
 * File: Button.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


// Button composable

@Composable
fun NkAppBarButton(
   modifier: Modifier = Modifier,
   onClick: () -> Unit,
   icon: ImageVector,
   contentDescription: String? = null
) {
   IconButton(
      modifier = modifier,
      onClick = onClick
   ) {
      Icon(
         imageVector = icon,
         contentDescription = contentDescription,
         tint = MaterialTheme.colorScheme.primary
      )
   }
}

@Composable
fun NkIconTextButton(
   modifier: Modifier = Modifier,
   text: String?,
   icon: ImageVector,
   enabled: Boolean = true,
   onClick: () -> Unit
) {
   Button(
      onClick = { onClick() },
      enabled = enabled,
      contentPadding = PaddingValues(0.dp)
   ) {
      Icon(
         modifier = Modifier.padding(start = 10.dp, end = 5.dp),
         imageVector = icon,
         contentDescription = null
      )

      if (text != null)
         NkText(text = text, modifier = modifier.padding(end = 10.dp))
   }
}

@Composable
fun NkIconButton(
   modifier: Modifier = Modifier,
   icon: ImageVector,
   contentDescription: String? = null,
   color: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
   onClick: () -> Unit,
   enabled: Boolean = true
) {
   FilledIconButton(
      modifier = modifier,
      onClick = { onClick() },
      colors = color,
      enabled = enabled
   ) {
      NkIcon(icon = icon, contentDescription = contentDescription)
   }
}

@Composable
fun NkIconToggleButton(
   modifier: Modifier = Modifier,
   icon: ImageVector,
   contentDescription: String? = null,
   checked: Boolean,
   onCheckedChange: () -> Unit
) {
   FilledIconToggleButton(
      modifier = modifier,
      checked = checked,
      onCheckedChange = { onCheckedChange() }
   ) {
      NkIcon(icon = icon, contentDescription = contentDescription)
   }
}

@Composable
fun NkFloatingActionButton(
   modifier: Modifier = Modifier,
   onClick: () -> Unit,
   icon: ImageVector? = null,
   contentDescription: String? = null,
   containerColor: Color = MaterialTheme.colorScheme.background,
   content: @Composable () -> Unit = {}
) {
   SmallFloatingActionButton(
      modifier = modifier,
      onClick = { onClick() },
      containerColor = containerColor,
      contentColor = MaterialTheme.colorScheme.primary,
      shape = FloatingActionButtonDefaults.largeShape,
      elevation = FloatingActionButtonDefaults.elevation(5.dp)
   ) {
      if (icon == null)
         content()
      else
         Icon(
            imageVector = icon,
            contentDescription = contentDescription
         )
   }
}
