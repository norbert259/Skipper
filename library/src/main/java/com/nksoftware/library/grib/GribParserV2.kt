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
 * File: GribParserV2.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.grib

import android.util.Log
import com.nksoftware.library.utilities.nkHandleException
import java.io.InputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.pow


class V2GribFileEntry(inp: InputStream) : GribFileEntry() {

   private var section0: V2Section0
   private var section1: V2Section1? = null
   private var section2: V2Section2? = null
   private var section3: V2Section3? = null
   private var section4: V2Section4? = null
   private var section5: V2Section5? = null
   private var section6: V2Section6? = null
   private var section7: V2Section7? = null

   init {
      val data = ByteBuffer.allocate(16)

      inp.read(data.array())

      section0 = V2Section0(data)
      var nextSection = getNextSection(inp)

      while (nextSection.array().size > 4) {
         when (nextSection.get(4).toInt()) {
            1    -> section1 = V2Section1(nextSection)
            2    -> section2 = V2Section2(nextSection)
            3    -> section3 = V2Section3(nextSection)
            4    -> section4 = V2Section4(nextSection)
            5    -> section5 = V2Section5(nextSection)
            6    -> section6 = V2Section6(nextSection)
            7    -> section7 = V2Section7(nextSection)
            else -> {}
         }
         nextSection = getNextSection(inp)
      }
   }

   private fun getNextSection(inp: InputStream): ByteBuffer {
      val start = ByteBuffer.allocate(4)
      inp.read(start.array())

      if (String(start.array()) in listOf("GRIB", "7777"))
         return start
      else {
         val length = start.getInt(0)
         val data = ByteBuffer.allocate(length)

         data.put(start.array(), 0, 4)
         inp.read(data.array(), 4, length - 4)

         return data
      }
   }


   override fun getParameter(): String {
      return section4!!.getParameter(section0.discipline)
   }

   override fun getReferenceTime(): Calendar {
      return section4!!.getTime(section1!!.referenceTime)
   }

   override fun getGridInfo(type: GribFile.GridInfo): Float {
      return section3!!.getGridInfo(type)
   }

   override fun getGridValues(): MutableList<GpsGridPoint> {
      return section3!!.getGridData(section5!!, section7!!)
   }
}


private fun getShort(data: ByteBuffer, ind: Int): Int {
   val firstByte = data.get(ind).toUByte()
   val negative = firstByte.and(128u).compareTo(128u) == 0

   val upperValue = firstByte.and(127u).toInt()
   val value = (upperValue shl 8) + data.get(ind + 1).toUByte().toInt()

   return if (negative) -value else value
}


open class V2Section(buffer: ByteBuffer) {
   val length = buffer.limit()
   var errorCounter = 0
}


class V2Section0(private val data: ByteBuffer) : V2Section(data) {

   val discipline = data.get(6).toInt()
   val editionNumber: Byte = data.get(7)
   val totalLength: Long = data.getLong(8)

   init {
      Log.d(
         logTag,
         "Section 0($length): Discipline: $discipline Edition no: $editionNumber Total length: $totalLength"
      )

      if (editionNumber.compareTo(2) != 0)
         throw Exception("Only grib edition 2 supported")
   }
}


class V2Section1(private val data: ByteBuffer) : V2Section(data) {

   private val originatingCenter: Short = data.getShort(5)
   private val originatingSubCenter: Short = data.getShort(7)
   private val gribMasterTableVersion: Byte = data.get(9)
   private val gribLocalTableVersion: Byte = data.get(10)
   private val significanceReferenceTime: Byte = data.get(11)

   private val year: Short = data.getShort(12)
   private val month: Byte = data.get(14)
   private val day: Byte = data.get(15)
   private val hour: Byte = data.get(16)
   private val minute: Byte = data.get(17)
   private val second: Byte = data.get(18)

   private val dateFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
   val referenceTime: Calendar = Calendar.getInstance()

   init {
      referenceTime.set(Calendar.YEAR, year.toInt())
      referenceTime.set(Calendar.MONTH, month.toInt() - 1)
      referenceTime.set(Calendar.DAY_OF_MONTH, day.toInt())
      referenceTime.set(Calendar.HOUR_OF_DAY, hour.toInt())
      referenceTime.set(Calendar.MINUTE, minute.toInt())
      referenceTime.set(Calendar.SECOND, second.toInt())
      referenceTime.set(Calendar.MILLISECOND, 0)

      Log.d(
         logTag,
         """Section 1($length): Originating center: $originatingCenter Originating sub center: $originatingSubCenter Date: ${dateFormatter.format(referenceTime.time)}
            #   Master table: $gribMasterTableVersion Local table: $gribLocalTableVersion
            #   Significance of reference time: $significanceReferenceTime Reference time: ${dateFormatter.format(referenceTime.time)}
         """.trimMargin("#")
      )
   }
}


