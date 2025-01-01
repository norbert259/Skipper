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
 * File: Compound.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Compound composable

@Composable
fun NkRowNValues(
   modifier: Modifier = Modifier,
   arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(5.dp),
   content: @Composable () -> Unit
) {
   Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = arrangement,
      verticalAlignment = Alignment.CenterVertically
   ) {
      content()
   }
}
