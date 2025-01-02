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
 * File: GribParserV1.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.grib

import android.util.Log
import com.nksoftware.library.utilities.nkHandleException
import java.io.InputStream
import java.nio.ByteBuffer
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow


class V1GribFileEntry(inp: InputStream) : GribFileEntry() {

   private var section0: V1Section0
   private var section1: V1Section1
   private var section2: V1Section2? = null
   private var section3: V1Section3? = null
   private var section4: V1Section4

   init {
      val data = ByteBuffer.allocate(8)
      inp.read(data.array())

      section0 = V1Section0(data)
      section1 = V1Section1(getNextSection(inp))

      if (section1.section2)
         section2 = V1Section2(getNextSection(inp))

      if (section1.section3)
         section3 = V1Section3(getNextSection(inp))

      section4 = V1Section4(getNextSection(inp))

      checkPattern(inp, "7777")
   }


   private fun getNextSection(inp: InputStream): ByteBuffer {
      val start = ByteBuffer.allocate(4)
      inp.read(start.array())

      val length = (start.getShort(0).toUShort().toInt() shl 8) + start.get(2).toUByte().toInt()
      val data = ByteBuffer.allocate(length)

      data.put(start.array(), 0, 4)
      inp.read(data.array(), 4, length - 4)

      return data
   }


   private fun checkPattern(input: InputStream, pattern: String) {
      val pbuf = ByteBuffer.allocate(pattern.length)

      if (input.read(pbuf.array()) > 0) {
         when (val str = String(pbuf.array())) {
            "GRIB" -> Log.d(logTag, "Grib entry found")
            "7777" -> Log.d(logTag, "End of GRIB file found")
            else   -> throw Exception("Wrong pattern: $str found - expected: $pattern")
         }
      }
   }


   override fun getParameter(): String {
      return section1.getParameter()
   }


   override fun getReferenceTime(): Calendar {
      return section1.referenceTime
   }


   override fun getGridInfo(type: GribFile.GridInfo): Float {
      return when (type) {
         GribFile.GridInfo.MinLat -> (section2!!.gridDefinition.firstLat) / 1000.0f
         GribFile.GridInfo.MinLon -> (section2!!.gridDefinition.firstLon) / 1000.0f
         GribFile.GridInfo.MaxLat -> (section2!!.gridDefinition.lastLat) / 1000.0f
         GribFile.GridInfo.MaxLon -> (section2!!.gridDefinition.lastLon) / 1000.0f
         GribFile.GridInfo.NoLat  -> (section2!!.gridDefinition.noLat).toFloat()
         GribFile.GridInfo.NoLon  -> (section2!!.gridDefinition.noLon).toFloat()
         GribFile.GridInfo.LatInc -> (section2!!.gridDefinition.latIncrement) / 1000.0f
         GribFile.GridInfo.LonInc -> (section2!!.gridDefinition.lonIncrement) / 1000.0f
      }
   }


   override fun getGridValues(): MutableList<GpsGridPoint> {
      return section2!!.getGridData(section4, section1.decimalScaleFactor)
   }
}


open class V1Section(private val buffer: ByteBuffer) {

   val length = buffer.limit()
   private val mantissaFactor: Double = 1.0 / 16777216.0

   fun getIntFromByte(i: Int): Int {
      return buffer.get(i).toUByte().toInt()
   }

   fun getIntFrom2Bytes(i: Int): Int {
      return buffer.getShort(i).toUShort().toInt()
   }

   fun getIntFrom3Bytes(i: Int): Int {
      return (getIntFrom2Bytes(i) shl 8) + getIntFromByte(i + 2)
   }

   fun getIntFrom4Bytes(i: Int): UInt {
      return buffer.getInt(i).toUInt()
   }

   fun getMaskedIntFromNBytes(i: Int, offset: Int, numberOfBits: Int): UInt {

      if (numberOfBits > 25)
         throw Exception("Cannot decode more than 25 bits")

      val firstShift = 24 + offset
      var rawValue = (buffer.get(i).toUByte().toUInt() shl firstShift) shr firstShift

      var noBytes = 1
      while ((8 * noBytes - offset - numberOfBits) < 0) {
         rawValue = (rawValue shl 8) + buffer.get(i + noBytes).toUByte().toUInt()
         noBytes++
      }

      return rawValue shr (8 * noBytes - numberOfBits - offset)
   }

   fun getSignedIntFrom2Bytes(i: Int): Int {
      val rawValue = buffer.getShort(i).toUShort().toInt()

      val value = rawValue and 0x00007fff
      val signNegative = (rawValue and 0x00008000) > 0

      return if (signNegative) -value else value
   }

   fun getSignedIntFrom3Bytes(i: Int): Int {
      val rawValue = getIntFrom3Bytes(i)

      val value = rawValue and 0x007fffff
      val signNegative = (rawValue and 0x00800000) > 0

      return if (signNegative) -value else value
   }