class V2Section2(private val data: ByteBuffer) : V2Section(data)


abstract class V2Template3 {
   abstract fun getGridInfo(type: GribFile.GridInfo): Float
   abstract fun getGridData(s5: V2Section5, s7: V2Section7): MutableList<GpsGridPoint>
}


class V2Template30(private val data: ByteBuffer) : V2Template3() {

   val shapeOfEarth: Byte = data.get(14)
   val scaleFactorSphericalEarth: Byte = data.get(15)
   val scaleValueSphericalEarth: Int = data.getInt(16)

   val scaleFactorMajorAxis: Byte = data.get(20)
   val scaleValueMajorAxis: Int = data.getInt(21)
   val scaleFactorMinorAxis: Byte = data.get(25)
   val scaleValueMinorAxis: Int = data.getInt(26)

   val noLon: Int = data.getInt(30)
   val noLat: Int = data.getInt(34)

   val firstLat: Float = data.getInt(46).toFloat() / 1000000
   val firstLon: Float = data.getInt(50).toFloat() / 1000000

   val resolution: Byte = data.get(54)

   val lastLat: Float = data.getInt(55).toFloat() / 1000000
   val lastLon: Float = data.getInt(59).toFloat() / 1000000

   var lonIncrement: Float = data.getInt(63).toFloat() / 1000000
   var latIncrement: Float = data.getInt(67).toFloat() / 1000000

   val scanningMode: UShort = data.get(71).toUByte().toUShort()

   private val lonScanNegativ = (scanningMode and 0x0080u) > 0u
   private val latScanNegativ = !((scanningMode and 0x0040u) > 0u)
   private val southNorthDirectionConsecutive = ((scanningMode and 0x0020u) > 0u)
   private val adjacentRowsScanOpposite = ((scanningMode and 0x0010u) > 0u)

   private val pointsWithOddRowsOffset = (scanningMode and 0x0008u) > 0u
   private val pointsWithEvenRowsOffset = ((scanningMode and 0x0004u) > 0u)
   private val pointsLatOffset = ((scanningMode and 0x0002u) > 0u)
   private val noPointsLonReduced = ((scanningMode and 0x0001u) > 0u)


   init {
      Log.d(
         logTag,
         """Template 30: Shape of earth: $shapeOfEarth Scale factor radius: $scaleFactorSphericalEarth Scale value radius: $scaleValueSphericalEarth
            #   Scale factor major: $scaleFactorMajorAxis Scale value major: $scaleValueMajorAxis Scale factor minor: $scaleFactorMinorAxis Scale value minor: $scaleValueMinorAxis
            #   Points parallel: $noLon Point meridian: $noLat
            #   First lat: $firstLat First lon: $firstLon Last lat: $lastLat Last lon: $lastLon
            #   iIncrement: $lonIncrement jIncrement: $latIncrement Scanning mode: $scanningMode
         """.trimMargin("#")
      )

      if (lonScanNegativ) lonIncrement = -lonIncrement
      if (latScanNegativ) latIncrement = -latIncrement
   }


   override fun getGridInfo(type: GribFile.GridInfo): Float {
      return when (type) {
         GribFile.GridInfo.MinLat -> firstLat
         GribFile.GridInfo.MinLon -> firstLon
         GribFile.GridInfo.MaxLat -> lastLat
         GribFile.GridInfo.MaxLon -> lastLon
         GribFile.GridInfo.NoLat  -> noLat.toFloat()
         GribFile.GridInfo.NoLon  -> noLon.toFloat()
         GribFile.GridInfo.LatInc -> lonIncrement
         GribFile.GridInfo.LonInc -> latIncrement
      }
   }

   override fun getGridData(s5: V2Section5, s7: V2Section7): MutableList<GpsGridPoint> {
      val gridPoints = mutableListOf<GpsGridPoint>()

      try {
         if (southNorthDirectionConsecutive) {
            for (i in 0 until noLon) {
               for (j in 0 until noLat) {
                  gridPoints.add(
                     GpsGridPoint(
                     (firstLat + j * latIncrement) / 1000f,
                     (firstLon + i * lonIncrement) / 1000f,
                     s5.get(i * noLat + j, s7)
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
                     s5.get(i * noLon + j, s7)
                  )
                  )
               }
            }
         }
      }

      catch(e: Exception) {
         nkHandleException(logTag, "Exception: Cannot decode values in Template30", e)
      }

      return gridPoints
   }
}


