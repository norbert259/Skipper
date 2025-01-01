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
 * File: Saildocs.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.saildocs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import com.nksoftware.library.composables.MultipleSelectList
import com.nksoftware.library.composables.SingleSelectList
import com.nksoftware.library.core.DataModel
import com.nksoftware.library.utilities.nkHandleException
import com.nksoftware.library.R
import org.osmdroid.util.BoundingBox
import java.util.Locale


const val weatherModelKey = "WeatherModel"
const val weatherResolutionKey = "WeatherResolution"
const val weatherIncrementKey = "WeatherIncrement"
const val weatherNoDaysKey = "WeatherNoDays"
const val weatherNoParametersKey = "WeatherNoParameters"


data class WeatherModelParameter(
   val resolutions: SingleSelectList<Float>,
   val parameters: MultipleSelectList<String>,
   val timeIncrement: SingleSelectList<Int>,
   val noDays: SingleSelectList<Int>
)


class SailDocs(val ctx: Context): DataModel(0) {

   val sailDocsModels = mapOf(

      "GFS" to WeatherModelParameter(
         SingleSelectList<Float>(listOf(0.25f, 0.5f, 1.0f)),
         MultipleSelectList<String>(
            listOf(
               "PRMSL", "WIND", "GUST", "AIRTMP", "SFCTMP", "RH", "LFTX", "CAPE", "RAIN", "APCP",
               "HGT500", "TMP500", "WIND500", "ABSV", "CLOUDS", "WAVES"
            ),
            listOf(0, 1, 2, 3)
         ),
         SingleSelectList<Int>(listOf(3, 6, 12)),
         SingleSelectList<Int>(listOf(3, 5))
      ),

      "ECMWF" to WeatherModelParameter(
         SingleSelectList<Float>(listOf(0.1f, 0.4f)),
         MultipleSelectList(
            listOf("WIND", "MSLP", "HGT500", "TEMP"),
            listOf(0, 1, 3)
         ),
         SingleSelectList<Int>(listOf(3, 6, 12)),
         SingleSelectList<Int>(listOf(3, 6))
      ),

      "ICON" to WeatherModelParameter(
         SingleSelectList<Float>(listOf(0.125f)),
         MultipleSelectList(
            listOf("PRMSL", "WIND", "GUST", "AIRTMP", "SFCTMP"),
            listOf(0, 1, 2, 3)
         ),
         SingleSelectList<Int>(listOf(3, 6, 12)),
         SingleSelectList<Int>(listOf(3, 6, 9))
      )
   )

   val modelName = SingleSelectList<String>(sailDocsModels.keys.toList(), 0)

   val actualModel: WeatherModelParameter
      get() = sailDocsModels[modelName.value]!!


   override fun loadPreferences(preferences: SharedPreferences) {
      modelName.index = preferences.getInt(weatherModelKey, 0)
      actualModel.resolutions.index = preferences.getInt(weatherResolutionKey, 0)
      actualModel.timeIncrement.index = preferences.getInt(weatherIncrementKey, 0)
      actualModel.noDays.index = preferences.getInt(weatherNoDaysKey, 0)

      val noParameter = preferences.getInt(weatherNoParametersKey, 0)
      if (noParameter > 0) {
         val tmp = mutableListOf<Int>()
         for (i in 0 until noParameter)
            tmp.add(preferences.getInt("WeatherParameter$i", 0))
         actualModel.parameters.indices = tmp
      }
   }


   override fun storePreferences(edit: SharedPreferences.Editor) {
      edit.putInt(weatherModelKey, modelName.index)
      edit.putInt(weatherResolutionKey, actualModel.resolutions.index)
      edit.putInt(weatherIncrementKey, actualModel.timeIncrement.index)
      edit.putInt(weatherNoDaysKey, actualModel.noDays.index)

      edit.putInt(weatherNoParametersKey, actualModel.parameters.indices.size)
      actualModel.parameters.indices.forEachIndexed { i: Int, v: Int -> edit.putInt("WeatherParameter$i", v) }
   }


   fun sendSailDocsRequest(
      boundingBox: BoundingBox,
      msg: ((String) -> Unit)? = null
   ) {
      val minLat = boundingBox.latSouth.toInt()
      val minLon = boundingBox.lonWest.toInt()
      val maxLat = boundingBox.latNorth.toInt() + 1
      val maxLon = boundingBox.lonEast.toInt() + 1

      val sailDocsStr = "send %s:%d%s,%d%s,%d%s,%d%s/%.2f,%.2f/%d,%d..%d/%s".format(Locale.ENGLISH,
         modelName.value,

         minLat, if (minLat >= 0.0) "N" else "S",
         maxLat, if (maxLat >= 0.0) "N" else "S",
         minLon, if (minLon >= 0.0) "E" else "W",
         maxLon, if (maxLon >= 0.0) "E" else "W",

         actualModel.resolutions.value, actualModel.resolutions.value, 0,
         actualModel.timeIncrement.value, 24 * actualModel.noDays.value,
         actualModel.parameters.values.joinToString(separator = ",") { it }
      )

      val intent = Intent(Intent.ACTION_SENDTO).apply {
         data = Uri.parse("mailto:")
         putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf("query@saildocs.com")
         )
         putExtra(
            Intent.EXTRA_SUBJECT,
            "Grib Request"
         )
         putExtra(
            Intent.EXTRA_TEXT,
            sailDocsStr
         )
      }

      try {
         startActivity(ctx, intent, Bundle.EMPTY)
      }
      catch (e: Exception) {
         nkHandleException("SailDocs", ctx.getString(R.string.exception_unable_to_start_mail_app), e, msg)
      }
   }
}

