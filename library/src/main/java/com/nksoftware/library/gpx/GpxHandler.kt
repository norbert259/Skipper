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
 * File: GpxHandler.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.gpx

import android.location.Location
import com.nksoftware.library.location.ExtendedLocation
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

class GpxHandler(val pts: MutableList<ExtendedLocation>) : DefaultHandler() {

   private var currentValue = ""
   private var currentElement = false

   private var gpx = false
   private var metadata = false

   private var loc: Location = Location("")

   // Start element function
   @Throws(SAXException::class)
   override fun startElement(
      uri: String,
      localName: String,
      qName: String,
      attributes: Attributes
   ) {
      currentElement = true
      currentValue = ""

      if (localName.equals("gpx", ignoreCase = true)) {
         gpx = true
         pts.clear()
      }

      if (gpx && localName.equals("metadata", ignoreCase = true)) metadata = true

      if (gpx && (localName.equals("wpt", ignoreCase = true) || localName.equals("trkpt", ignoreCase = true))) {
         loc = Location("")

         loc.latitude = attributes.getValue("lat").toDouble()
         loc.longitude = attributes.getValue("lon").toDouble()
      }

   }

   // End element function
   @Throws(SAXException::class)
   override fun endElement(
      uri: String,
      localName: String,
      qName: String
   ) {
      currentElement = false

      if (localName.equals("gpx", ignoreCase = true))
         gpx = false

      if (localName.equals("metadata", ignoreCase = true))
         metadata = false

      if (localName.equals("ele", ignoreCase = true)) {
         loc.altitude = currentValue.toDouble()
      }

      if (gpx && (localName.equals("wpt", ignoreCase = true) || localName.equals("trkpt", ignoreCase = true))) {
         pts.add(ExtendedLocation(loc))
      }
   }

   // characters function
   @Throws(SAXException::class)
   override fun characters(
      ch: CharArray,
      start: Int,
      length: Int
   ) {
      if (currentElement) {
         currentValue += String(ch, start, length)
      }
   }

}