class V2Section3(private val data: ByteBuffer) : V2Section(data) {

   private val sourceGrid: Byte = data.get(5)
   private val noDataPoints: Int = data.getInt(6)
   private val optionalListOfNumbers: Byte = data.get(10)
   private val interpretationListOfNumbers: Byte = data.get(11)

   private val gridDefinitionTemplate: Short = data.getShort(12)
   var template: V2Template3? = null

   init {
      Log.d(
         logTag,
         """
         #Section 3($length): Source grid: $sourceGrid No of data points: $noDataPoints 
         #   Octets for optional list: $optionalListOfNumbers Interpretation: $interpretationListOfNumbers
         #   Grid definition template: $gridDefinitionTemplate
         """.trimMargin("#")
      )

      when (gridDefinitionTemplate.toInt()) {
         0 -> template = V2Template30(data)
         else -> {
            throw Exception("Unknown Section3 template: $gridDefinitionTemplate")
         }
      }
   }

   fun getGridInfo(type: GribFile.GridInfo): Float {
      return template!!.getGridInfo(type)
   }

   fun getGridData(s5: V2Section5, s7: V2Section7): MutableList<GpsGridPoint> {
      return template!!.getGridData(s5, s7)
   }
}


abstract class V2Template4(open val data: ByteBuffer) {
   abstract fun getTime(refTime: Calendar): Calendar
}


class V2Template40(override val data: ByteBuffer): V2Template4(data) {

   val typeGeneratingProcess: Byte = data.get(11)
   private val hoursAfterReferenceTime: Short = data.getShort(14)
   private val minutesAfterReferenceTime: Byte = data.get(16)
   private val unitTimeRange: Byte = data.get(17)
   private val forecastTimeUnits: Int = data.getInt(18)

   init {
      Log.d(
         logTag,
         """Template 40: Hours after ref time: $hoursAfterReferenceTime Minutes after ref time: $minutesAfterReferenceTime
            #   Unit time range: $unitTimeRange Forecast time units: $forecastTimeUnits
         """.trimMargin("#"))
   }

   override fun getTime(refTime: Calendar): Calendar {

      val cal: Calendar = Calendar.getInstance()

      cal.set(Calendar.YEAR, refTime.get(Calendar.YEAR))
      cal.set(Calendar.MONTH, refTime.get(Calendar.MONTH))
      cal.set(Calendar.DAY_OF_MONTH, refTime.get(Calendar.DAY_OF_MONTH))
      cal.set(Calendar.HOUR_OF_DAY, refTime.get(Calendar.HOUR_OF_DAY))
      cal.set(Calendar.MINUTE, refTime.get(Calendar.MINUTE))
      cal.set(Calendar.SECOND, refTime.get(Calendar.SECOND))
      cal.set(Calendar.MILLISECOND, 0)

      val timeIncrement = when (unitTimeRange.toInt()) {
         0 -> forecastTimeUnits
         1 -> forecastTimeUnits * 60
         2 -> forecastTimeUnits * 60 * 24
         else -> throw Exception("Only Time unit 1,2,3 supported - found: $unitTimeRange")
      }

      cal.add(Calendar.MINUTE, timeIncrement)
      return cal
   }
}


class V2Template48(override val data: ByteBuffer) : V2Template4(data) {

   private val typeGeneratingProcess: Byte = data.get(11)
   private val hoursAfterReferenceTime: Short = data.getShort(14)
   private val minutesAfterReferenceTime: Byte = data.get(16)
   private val unitTimeRange: Byte = data.get(17)
   private val forecastTimeUnits: Int = data.getInt(18)

   init {
      Log.d(
         logTag,
         """Template 48: Hours after ref time: $hoursAfterReferenceTime Minutes after ref time: $minutesAfterReferenceTime
            #   Unit time range: $unitTimeRange Forecast time units: $forecastTimeUnits
         """.trimMargin("#"))
   }

