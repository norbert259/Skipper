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
 * File: DataModel.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.core

import android.content.SharedPreferences
import com.nksoftware.library.location.ExtendedLocation
import org.osmdroid.views.MapView


open class DataModel(val mapModeToBeUpdated: Int = 0) {

   companion object Register {
      val dm: MutableList<DataModel> = mutableListOf()

      fun loadPreferences(preferences: SharedPreferences) {
         for (model in dm)
            model.loadPreferences(preferences)
      }

      fun storePreferences(edit: SharedPreferences.Editor) {
         for (model in dm)
            model.storePreferences(edit)
      }

      fun updateMap(mapView: MapView, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {
         for (model in dm)
            model.updateMap(mapView, mapMode, location, snackbar)
      }
   }

   init {
      dm.add(this)
   }

   open fun loadPreferences(preferences: SharedPreferences) {}

   open fun storePreferences(edit: SharedPreferences.Editor) {}

   open fun updateMap(mapView: MapView, mapMode: Int, location: ExtendedLocation, snackbar: (String) -> Unit) {}
}