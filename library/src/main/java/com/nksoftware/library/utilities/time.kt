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
 * File: time.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.utilities


import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone


fun getTimeStr(time: Long?, pattern: String = "HH:mm:ss z", utc: Boolean = false): String {
   val dateFormatter = SimpleDateFormat(pattern, Locale.getDefault())

   dateFormatter.timeZone = if (utc) TimeZone.getTimeZone("UTC") else TimeZone.getDefault()
   return if (time != null) dateFormatter.format(time) else ""
}


fun getTimeStr(d: ZonedDateTime?, pattern: String = "HH:mm:ss z", utc: Boolean = false): String {
   val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

   return if (utc) d?.withZoneSameInstant(ZoneId.of("UTC"))?.format(formatter) ?: ""
      else d?.withZoneSameInstant(ZoneId.systemDefault())?.format(formatter) ?: ""
}


fun getIsoTimeStr(time: Long): String {
   val formatter = DateTimeFormatter.ISO_INSTANT
   return formatter.format(Instant.ofEpochMilli(time))
}
