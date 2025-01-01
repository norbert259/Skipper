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
 * File: Menu.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


// Menu composable

@Composable
fun NkPopupMenu(
   modifier: Modifier = Modifier,
   text: String? = null,
   icon: ImageVector,
   enabled: Boolean = true,
   list: List<Any>,
   selected: Int? = null,
   set: (Int) -> Unit
) {
   var expanded by rememberSaveable { mutableStateOf(false) }

   Column(
      modifier = modifier
   ) {
      if (text == null) {
         FilledIconButton(
            onClick = { expanded = true },
            enabled = enabled
         ) {
            Icon(icon, contentDescription = null)
         }

      } else {
         NkIconTextButton(
            icon = icon,
            text = text,
            enabled = enabled,
            onClick = { expanded = true }
         )
      }

      DropdownMenu(
         expanded = expanded,
         onDismissRequest = { expanded = false },
         modifier = Modifier.padding(5.dp)
      ) {
         for (i in list.indices) {
            DropdownMenuItem(
               modifier = if (i == selected) Modifier.background(Color.LightGray)
               else Modifier.background(Color.Transparent),
               text = { Text(list[i].toString()) },
               onClick = {
                  set(i)
                  expanded = false
               }
            )
         }
      }
   }
}

