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
 * File: Gpx.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.gpx

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.util.Xml
import com.nksoftware.library.location.ExtendedLocation
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import javax.xml.parsers.SAXParserFactory

val logTag = "GPX"


class Gpx(private val ctx: Context) {

   fun importRoute(uri: Uri, pts: MutableList<ExtendedLocation>) {
      val inp: InputStream? = ctx.contentResolver.openInputStream(uri)

      val parserFactory = SAXParserFactory.newInstance()
      val parser = parserFactory.newSAXParser()

      parser.parse(inp, GpxHandler(pts))
   }


   fun exportRoute(uri: Uri, pts: List<ExtendedLocation>, name: String) {
      val xmlSerializer = Xml.newSerializer()

      val xmlString = xmlSerializer.gpxdocument(
         creator = "NK Software GPX",
         author = "Norbert Kraft",
         desc = "List of route points",
         name = name
      ) {
         for (pt in pts) {
            element("wpt") {
               attribute("lat", "%.4f".format(locale = Locale.US, pt.latitude))
               attribute("lon", "%.4f".format(locale = Locale.US, pt.longitude))
            }
         }
      }

      Log.i(logTag, "exportRoute: $xmlString")

      val out: OutputStream? = ctx.contentResolver.openOutputStream(uri)
      out.use { outputStream -> outputStream?.write(xmlString.encodeToByteArray()) }
   }


   fun exportTrack(uri: Uri, pts: List<Location>, name: String) {
      val xmlSerializer = Xml.newSerializer()

      val xmlString = xmlSerializer.gpxdocument(
         creator = "NK Software GPX",
         author = "Norbert Kraft",
         desc = "List of track points",
         name = name
      ) {
         element("trk") {
            element("trkseg") {
               for (pt in pts) {
                  element("trkpt") {
                     attribute("lat", "%.4f".format(locale = Locale.US, pt.latitude))
                     attribute("lon", "%.4f".format(locale = Locale.US, pt.longitude))

                     element("time", ExtendedLocation.getIsoTimeStr(pt.time))
                     element("ele", "%.1f".format(locale = Locale.US, pt.altitude))
                  }
               }
            }
         }
      }

      Log.i(logTag, "exportTrack: $xmlString")

      val out: OutputStream? = ctx.contentResolver.openOutputStream(uri)
      out.use { outputStream -> outputStream?.write(xmlString.encodeToByteArray()) }
   }
}
