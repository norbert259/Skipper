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
 * File: XmlGpx.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.gpx

import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter


fun XmlSerializer.document(
   init: XmlSerializer.() -> Unit
): String {
   val xmlStringWriter = StringWriter()
   setOutput(xmlStringWriter)

   startDocument("UTF-8", true)
   init()
   endDocument()

   return xmlStringWriter.toString()
}


fun XmlSerializer.element(name: String, init: XmlSerializer.() -> Unit) {
   startTag("", name)
   init()
   endTag("", name)
}


fun XmlSerializer.element(name: String, content: String, init: XmlSerializer.() -> Unit) {
   startTag("", name)
   init()
   text(content)
   endTag("", name)
}


fun XmlSerializer.element(name: String, content: String) = element(name) { text(content) }

fun XmlSerializer.attribute(name: String, value: String): XmlSerializer = attribute("", name, value)


fun XmlSerializer.gpxdocument(
   creator: String,
   author: String,
   desc: String,
   name: String,
   init: XmlSerializer.() -> Unit
): String {

   return document {
      element("gpx") {
         attribute("xmlns", "http://www.topografix.com/GPX/1/1")
         attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
         attribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd")
         attribute("version", "1.1")
         attribute("creator", creator)

         element("metadata") {
            element("author", author)
            element("desc", desc)
            element("name", name)
         }

         init()
      }
   }
}
