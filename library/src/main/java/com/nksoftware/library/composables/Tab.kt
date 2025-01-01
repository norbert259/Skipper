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
 * File: Tab.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NkTabRowText(
   tabIndex: Int,
   titles: List<String>,
   set: (Int) -> Unit
) {
   PrimaryTabRow(
      selectedTabIndex = tabIndex,
      divider = {}
   ) {
      titles.forEachIndexed { index, s ->
         Tab(
            text = { Text(text = s) },
            selected = tabIndex == index,
            onClick = { set(index) }
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NkTabRowIcon(
   tabIndex: Int,
   icons: List<ImageVector>,
   set: (Int) -> Unit
) {
   PrimaryTabRow(
      selectedTabIndex = tabIndex,
      divider = {}
   ) {
      icons.forEachIndexed { index, s ->
         Tab(
            icon = { NkIcon(icon = s) },
            selected = tabIndex == index,
            onClick = { set(index) }
         )
      }
   }
}
