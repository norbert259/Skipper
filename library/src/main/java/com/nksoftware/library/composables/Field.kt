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
 * File: Field.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nksoftware.library.theme.secondaryContainerLight


// Field composables

@Composable
fun NkValueField(
   modifier: Modifier = Modifier,
   label: String,
   icon: ImageVector? = null,
   value: Any?,
   dimension: String? = null
) {

   val precision = when (value) {
      is Double -> if (value > 0 && value < 10) 1 else 0
      is Float  -> if (value > 0 && value < 10) 1 else 0
      else      -> 0
   }

   Column(
      modifier = modifier
         .defaultMinSize(minWidth = 60.dp)
         .border(1.dp, SolidColor(Color.Gray), shape = RoundedCornerShape(8.dp))
         .padding(5.dp)
   ) {
      NkText(text = label, size = 12, weight = FontWeight.Light)

      Row(
         modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
         horizontalArrangement = Arrangement.spacedBy(3.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         if (icon != null)
            NkIcon(icon = icon, modifier = Modifier.size(14.dp))

         NkText(
            modifier = Modifier.weight(2f),
            text = when (value) {
               null      -> ""
               is Float  -> "%.${precision}f".format(value)
               is Double -> "%.${precision}f".format(value)
               is Int    -> "%d".format(value)
               else      -> value.toString()
            },
            align = TextAlign.End,
            size = 14
         )

         if (dimension != null)
            NkText(text = dimension, size = 12)
      }
   }
}

@Composable
fun NkInputField(
   modifier: Modifier = Modifier,
   label: String = "",
   value: String,
   regex: String,
   dimension: String? = null,
   onValueChange: (String) -> Unit
) {
   val pattern = Regex(regex)
   var text by remember { mutableStateOf(value) }
   var isError by rememberSaveable { mutableStateOf(false) }

   BasicTextField(
      modifier = modifier,
      value = text,
      textStyle = TextStyle(textAlign = TextAlign.Right, fontSize = 14.sp),
      onValueChange = {
         text = it
         if (pattern.matches(it)) {
            onValueChange(text)
            isError = false
         } else {
            isError = true
         }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
      decorationBox = { innerTextField ->
         Column(
            modifier = modifier
               .defaultMinSize(minWidth = 60.dp)
               .border(
                  width = 2.dp,
                  brush = SolidColor(if (isError) Color.Red else MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(8.dp)
               )
               .padding(5.dp)
         ) {
            NkText(text = label, size = 12, weight = FontWeight.Light)

            Row(
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
               Column(
                  modifier = Modifier.weight(3f)
               ) {
                  innerTextField()
               }

               if (dimension != null)
                  NkText(text = dimension, size = 12)
            }
         }
      }
   )
}

@Composable
fun NkMatrixCell(
   modifier: Modifier = Modifier.width(40.dp),
   value: Any? = "",
   min: Float? = null,
   max: Float? = null,
   precision: Int = 0,
   alignment: Alignment = Alignment.BottomCenter,
   color: Color = secondaryContainerLight,
   header: String = "",
   subHeader: String = ""
) {
   Box(modifier = modifier
      .height(40.dp)
      .padding(bottom = 5.dp)
      .drawWithCache {
         onDrawBehind {
            if ((value is Float) && (min != null) && (max != null)) {
               val diff = max - min
               val offset = (max - value) * size.height / diff
               val height = (value - min) * size.height / diff

               drawRect(
                  color = color,
                  topLeft = Offset(0f, offset),
                  size = Size(size.width, height)
               )
            }
         }
      }) {

      if (header != "")
         NkText(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 3.dp),
            text = header,
            size = 10,
            weight = FontWeight.Light
         )

      if (subHeader != "")
         NkText(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 3.dp),
            text = subHeader,
            size = 10,
            weight = FontWeight.Light
         )

      if (value is ImageVector) {
         NkIcon(
            modifier = Modifier.align(Alignment.Center),
            icon = value,
            tint = color
         )

      } else {

         NkText(
            modifier = Modifier.align(alignment),
            text = when (value) {
               is String -> value
               is Int    -> value.toString()
               is Float  -> { if (!value.isNaN()) "%.${precision}f".format(value) else "" }
               is Double -> { if (!value.isNaN()) "%.${precision}f".format(value) else "" }
               else      -> ""
            },
            size = 12
         )
      }
   }
}

@Preview
@Composable
fun ComposablePreview() {
   Column(modifier = Modifier.width(100.dp)) {
      NkValueField(
         label = "1234",
         value = 1234.66
      )
      NkValueField(
         label = "12345",
         value = 12.66,
         dimension = "km/h",
      )
      NkValueField(
         label = "1234",
         value = 1234.66,
         dimension = "km/h",
      )
      NkValueField(
         label = "12345678901234567890",
         value = 1234567890.66,
         dimension = "123km/h",
      )
      NkValueField(
         label = "1234567890",
         value = 12345678.66, dimension = "km/h",
         icon = Icons.Outlined.Mail
      )
      NkInputField(
         value = "%.1f".format(2.5f),
         onValueChange = { i -> },
         regex = "[0-9]+|[0-9]+[.][0-9]*",
         label = "Eye level"
      )

      NkInputField(
         value = "%.1f".format(2.5f),
         onValueChange = { i -> },
         regex = "[0-9]+|[0-9]+[.][0-9]*",
         label = "Eye level",
         dimension = "m"
      )
   }
}