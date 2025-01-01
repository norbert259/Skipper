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
 * File: Text.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

// Text composable

@Composable
fun NkText(
   modifier: Modifier = Modifier,
   text: String,
   size: Int = 14,
   weight: FontWeight? = null,
   align: TextAlign? = null
) {
   Text(
      modifier = modifier,
      text = text,
      fontSize = size.sp,
      fontWeight = weight,
      lineHeight = size.sp,
      textAlign = align,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
   )
}
