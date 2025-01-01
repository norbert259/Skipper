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
 * File: Chart.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.composables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nksoftware.library.utilities.getTimeStr
import java.time.ZonedDateTime


// charts

class NkDateFormatterZd (
   private val timeSlots: List<ZonedDateTime>,
   val format: String = "HH:mm",
   val utc: Boolean = false
) : ValueFormatter() {

   override fun getFormattedValue(value: Float): String {
      return try {
         getTimeStr(timeSlots[value.toInt()], format, utc)
      }
      catch (e: Exception) {
         "???"
      }
   }
}


class NkDateFormatterEpoch (
   private val timeSlots: List<Long>,
   val format: String = "HH:mm",
   val utc: Boolean = false
) : ValueFormatter() {

   override fun getFormattedValue(value: Float): String {
      return try {
         getTimeStr(timeSlots[value.toInt()], format, utc)
      }
      catch (e: Exception) {
         "???"
      }
   }
}


fun rotateDrawable(drawable: Drawable, angle: Float): Drawable {
   val arD = arrayOf(drawable)

   return object : LayerDrawable(arD) {

      override fun draw(canvas: Canvas) {
         canvas.save()

         canvas.rotate(
            angle,
            arD.first().bounds.width() / 2f,
            arD.first().bounds.height() / 2f
         )

         super.draw(canvas)
         canvas.restore()
      }
   }
}


@Composable
fun NkLineChartComponent(
   xData: List<Float>,
   xFormatter: ValueFormatter? = null,
   yData1: List<Float>?,
   yData2: List<Float>? = null,
   dataLabel1: String = "",
   dataLabel2: String = "",
   secondAxis: Boolean = true,
   modifier: Modifier = Modifier
) {
   AndroidView(
      modifier = modifier.fillMaxWidth(),

      factory = { context ->
         LineChart(context).apply {
            description.text = ""

            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            xAxis.valueFormatter = xFormatter

            setTouchEnabled(true)
         }
      },

      update = { chart ->
         try {
            val lineData = LineData()

            if (yData1 != null) {
               val entries1: List<Entry> = xData.zip(yData1) { x, y -> Entry(x, y) }
               val lineDataSet1 = LineDataSet(entries1, dataLabel1)

               lineDataSet1.color = Color.BLUE
               lineDataSet1.setCircleColor(Color.BLUE)
               lineDataSet1.fillColor = Color.BLUE

               lineDataSet1.setDrawFilled(true)
               lineData.addDataSet(lineDataSet1)
            }

            if (yData2 != null) {
               val entries2: List<Entry> = xData.zip(yData2) { x, y -> Entry(x, y) }
               val lineDataSet2 = LineDataSet(entries2, dataLabel2)

               lineDataSet2.color = Color.RED
               lineDataSet2.setCircleColor(Color.RED)
               lineDataSet2.fillColor = Color.RED

               if (secondAxis)
                  lineDataSet2.axisDependency = YAxis.AxisDependency.RIGHT

               lineDataSet2.setDrawFilled(true)
               lineData.addDataSet(lineDataSet2)
            }

            chart.data = lineData
            chart.invalidate()
         }

         catch (e: Exception) {
            Log.e("NkLineChartComponent", "Error in NkLineChartComponent")
         }
      }
   )
}


@Composable
fun NkCombinedChartComponent(
   xData: List<Float>,
   xFormatter: ValueFormatter? = null,
   yLineData: List<Float>?,
   yBarData: List<Float>? = null,
   lineLabel: String = "",
   barLabel: String = "",
   secondAxis: Boolean = true,
   iconResource: Int? = null,
   rotationData: List<Float>? = null,
   modifier: Modifier = Modifier
) {
   var icon: Drawable? = null

   if (iconResource != null)
      icon = ContextCompat.getDrawable(LocalContext.current, iconResource)

   AndroidView(
      modifier = modifier.fillMaxWidth(),

      factory = { context ->
         CombinedChart(context).apply {
            description.text = ""

            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            xAxis.valueFormatter = xFormatter

            setTouchEnabled(true)
         }
      },

      update = { chart ->
         val data = CombinedData()
         val lineData = LineData()
         val barData = BarData()

         if (yLineData != null) {
            val lineEntries: List<Entry> = xData.mapIndexed { i, x ->
               if (iconResource != null && rotationData != null)
                  Entry(x, yLineData[i], rotateDrawable(icon!!, rotationData[i]))
               else
                  Entry(x, yLineData[i])
            }

            val lineDataSet = LineDataSet(lineEntries, lineLabel)

            lineDataSet.color = Color.BLUE
            lineDataSet.setCircleColor(Color.BLUE)
            lineDataSet.fillColor = Color.BLUE

            lineDataSet.setDrawFilled(true)
            lineData.addDataSet(lineDataSet)
         }

         if (yBarData != null) {
            val barEntries: List<BarEntry> = xData.zip(yBarData) { x, y -> BarEntry(x, y) }
            val barDataSet = BarDataSet(barEntries, barLabel)

            barDataSet.color = Color.RED

            if (secondAxis)
               barDataSet.axisDependency = YAxis.AxisDependency.RIGHT

            barData.addDataSet(barDataSet)
         }

         data.setData(lineData)
         data.setData(barData)

         chart.data = data
         chart.invalidate()
      }
   )
}