   fun getFloatFrom4Bytes(i: Int): Float {
      val signAndExponent = getIntFromByte(i)
      val signNegative = (signAndExponent and 0x00000080) > 0

      val exponent = (signAndExponent and 0x0000007F) - 64
      val mantissa = getIntFrom3Bytes(i + 1).toDouble()

      val value = mantissaFactor * mantissa * 16.0.pow(exponent)
      return if (signNegative) -value.toFloat() else value.toFloat()
   }

   fun getFloatFromBits(offset: Int, i: Int, noBits: Int): Float {
      val pos = noBits * i

      val byteBoundary = pos / 8
      val startOffset = pos % 8

      val retValue = getMaskedIntFromNBytes(offset + byteBoundary, startOffset, noBits)
      return retValue.toFloat()
   }
}


class V1Section0(data: ByteBuffer): V1Section(data) {

   private val totalLength = getIntFrom3Bytes(4)
   private val editionNumber = getIntFromByte(7)

   init {
      Log.d(
         logTag,
         "Section 0($length): grib file edition no: $editionNumber total length: $totalLength"
      )

      if (editionNumber != 1)
         throw Exception("Only grib edition 1 supported")
   }
}


class V1Section1(data: ByteBuffer): V1Section(data) {

   private val tableVersion = getIntFromByte(3)
   private val originatingCenter = getIntFromByte(4)
   private val generationProcess = getIntFromByte(5)
   private val gridDefinition = getIntFromByte(6)

   private val flag = getIntFromByte(7)
   val section2 = (flag and 0x00000080) > 0
   val section3 = (flag and 0x00000040) > 0

   val parameter = getIntFromByte(8)
   val levelType = getIntFromByte(9)
   val height = getIntFrom2Bytes(10)

   private val year = getIntFromByte(12)
   private val month = getIntFromByte(13)
   private val day = getIntFromByte(14)
   private val hour = getIntFromByte(15)
   private val minute = getIntFromByte(16)
   private val timeUnit = getIntFromByte(17)

   private val p1 = getIntFromByte(18)
   private val p2 = getIntFromByte(19)
   private val timeRangeIndicator = getIntFromByte(20)

   private val n = getIntFrom2Bytes(21)
   private val numberMissing = getIntFromByte(23)
   private val century = getIntFromByte(24)
   private val subCenter = getIntFromByte(25)
   val decimalScaleFactor = 10f.pow(getSignedIntFrom2Bytes(26))

   private val codeTable2 = mapOf(
      2 to "pressure",
      7 to "geopotential height",
      11 to "temperature",
      33 to "wind (u)",
      34 to "wind (v)",
      41 to "absolute vorticity",
      51 to "humidity",
      52 to "relative humidity",
      59 to "precipitation rate",
      61 to "precipitation",
      71 to "total cloud cover",
      100 to "significant wave height",
      107 to "wave direction",
      108 to "wave period",
      131 to "surface lifted index",
      157 to "potential convective energy",
      180 to "wind gust",
      212 to "long wave"
   )

   private val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
   val referenceTime: Calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.getDefault())

   init {
      referenceTime.set((century - 1) * 100 + year, month -  1, day, hour, minute, 0)
      referenceTime.set(Calendar.MILLISECOND, 0)
    
      val timeOffset = when (timeRangeIndicator) {
         0 -> p1
         1 -> 0
         2 -> p1
         4 -> (p1 + p2) / 2
         10 -> (p1 shl 255) + p2
         else -> throw Exception("Unknown time offset indicator($timeRangeIndicator)")
      }

      val timeIncrement = when (timeUnit) {
         0 -> timeOffset
         1 -> timeOffset * 60
         2 -> timeOffset * 60 * 24
         else -> throw Exception("Only Time unit 1,2,3 supported - found: $timeUnit")
      }

      referenceTime.add(Calendar.MINUTE, timeIncrement)

      Log.d(
         logTag,
         "Section 1($length): parameter: ${getParameter()} reference time: ${dateFormatter.format(referenceTime.time)} time increment: $timeIncrement minutes"
      )
   }

   fun getParameter(): String {
      return codeTable2.getOrDefault(parameter, parameter.toString())
   }

   fun getTime(): Calendar {
      return referenceTime
   }
}


interface V1Section2Interface {
   fun getGridData(s4: V1Section4, scaling: Float): MutableList<GpsGridPoint>
}


class V1Section2Grid(data: ByteBuffer): V1Section(data), V1Section2Interface {

   val noLon = getIntFrom2Bytes(6)
   val noLat = getIntFrom2Bytes(8)
   val firstLat = getSignedIntFrom3Bytes(10)
   val firstLon = getSignedIntFrom3Bytes(13)

   val resolution = getIntFromByte(16)

   val lastLat = getSignedIntFrom3Bytes(17)
   val lastLon = getSignedIntFrom3Bytes(20)
   var lonIncrement = getSignedIntFrom2Bytes(23)
   var latIncrement = getSignedIntFrom2Bytes(25)

