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
 * File: CompassDashboard.kt
 * Last modified: 01/01/2025, 14:03
 *
 */

package com.nksoftware.library.location

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkText
import com.nksoftware.library.composables.NkValueField
import com.nksoftware.library.route.Route
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CompassDashboard(location: ExtendedLocation, route: Route) {

   NkCardWithHeadline(
      modifier = Modifier
         .fillMaxWidth()
         .padding(bottom = 5.dp)
   ) {
      Box {
         val image: Painter = painterResource(id = R.drawable.kompass_360_conv)

         val localDensity = LocalDensity.current
         var columnHeightPx by remember { mutableFloatStateOf(0f) }
         var columnHeightDp by remember { mutableStateOf(0.dp) }

         val wp = route.selectedPoint

         Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
               .align(Alignment.CenterStart)
               .fillMaxWidth()
               .height(250.dp)
               .onGloballyPositioned { coordinates ->
                  columnHeightPx = coordinates.size.height.toFloat()
                  columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
               }
               .graphicsLayer { rotationZ = -location.getHeading() }
         )

         Column(
            modifier = Modifier.align(Alignment.TopStart)
         ) {
            NkValueField(
               modifier = Modifier.width(80.dp),
               label = stringResource(R.string.heading),
               dimension = "°",
               value = location.getHeading(),
            )
         }
         NkValueField(
            modifier = Modifier
               .align(Alignment.TopEnd)
               .width(80.dp),
            label = stringResource(R.string.speed),
            dimension = ExtendedLocation.speedDimension,
            value = location.appliedSpeed
         )
         NkValueField(
            modifier = Modifier
               .align(Alignment.TopCenter)
               .offset(y = 65.dp)
               .width(60.dp),
            label = stringResource(R.string.altitude),
            dimension = "m",
            value = location.altitude,
         )
         Column(
            modifier = Modifier
               .align(Alignment.Center)
               .offset(y = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
         ) {
            NkText(
               modifier = Modifier.height(18.dp),
               text = stringResource(R.string.lat, location.latStr),
               size = 12
            )
            NkText(
               modifier = Modifier.height(18.dp),
               text = stringResource(R.string.lon, location.lonStr),
               size = 12
            )
         }

         if (route.active && wp != null) {
            NkValueField(
               modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .width(80.dp),
               label = stringResource(R.string.vmg),
               dimension = ExtendedLocation.speedDimension,
               value = location.getAppliedVelocityMadeGood(wp)
            )
            NkValueField(
               modifier = Modifier
                  .align(Alignment.BottomStart)
                  .width(80.dp),
               label = stringResource(R.string.bearing),
               dimension = "°",
               value = location.getAppliedBearing(wp),
            )
         }

         Canvas(
            modifier = Modifier
               .align(Alignment.CenterStart)
               .fillMaxWidth()
               .height(columnHeightDp)
         ) {
            val canvasWidth = size.width / 2f
            val canvasHeight = size.height / 2f

            val trianglePath = Path().apply {
               moveTo(canvasWidth + 30f, 100f)
               lineTo(canvasWidth, 50f)
               lineTo(canvasWidth - 30f, 100f)
            }

            drawPath(
               color = Color.DarkGray,
               path = trianglePath
            )

            if (route.active && wp != null) {
               val angle = location.getHeadingDeviation(wp)
               val radius = canvasWidth * 0.5f

               if (abs(angle) < 90f) {
                  drawCircle(
                     center = Offset(canvasWidth, canvasHeight),
                     color = Color.LightGray,
                     radius = canvasWidth / 20f
                  )

                  val x = radius * sin(toRadians(angle.toDouble()))
                  val y = radius * cos(toRadians(angle.toDouble()))

                  drawLine(
                     start = Offset(x = canvasWidth, y = canvasHeight),
                     end = Offset(x = canvasWidth + x.toFloat(), y = canvasHeight - y.toFloat()),
                     strokeWidth = 8.0f,
                     cap = StrokeCap.Round,
                     color = Color.LightGray
                  )
               }
            }
         }
      }
   }

}