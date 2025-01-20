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
 * File: OsmMap.kt
 * Last modified: 02/01/2025, 11:04
 *
 */

package com.nksoftware.library.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.nksoftware.library.R
import com.nksoftware.library.location.ExtendedLocation
import com.nksoftware.library.utilities.nkHandleException
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.InputStream
import java.nio.ByteBuffer


const val chartKey = "Chart"
const val openSeaMapKey = "OpenSeaMap"
const val logTag = "Map"

@SuppressLint("ViewConstructor")
class OsmMap(
   private val ctx: Context,
   private val sharedPreferences: SharedPreferences,
   private val dir: String?,
   private val location: ExtendedLocation,
   private val updateLocation: (Double, Double) -> Unit
) : MapView(ctx), MapListener {

   var chart by mutableIntStateOf(0)
   var actualChart by mutableIntStateOf(chart)

   var openSeaMap by mutableStateOf(false)

   var chartTypes = mutableMapOf<String, OnlineTileSourceBase>(
      "Mapnik" to TileSourceFactory.MAPNIK,
      "TopoMap" to TileSourceFactory.OpenTopo
   )

   var zoomLevel by mutableDoubleStateOf(0.0)
   var outerBoundingBox: BoundingBox? by mutableStateOf(null)

   private var scaleBarOverlay: ScaleBarOverlay
   private var rotationOverlay: RotationGestureOverlay

   private var seamarkTileProvider: MapTileProviderBasic
   private var seamarksOverlay: TilesOverlay

   private val locationIcon = ContextCompat.getDrawable(ctx, R.drawable.twotone_navigation_black_48)
   private var locMarker: NkMarker

   private val updateFailure = ctx.getString(R.string.exception_failure_in_osmdroid_update_routine)


   init {
      chart = sharedPreferences.getInt(chartKey, 0)
      openSeaMap = sharedPreferences.getBoolean(openSeaMapKey, false)

      val f = File("${dir}/files/maps")
      if (!f.exists()) f.mkdirs()

      clipToOutline = true
      Configuration.getInstance().userAgentValue = context.applicationContext.packageName

      overlays.add(CompassOverlay(context, this).apply { enableCompass() })

      overlays.add(LatLonGridlineOverlay2().apply {
         setBackgroundColor(Color.TRANSPARENT)
         setFontColor(Color.DKGRAY)
         setTextStyle(Paint.Style.FILL)
         setLineWidth(1.0f)
         setMultiplier(2.0f)
      })

      scaleBarOverlay = ScaleBarOverlay(this).apply {
         setScaleBarOffset(400, 200)
         setCentred(true)
      }
      overlays.add(scaleBarOverlay)

      rotationOverlay = RotationGestureOverlay(this).apply {
         isEnabled = false
      }
      overlays.add(rotationOverlay)

      seamarkTileProvider = MapTileProviderBasic(context, TileSourceFactory.OPEN_SEAMAP)
      seamarksOverlay = TilesOverlay(seamarkTileProvider, context).apply {
         loadingBackgroundColor = Color.TRANSPARENT
      }
      overlays.add(seamarksOverlay)

      setMultiTouchControls(true)
      zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

      minZoomLevel = 3.0
      maxZoomLevel = 18.0

      controller.zoomTo(12.0)
      controller.animateTo(location.locGp)

      locMarker = NkMarker(
         this,
         dragFunc = { lat, lon -> updateLocation(lat, lon) }
      ).apply {
         isEnabled = true
         icon = locationIcon
      }

      val fileFilter = FilenameFilter { _, s -> File(s).extension == "mbtiles" }
      File("${dir}/files/maps").listFiles(fileFilter)?.forEach {
         val name = it.name.substringBeforeLast(".")
         Log.i("Skipper", "Offline tiles found: ${it.name}")
         chartTypes[name] = TileSourceFactory.DEFAULT_TILE_SOURCE
      }

      setOsmChart(chart)
      addMapListener(this)
   }


   fun update(gps: Boolean, location: ExtendedLocation, centerMap: Boolean, snackBar: (String) -> Unit) {
      try {
         setScaleBarDimension(ExtendedLocation.dimensions.index)
         setOpenseaMapOverlay(openSeaMap)

         if (chart != actualChart) {
            setOsmChart(chart)
            actualChart = chart
         }

         val locGP = location.locGp

         if (centerMap) {
            controller.animateTo(locGP)
         }

         setLocationMarker(
            loc = locGP,
            heading = location.appliedHeading ?: 0f,
            description = location.description(ctx),
            gps = gps
         )
      }
      catch (e: Exception) {
         nkHandleException(logTag, updateFailure, e, snackBar)
      }
   }


   fun checkOutsideOfOuterBoundary(): Boolean {
      return (outerBoundingBox != null &&
              (boundingBox.latSouth < outerBoundingBox!!.latSouth || boundingBox.lonWest < outerBoundingBox!!.lonWest ||
              boundingBox.latNorth > outerBoundingBox!!.latNorth || boundingBox.lonEast > outerBoundingBox!!.lonEast)
             )
   }


   override fun onScroll(event: ScrollEvent?): Boolean {
      if (checkOutsideOfOuterBoundary())
         outerBoundingBox = null

      return true
   }


   override fun onZoom(event: ZoomEvent?): Boolean {
      zoomLevel = zoomLevelDouble

      if (checkOutsideOfOuterBoundary())
         outerBoundingBox = null

      return true
   }


   fun setChartType(c: Int) {
      chart = c
      storeSharedPreferences(sharedPreferences)
   }

   fun toggleOpenseaMap() {
      openSeaMap = !openSeaMap
      storeSharedPreferences(sharedPreferences)
   }


   val cacheCapacity: Long
      get() = CacheManager(this).cacheCapacity()

   val cacheUsage: Long
      get() = CacheManager(this).currentCacheUsage()

   fun download() {
      CacheManager(this).downloadAreaAsync(context, boundingBox, 0, 14)
   }


   private fun storeSharedPreferences(pref: SharedPreferences) {
      val edit = pref.edit()

      edit.putInt(chartKey, chart)
      edit.putBoolean(openSeaMapKey, openSeaMap)

      edit.apply()
   }

   fun toggleMapRotation() {
      rotationOverlay.apply {
         isEnabled = !isEnabled
      }

      if (!rotationOverlay.isEnabled)
         mapOrientation = 0f
   }


   fun setScaleBarDimension(dimension: Int) {
      scaleBarOverlay.apply {
         unitsOfMeasure = if (dimension == 1)
            ScaleBarOverlay.UnitsOfMeasure.nautical
         else
            ScaleBarOverlay.UnitsOfMeasure.metric
      }
   }


   fun setOpenseaMapOverlay(seamap: Boolean) {
      seamarksOverlay.isEnabled = seamap
   }


   fun importMap(uri: Uri) {
      val inp: InputStream? = ctx.contentResolver.openInputStream(uri)
      val header = ByteBuffer.allocate(1024)

      val documentFile = DocumentFile.fromSingleUri(ctx, uri)
      val mapFileName = documentFile?.name.toString()

      val out = FileOutputStream("${dir}/files/maps/${mapFileName}")
      while (inp?.read(header.array())!! > 0) {
         out.write(header.array())
      }

      inp.close()
      out.close()
   }


   fun setOsmChart(chart: Int) {
      when (chart) {
         0, 1 -> {
            tileProvider = MapTileProviderBasic(context)
            setTileSource(chartTypes[chartTypes.keys.toList()[chart]])
         }

         else -> {
            val tp = OfflineTileProvider(
               SimpleRegisterReceiver(context),
               arrayOf(File("${dir}/files/maps/${chartTypes.keys.toList()[chart]}.mbtiles"))
            )
            tileProvider = tp
            setTileSource(tileProvider.tileSource)
         }
      }
   }


   fun setCenter(loc: GeoPoint) {
      if (!boundingBox?.contains(loc)!!)
         controller.setCenter(loc)
   }


   fun setLocationMarker(loc: GeoPoint, heading: Float, description: String, gps: Boolean) {
      try {
         locMarker.apply {
            position = loc
            rotation = (360.0f).minus(heading)
            title = description
            isDraggable = !gps
         }
      }
      catch (e: Exception) {
         nkHandleException(logTag, context.getString(R.string.exception_setting_location_marker_in_map), e)
      }
   }
}