   override fun getTime(refTime: Calendar): Calendar {

      val cal: Calendar = Calendar.getInstance()

      cal.set(Calendar.YEAR, refTime.get(Calendar.YEAR))
      cal.set(Calendar.MONTH, refTime.get(Calendar.MONTH))
      cal.set(Calendar.DAY_OF_MONTH, refTime.get(Calendar.DAY_OF_MONTH))
      cal.set(Calendar.HOUR_OF_DAY, refTime.get(Calendar.HOUR_OF_DAY))
      cal.set(Calendar.MINUTE, refTime.get(Calendar.MINUTE))
      cal.set(Calendar.SECOND, refTime.get(Calendar.SECOND))
      cal.set(Calendar.MILLISECOND, 0)

      val timeIncrement = when (unitTimeRange.toInt()) {
         0 -> forecastTimeUnits
         1 -> forecastTimeUnits * 60
         2 -> forecastTimeUnits * 60 * 24
         else -> throw Exception("Only Time unit 1,2,3 supported - found: $unitTimeRange")
      }

      cal.add(Calendar.MINUTE, timeIncrement)
      return cal
   }
}


class V2Section4(data: ByteBuffer) : V2Section(data) {

   private val noCoordinates: Short = data.getShort(5)
   private val productDefTemplate: Short = data.getShort(7)
   private var template: V2Template4? = null
   private val parameterCategory: Byte = data.get(9)
   private val parameterNo: Byte = data.get(10)

   private val codeTable42 = mapOf(
      "0_0_0" to "temperature",
      "0_1_1" to "humidity",
      "0_1_8" to "precipitation",
      "0_2_2" to "wind (u)",
      "0_2_3" to "wind (v)",
      "0_2_22" to "wind speed (gust)",
      "0_3_1" to "pressure",
      "0_6_1" to "cloud cover",
      "0_7_6" to "convective Energy",
      "10_0_3" to "wave height",
      "10_0_4" to "wave direction",
      "10_0_5" to "wind wave height",
      "10_0_6" to "wave period",
      "10_0_7" to "swell direction",
      "10_0_8" to "swell wave height",
      "10_0_9" to "swell wave period"
   )

   init {
      Log.d(
         logTag,
         """Section 4($length): No coordinates: $noCoordinates product definition template: $productDefTemplate
            #   parameter category: $parameterCategory parameter no: $parameterNo
         """.trimMargin("#")
      )

      when (productDefTemplate.toInt()) {
         0 -> template = V2Template40(data)
         8 -> template = V2Template48(data)
         else -> {
            Log.e(logTag, "Unsupported template type in section 4: $productDefTemplate")
         }
      }
   }

   fun getParameter(discipline: Int): String {
      val key = "${discipline}_${parameterCategory}_${parameterNo}"

      return if (key in codeTable42.keys) codeTable42[key]!!
         else "${discipline}_${parameterCategory}_${parameterNo}"
   }

   fun getTime(refTime: Calendar): Calendar {
      return template!!.getTime(refTime)
   }
}


abstract class V2Template5 {
   abstract fun get(ind: Int, data: V2Section7): Float
}


class V2Template50(data: ByteBuffer) : V2Template5() {

   private val referenceValue: Float = data.getFloat(11)
   private var binaryScaleFactor = 2f.pow(getShort(data, 15))
   private val decimalScaleFactor = getShort(data, 17)

   private val numberOfBitsUsed: Byte = data.get(19)
   private val typeOfOriginalField: Byte = data.get(20)

   init {
      Log.d(
         logTag,
         """Template 50: Reference value: $referenceValue Binary scale factor: $binaryScaleFactor Decimal scale factor: $decimalScaleFactor
            #   No of bits used: $numberOfBitsUsed Type of original field: $typeOfOriginalField
         """.trimMargin("#")
      )
   }

   override fun get(ind: Int, data: V2Section7): Float {

      val value = when (numberOfBitsUsed.toInt()) {
         0 -> 0.0f
         8 -> data.get(ind + 1).toFloat()
         16 -> data.getShort(2 * ind + 1).toUShort().toFloat()
         32 -> data.getInt(4 * ind + 1).toUInt().toFloat()

         else -> {
            val pos = numberOfBitsUsed * ind

            val byteBoundary = pos / 8
            val offset = pos % 8

            ((data.getInt(byteBoundary).toUInt() shl offset) shr (32 - numberOfBitsUsed)).toFloat()
         }
      }

      return (referenceValue + value * binaryScaleFactor) / decimalScaleFactor
   }
}

class V2Template53(data: ByteBuffer) : V2Template5() {

   private val referenceValue = data.getFloat(11)
   private var binaryScaleFactor = 2f.pow(getShort(data, 15))
   private val decimalScaleFactor = 10f.pow(getShort(data, 17))
   private val numberOfBitsUsed = data.get(19).toUByte().toInt()

