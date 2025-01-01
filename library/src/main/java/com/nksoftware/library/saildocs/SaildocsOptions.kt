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
 * File: SaildocsOptions.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.saildocs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkMultipleSelect
import com.nksoftware.library.composables.NkSingleSelect
import com.nksoftware.library.R

@Composable
fun SaildocsOptions(saildocs: SailDocs) {
   NkCardWithHeadline(
      headline = stringResource(R.string.weather_model, saildocs.modelName.value),
      icon = Icons.Outlined.Settings
   ) {
      NkSingleSelect<String>(item = saildocs.modelName)
   }

   val weatherModel = saildocs.actualModel

   NkCardWithHeadline(
      headline = stringResource(R.string.parameters_for_grid, saildocs.modelName.value),
      icon = Icons.Outlined.Settings
   ) {
      NkMultipleSelect<String>(itemList = weatherModel.parameters)
   }

   NkCardWithHeadline(
      headline = stringResource(R.string.grid_resolution, weatherModel.resolutions.value),
      icon = Icons.Outlined.Settings
   ) {
      NkSingleSelect(item = weatherModel.resolutions)
   }

   NkCardWithHeadline(
      headline = stringResource(R.string.forecast_increment_hours, weatherModel.timeIncrement.value),
      icon = Icons.Outlined.Settings
   ) {
      NkSingleSelect<Int>(item = weatherModel.timeIncrement)
   }

   NkCardWithHeadline(
      headline = stringResource(R.string.forecast_for_days, weatherModel.noDays.value),
      icon = Icons.Outlined.Settings
   ) {
      NkSingleSelect<Int>(item = weatherModel.noDays)
   }
}