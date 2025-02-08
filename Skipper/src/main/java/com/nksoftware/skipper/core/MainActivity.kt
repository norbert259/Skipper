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
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.nksoftware.library.location.TrackDatabase
import com.nksoftware.library.locationservice.ACTION_LOCATION_BROADCAST
import com.nksoftware.library.locationservice.LocationService
import com.nksoftware.library.utilities.nkCheckAndGetPermission
import com.nksoftware.library.utilities.nkHandleException
import com.nksoftware.skipper.coreui.MainScreen

const val logTag = "Skipper"
const val skipperPreferences = "SkipperPref"


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: SkipperViewModel

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mgr: LocationManager
    private lateinit var trackDb: TrackDatabase


    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val loc: Location? = intent.getParcelableExtra("location")
            val trackUpdate = intent.getBooleanExtra("track", false)

            if (viewModel.gpsLocation.gps) viewModel.setLocation(loc, trackUpdate)
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.i(logTag, "$className connected")
            viewModel.setService(service as LocationService.LocalBinder)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.i(logTag, "$className disconnected")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(logTag, "Activity created")

        nkCheckAndGetPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        nkCheckAndGetPermission(this, Manifest.permission.POST_NOTIFICATIONS)

        mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        sharedPreferences = getSharedPreferences(skipperPreferences, MODE_PRIVATE)

        trackDb = Room.databaseBuilder(this, TrackDatabase::class.java, "SkipperTracks")
            .allowMainThreadQueries()
            .build()

        viewModel = ViewModelProvider(
            this, SkipperViewModelFactory(mgr, applicationInfo.dataDir)
        )[SkipperViewModel::class.java]

        viewModel.load(sharedPreferences, trackDb)

        try {
            val intent = Intent(applicationContext, LocationService::class.java)
            intent.putExtra("gpsProvider", viewModel.gpsLocation.gpsProvider.value)

            startForegroundService(intent)
        } catch (e: Exception) {
            nkHandleException(logTag, "Exception: cannot start LocationService", e)
        }

        registerReceiver(
            broadcastReceiver,
            IntentFilter(ACTION_LOCATION_BROADCAST),
            RECEIVER_EXPORTED
        )

        setContent { MainScreen(viewModel, { this.finish() }) }
    }

    override fun onStart() {
        super.onStart()
        Log.i(logTag, "Activity started")

        Intent(applicationContext, LocationService::class.java).also { intent ->
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
        viewModel.store(sharedPreferences, trackDb)
    }

    override fun onStop() {
        super.onStop()

        Log.i(logTag, "Activity stop and unbind service")
        unbindService(connection)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(logTag, "Activity destroy")

        if (!viewModel.track.saveTrack)
            stopService(Intent(this, LocationService::class.java))

        unregisterReceiver(broadcastReceiver)
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
