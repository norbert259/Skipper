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
 * File: Card.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun NkCardWithHeadline(
   modifier: Modifier = Modifier,
   icon: ImageVector? = null,
   headline: String? = null,
   headline2: String? = null,
   content: @Composable () -> Unit
) {
   OutlinedCard(
      modifier = modifier
         .fillMaxWidth()
         .padding(start = 8.dp, end = 8.dp, top = 8.dp),
      border = BorderStroke(width = 1.dp, color = Color.LightGray)
   ) {
      Column(
         Modifier.padding(8.dp)
      ) {
         NkRowNValues {
            if (icon != null)
               NkIcon(icon = icon)

            Row(
               modifier = Modifier.fillMaxWidth(),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
               if (headline != null)
                  NkText(text = headline, weight = FontWeight.SemiBold)

               if (headline2 != null)
                  NkText(text = headline2, weight = FontWeight.SemiBold)
            }
         }
         content()
      }
   }
}
