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
 * File: Selector.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Selector

class SingleSelectList<E>(
   val list: List<E> = listOf<E>(),
   selected: Int = 0
) {
   var index: Int by mutableIntStateOf(selected)

   val value: E
      get() = list[index]
}


class MultipleSelectList<E>(
   val list: List<E> = listOf(),
   selected: List<Int> = listOf()
) {
   var indices: List<Int> by mutableStateOf(selected)

   val values: List<E>
      get() = list.filterIndexed { index, _ -> index in indices }

   fun toggle(i: Int) {
      val tmp: MutableList<Int> = indices.toMutableList()

      if (i in tmp)
         tmp.remove(i)
      else
         tmp.add(i)

      indices = tmp.toList()
   }
}


@Composable
fun NkCheckBoxItems(
   modifier: Modifier = Modifier,
   item: Any,
   index: Int,
   selectedItem: List<Int>,
   set: (Int) -> Unit
) {
   FilterChip(
      modifier = modifier,
      selected = index in selectedItem,
      onClick = { set(index) },
      label = { Text(if (item is String) item else item.toString()) },
//      leadingIcon = {
//         Icon(
//            imageVector = if (index in selectedItem) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
//            contentDescription = "Localized Description",
//            modifier = Modifier.size(FilterChipDefaults.IconSize)
//         )
//      }
   )
}

@Composable
fun NkCheckBoxItem(
   modifier: Modifier = Modifier,
   item: Any,
   selected: Boolean,
   set: () -> Unit
) {
   FilterChip(
      modifier = modifier,
      selected = selected,
      onClick = { set() },
      label = { Text(if (item is String) item else item.toString()) },
      leadingIcon = {
         Icon(
            imageVector = if (selected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = "Localized Description",
            modifier = Modifier.size(
               FilterChipDefaults.IconSize
            )
         )
      }
   )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> NkSingleSelect(
   modifier: Modifier = Modifier,
   itemList: List<T>,
   selectedItem: Int,
   set: (Int) -> Unit
) {
   FlowRow(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 5
   ) {
      for (i in itemList.indices) {
         NkCheckBoxItems(
            item = itemList[i] as Any,
            index = i,
            selectedItem = listOf(selectedItem),
            set = set
         )
      }
   }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> NkSingleSelect(
   modifier: Modifier = Modifier,
   item: SingleSelectList<T>
) {
   FlowRow(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 5
   ) {
      for (i in item.list.indices) {
         NkCheckBoxItems(
            item = item.list[i] as Any,
            index = i,
            selectedItem = listOf(item.index),
            set = { item.index = i }
         )
      }
   }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> NkMultipleSelect(
   modifier: Modifier = Modifier,
   itemList: MultipleSelectList<T>,
) {
   FlowRow(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 5
   ) {
      for (i in itemList.list.indices) {
         NkCheckBoxItems(
            item = itemList.list[i] as Any,
            index = i,
            selectedItem = itemList.indices,
            set = { itemList.toggle(i) }
         )
      }
   }
}

@Composable
fun <T> NkSingleSelectMenu(
   modifier: Modifier = Modifier,
   item: SingleSelectList<T>,
   icon: ImageVector,
   action: () -> Unit = {}
) {
   var expanded by rememberSaveable { mutableStateOf(false) }

   Column(
      modifier = modifier
   ) {
      NkFloatingActionButton(
         icon = icon,
         onClick = { expanded = true }
      )

      DropdownMenu(
         expanded = expanded,
         onDismissRequest = { expanded = false },
         modifier = Modifier.padding(5.dp)
      ) {
         item.list.forEachIndexed { i, entry ->
            DropdownMenuItem(
               modifier = if (i == item.index) Modifier.background(Color.LightGray)
               else Modifier.background(Color.Transparent),
               text = { Text(text = entry.toString()) },
               onClick = {
                  item.index = i
                  expanded = false
                  action()
               }
            )
         }
      }
   }
}
