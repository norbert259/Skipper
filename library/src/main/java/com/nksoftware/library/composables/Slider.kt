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
 * File: Slider.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun NkSlider(
   modifier: Modifier = Modifier,
   text: String? = null,
   value: Float,
   func: (value: Float) -> Unit,
   min: Float,
   max: Float,
   steps: Int
) {
   Column(modifier = modifier) {
      if (text != null)
         NkText(text = text.format(value), weight = FontWeight.SemiBold)

      Slider(
         modifier = Modifier.height(30.dp),
         value = value,
         onValueChange = { func(it) },
         valueRange = min..max,
         steps = steps
      )
   }
}
