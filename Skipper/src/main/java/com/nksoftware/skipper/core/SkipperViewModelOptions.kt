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
 * File: SkipperViewModelOptions.kt
 * Last modified: 07/01/2025, 12:14
 *
 */

package com.nksoftware.skipper.core

import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkSingleSelect


@Composable
fun SkipperViewModelOptions(vm: SkipperViewModel) {
   NkCardWithHeadline(
      headline = stringResource(com.nksoftware.skipper.R.string.gpsprovider),
      icon = Outlined.Settings
   ) {
      NkSingleSelect(item = vm.gpsLocation.gpsProvider)
   }
}
