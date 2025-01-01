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
 * File: MainActivity.kt
 * Last modified: 01/01/2025, 14:01
 *
 */


package com.nksoftware.skipper.core

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.utilities.nkCheckAndGetPermission
import com.nksoftware.library.utilities.nkHandleException
import com.nksoftware.skipper.coreui.MainScreen

const val logTag = "Skipper"


class MainActivity : ComponentActivity() {

   private lateinit var viewModel: SkipperViewModel
   private lateinit var sharedPreferences: SharedPreferences

   private var mBound: Boolean = false
   private val connection = object : ServiceConnection {

      override fun onServiceConnected(className: ComponentName, service: IBinder) {
         mBound = true
         viewModel.setService(service as LocationService.LocalBinder)
      }

      override fun onServiceDisconnected(arg0: ComponentName) {
         mBound = false
      }
   }

   @RequiresApi(Build.VERSION_CODES.TIRAMISU)
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      Log.i(logTag, "Activity created")

      val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
      StrictMode.setThreadPolicy(policy)

      nkCheckAndGetPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      nkCheckAndGetPermission(this, Manifest.permission.POST_NOTIFICATIONS)

      try {
         val intent = Intent(this, LocationService::class.java)
         this.startForegroundService(intent)
      }

      catch(e: Exception) {
         nkHandleException(logTag, "Exception: cannot start LocationService", e)
      }

      sharedPreferences = getSharedPreferences(skipperPreferences, MODE_PRIVATE)

      viewModel = ViewModelProvider(
         this, SkipperViewModelFactory(this, applicationInfo.dataDir, sharedPreferences)
      )[SkipperViewModel::class.java]

      setContent { MainScreen(this, sharedPreferences, viewModel, applicationInfo.dataDir, ::finish) }
   }


   override fun onStart() {
      super.onStart()
      Log.i(logTag, "Activity started")

      Intent(this, LocationService::class.java).also { intent ->
         bindService(intent, connection, BIND_AUTO_CREATE)
      }
   }


   override fun onResume() {
      super.onResume()
      Log.i(logTag, "Activity resumed")
   }


   override fun onPause() {
      super.onPause()

      Log.i(logTag, "Activity paused")
      viewModel.saveData()
   }


   override fun onStop() {
      super.onStop()
      Log.i(logTag, "Activity stop")

      unbindService(connection)
      mBound = false

      if (!viewModel.track.saveTrack)
         stopService(Intent(this, LocationService::class.java))
   }


   override fun onConfigurationChanged(newConfig: Configuration) {
      super.onConfigurationChanged(newConfig)
      Log.i(logTag, "Configuration changed")
   }

   override fun onRestoreInstanceState(savedInstanceState: Bundle) {
      super.onRestoreInstanceState(savedInstanceState)
      Log.i(logTag, "Restore instance")
   }


   override fun onSaveInstanceState(outState: Bundle) {
      super.onSaveInstanceState(outState)
      Log.i(logTag, "Save instance")
   }
}
