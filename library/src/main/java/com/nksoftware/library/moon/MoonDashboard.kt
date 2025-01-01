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
 * File: MoonDashboard.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.moon

import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.composables.NkCardWithHeadline
import com.nksoftware.library.composables.NkText
import com.nksoftware.library.composables.NkTextWithIcon
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.R
import org.shredzone.commons.suncalc.MoonPhase


@Composable
fun MoonDashBoard(moon: Moon, loc: Location) {
   NkCardWithHeadline(
      headline = stringResource(R.string.moon_positions),
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
            text = moon.getMoonRiseStr(loc)
         )

         Image(
            modifier = Modifier
               .align(Alignment.Center)
               .size(50.dp)
               .offset(y = 15.dp),
            imageVector = when (moon.getMoonPhase(loc)) {
               MoonPhase.Phase.NEW_MOON        -> ImageVector.vectorResource(
                  R.drawable.new_moon
               )
               MoonPhase.Phase.WAXING_CRESCENT -> ImageVector.vectorResource(
                  R.drawable.waxing_crescent
               )
               MoonPhase.Phase.FIRST_QUARTER   -> ImageVector.vectorResource(
                  R.drawable.first_quarter
               )
               MoonPhase.Phase.WAXING_GIBBOUS  -> ImageVector.vectorResource(
                  R.drawable.waxing_gibbeous
               )
               MoonPhase.Phase.FULL_MOON       -> ImageVector.vectorResource(
                  R.drawable.full_moon
               )
               MoonPhase.Phase.WANING_GIBBOUS  -> ImageVector.vectorResource(
                  R.drawable.waning_gibbeous
               )
               MoonPhase.Phase.LAST_QUARTER    -> ImageVector.vectorResource(
                  R.drawable.last_quarter
               )
               MoonPhase.Phase.WANING_CRESCENT -> ImageVector.vectorResource(
                  R.drawable.waning_crescent
               )
            },
            contentDescription = null
         )

         NkTextWithIcon(
            modifier = Modifier
               .align(Alignment.CenterEnd),
            icon = Icons.Outlined.ArrowDownward,
            text = moon.getMoonSetStr(loc)
         )

         NkTextWithIcon(
            modifier = Modifier.align(Alignment.TopCenter),
            icon = Icons.Outlined.DarkMode,
            text = moon.getMoonPhaseStr(loc)
         )

         NkText(
            modifier = Modifier
               .align(Alignment.BottomStart),
            text = stringResource(
               R.string.altitude_s,
               moon.getMoonAltitudeStr(loc)
            ),
         )

         NkText(
            modifier = Modifier
               .align(Alignment.BottomEnd),
            text = stringResource(
               R.string.azimuth,
               moon.getMoonAzimuthStr(loc, eyeLevel = 0.0)
            )
         )

      }
   }
}

