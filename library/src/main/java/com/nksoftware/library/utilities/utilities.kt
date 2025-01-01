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
 * File: utilities.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.utilities

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat


fun nkHandleException(tag: String, msg: String, e: Exception, uiMsg: ((String) -> Unit)? = null) {
   Log.e(tag, msg, e)
   Log.e(tag, Log.getStackTraceString(e))

   if (uiMsg != null)
      uiMsg(msg)
}


fun nkCheckAndGetPermission(activity: ComponentActivity, permission: String) {
   if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
   }
}


fun convertUnits(parameter: String, value: Float): Float {
   return when (parameter) {
      "pressure"    -> value / 100f
      "temperature" -> value - 273.15f
      else          -> value
   }
}