   private val scanningMode = getIntFromByte(27)
   private val lonScanNegativ = (scanningMode and 0x00000080) > 0
   private val latScanNegativ = !((scanningMode and 0x00000040) > 0)
   private val southNorthDirectionConsecutive = ((scanningMode and 0x00000020) > 0)

//   val latitudeSouthernPole = get3ByteInt(32)
//   val longitudeSouthernPole = get3ByteInt(35)
//   val rotationAngle = data.getInt(38).toInt()
//   val latitudePoleStretching = get3ByteInt(42)
//   val longitudePoleStretching = get3ByteInt(45)
//   val stretchingFactor = data.getInt(48).toUInt()

   init {
      Log.d(
         logTag,
         """Section 2($length): Lat/Lon Grid: Lon points $noLon no Lat points: $noLat
            #   From Lat($firstLat)/Lon($firstLon) to Lat($lastLat)/Lon($lastLon)
            #   Scanning Lon negative: $lonScanNegativ Lat negative: $latScanNegativ south/north consecutive: $southNorthDirectionConsecutive
         """.trimMargin("#"))
   }


   override fun getGridData(s4: V1Section4, scaling: Float): MutableList<GpsGridPoint> {
      val gridPoints = mutableListOf<GpsGridPoint>()

      try {
         if (southNorthDirectionConsecutive) {
            for (i in 0 until noLon) {
               for (j in 0 until noLat) {
                  gridPoints.add(
                     GpsGridPoint(
                     (firstLat + j * latIncrement) / 1000f,
                     (firstLon + i * lonIncrement) / 1000f,
                     s4.decode(i * noLat + j) / scaling
                  )
                  )
               }
            }
         } else {
            for (i in 0 until noLat) {
               for (j in 0 until noLon) {
                  gridPoints.add(
                     GpsGridPoint(
                     (firstLat + i * latIncrement) / 1000f,
                     (firstLon + j * lonIncrement) / 1000f,
                     s4.decode(i * noLon + j) / scaling
                  )
                  )
               }
            }
         }
      }

      catch(e: Exception) {
         nkHandleException(logTag, "Exception: Cannot decode values in section4", e)
      }

      return gridPoints
   }
}


class V1Section2(data: ByteBuffer): V1Section(data), V1Section2Interface {

   private val numberOfVerticalCoordinates = getIntFromByte(3)
   private val pv = getIntFromByte(4)
   private val dataRepresentationType = getIntFromByte(5)
   var gridDefinition: V1Section2Grid

   init {
      when (dataRepresentationType) {
         0 -> gridDefinition = V1Section2Grid(data)
         else -> throw Exception("Only simple grids supported in section 2")
      }

      Log.d(
         logTag,
         "Section 2($length): no of vertical coord.: $numberOfVerticalCoordinates pv/pl: $pv representation type: $dataRepresentationType"
      )
   }

   override fun getGridData(s4: V1Section4, scaling: Float): MutableList<GpsGridPoint> {
      return gridDefinition.getGridData(s4, scaling)
   }
}


class V1Section3(data: ByteBuffer): V1Section(data)


class V1Section4(private val data: ByteBuffer): V1Section(data) {

   private val flags = getIntFromByte(3)
   private val sphericalHarmonicCoefficients = (flags and 0x00000080) > 0
   private val complexPacking = (flags and 0x00000040) > 0
   private val integerValues = (flags and 0x00000020) > 0
   private val flagsOctet14 = (flags and 0x00000010) > 0
   private val noUnusedBits = flags and 0x0000000F

   private val matrixValues = (flags and 0x00000004) > 0
   private val secondaryBitmap = (flags and 0x00000002) > 0
   private val secondOrderValuesDifferentWidth = (flags and 0x00000001) > 0

   private val scaleFactor = 2f.pow(getSignedIntFrom2Bytes(4))
   private val referenceValue = getFloatFrom4Bytes(6)
   private val numberOfBits = data.get(10).toInt()
   private val rightShift = 32 - numberOfBits


   init {
      Log.d(
         logTag,
         """Section 4($length): flags - harmonicCoefficients: $sphericalHarmonicCoefficients complexPacking: $complexPacking integerValues: $integerValues additionalFlags: $flagsOctet14
            #   scaleFactor: $scaleFactor referenceValue: $referenceValue numberOfBits: $numberOfBits noUnusedBits: $noUnusedBits
         """.trimMargin("#")
      )
   }

   fun decode(index: Int): Float {

      val value = when (numberOfBits) {
         0 -> 0.0f
         16 -> getIntFrom2Bytes(11 + 2 * index).toFloat()
         32 -> getIntFrom4Bytes(11 + 4 * index).toFloat()
         else -> getFloatFromBits(11, index, numberOfBits)
      }

      return referenceValue + value * scaleFactor
   }
}
