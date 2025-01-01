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
 * File: SunDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.sun

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkIcon
import com.nksoftware.library.composables.NkText
import com.nksoftware.library.composables.NkTextWithIcon
import com.nksoftware.library.location.ExtendedLocation
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import com.nksoftware.library.R
import com.nksoftware.library.astronavigation.AstroNavigation
import com.nksoftware.library.astronavigation.SunEdge


@Composable
fun SunDashboard(sun: Sun, loc: Location, astroNavigation: AstroNavigation) {

   val twilightCorrection = mapOf(
      SunEdge.Bottom to SunTimes.Twilight.VISUAL_LOWER,
      SunEdge.Middle to SunTimes.Twilight.HORIZON,
      SunEdge.Top to SunTimes.Twilight.VISUAL
   )

   NkCardWithHeadline(
      headline = stringResource(R.string.sun_positions),
      headline2 = ExtendedLocation.getTimeStr(loc.time),
      icon = Icons.Outlined.Star
   ) {
      Box(
         modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .background(Color.Transparent)
      ) {
         NkTextWithIcon(
            modifier = Modifier
               .align(Alignment.CenterStart),
            icon = Icons.Outlined.ArrowUpward,
            text = sun.getSunRiseStr(loc, astroNavigation.eyeLevel.toDouble(),
               twilightCorrection.getOrDefault(astroNavigation.sunEdge, SunTimes.Twilight.HORIZON)
            )
         )

         NkTextWithIcon(
            modifier = Modifier.align(Alignment.TopCenter),
            icon = Icons.AutoMirrored.Outlined.ArrowForward,
            text = sun.getSunNoonStr(loc, astroNavigation.eyeLevel.toDouble())
         )

         NkTextWithIcon(
            modifier = Modifier
               .align(Alignment.CenterEnd),
            icon = Icons.Outlined.ArrowDownward,
            text = sun.getSunSetStr(loc, astroNavigation.eyeLevel.toDouble(),
               twilightCorrection.getOrDefault(astroNavigation.sunEdge, SunTimes.Twilight.HORIZON))
         )

         NkText(
            modifier = Modifier
               .align(Alignment.BottomStart),
            text = stringResource(
               R.string.altitude_s,
               sun.getSunAltitudeStr(
                  loc,
                  Calendar.getInstance().time,
                  astroNavigation.eyeLevel.toDouble(),
                  astroNavigation.sunEdge
               )
            ),
         )

         NkText(
            modifier = Modifier
               .align(Alignment.BottomEnd),
            text = stringResource(
               R.string.azimuth,
               sun.getSunAzimuth(
                  loc,
                  Calendar.getInstance().time,
                  astroNavigation.eyeLevel.toDouble()
               )
            ),
         )

         NkIcon(
            modifier = Modifier
               .scale(2.8f)
               .align(Alignment.Center)
               .offset(y = 5.dp),
            icon = Icons.Filled.WbSunny,
            tint = Color(255, 179, 0)
         )
      }
   }
}