   private val typeOfOriginalField = data.get(20).toUByte().toInt()
   private val typeFloatingValues = typeOfOriginalField == 0
   private val typeIntegerValues = typeOfOriginalField == 1

   private val groupSplittingMethod = data.get(21).toUByte().toInt()
   private val rowByRowSplitting = groupSplittingMethod == 0
   private val generalGroupSplitting = groupSplittingMethod == 1

   private val missingValueManagement = data.get(22).toUByte().toInt()
   private val primaryMissingValuesIncluded = missingValueManagement == 1
   private val primaryAndSecondaryValuesIncluded = missingValueManagement == 2
   private val primaryMissingValue = data.getInt(23)
   private val secondaryMissingValue = data.getInt(27)

   private val noGroups = data.getInt(31)
   private val referenceForGroupWidth = data.get(35).toUByte().toInt()
   private val numberOfBitsUsedForGroupWidth = data.get(36).toUByte().toInt()
   private val numberOfValuesInGroup = data.getInt(37)
   private val lengthIncrement = data.get(41).toInt()
   private val trueLengthOfLastGroup = data.get(42).toInt()
   private val numberOfBitsForScaledGroupLength = data.get(46).toUByte().toInt()

   private val orderOfSpatialDifferencing = data.get(47).toUByte().toInt()
   private val firstOrderSpatialDifferencing = orderOfSpatialDifferencing == 1
   private val secondOrderSpatialDifferencing = orderOfSpatialDifferencing == 2
   private val noOfOctetsRequired = data.get(48).toUByte().toInt()

   init {
      Log.d(
         logTag,
         "Template 53: Reference value: $referenceValue Binary scale factor: $binaryScaleFactor Decimal scale factor: $decimalScaleFactor"
      )
      Log.d(
         logTag,
         "Template 53: No of bits used: $numberOfBitsUsed Type of original field: $typeOfOriginalField"
      )
      Log.d(
         logTag,
         "Template 53: group splitting: $groupSplittingMethod missing value management: $missingValueManagement no of groups: $noGroups"
      )
   }

   override fun get(ind: Int, data: V2Section7): Float {

      val value = when (numberOfBitsUsed) {
         0 -> 0.0f
         8 -> data.get(ind).toFloat()
         16 -> data.getShort(2 * ind).toUShort().toFloat()
         32 -> data.getInt(4 * ind).toUInt().toFloat()

         else -> {
            val pos = numberOfBitsUsed * ind

            val byteBoundary = pos / 8
            val offset = pos % 8

            ((data.getInt(byteBoundary).toUInt() shl offset) shr (32 - numberOfBitsUsed)).toFloat()
         }
      }

      return (referenceValue + value * binaryScaleFactor) / decimalScaleFactor
   }
}

class V2Section5(private val data: ByteBuffer) : V2Section(data) {

   private val noDataPoints: Int = data.getInt(5)
   private val dataRepresentationTemplateNo: Short = data.getShort(9)
   private var template: V2Template5? = null

   init {data
      Log.d(
         logTag,
         "Section 5($length): No data points: $noDataPoints Data representation template: $dataRepresentationTemplateNo"
      )

      when (dataRepresentationTemplateNo.toInt()) {
         0 -> template = V2Template50(data)
         3 -> template = V2Template53(data)
         else -> {
            Log.e(
               logTag,
               "Unsupported dataRepresentationTemplate($dataRepresentationTemplateNo) in section 5"
            )
            errorCounter++
         }
      }
   }

   fun get(ind: Int, data: V2Section7): Float {
      return template!!.get(ind, data)
   }
}


class V2Section6(private val data: ByteBuffer) : V2Section(data) {

   private val bitMapIndicator = data.get(5).toInt()

   init {
      Log.d(logTag, "Section 6($length): bitMapIndicator: $bitMapIndicator")

      if (bitMapIndicator >= 0)
         Log.e(logTag, "Bitmap not supported")
   }
}


class V2Section7(private val data: ByteBuffer) : V2Section(data) {

   init {
      Log.d(logTag, "Section 7($length): data length: $length")
   }

   fun get(ind: Int): UByte {
      return data.get(5 + ind).toUByte()
   }

   fun getShort(ind: Int): Short {
      return data.getShort(5 + ind)
   }

   fun getInt(ind: Int): Int {
      return data.getInt(5 + ind)
   }
}

