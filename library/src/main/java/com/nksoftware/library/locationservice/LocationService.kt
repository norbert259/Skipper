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
 * File: LocationService.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.locationservice

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nksoftware.library.R


const val SkipperLocTag = "SkipperLocationService"

const val NOTIFICATION_ID = 1
const val NOTIFICATION_CHANNEL_ID = "LocationUpdates"

const val ACTION_STOP_UPDATES = "ACTION_STOP_UPDATES"
const val ACTION_LOCATION_BROADCAST = "LOCATION_BROADCAST"


class LocationService : Service(), LocationListener {

   inner class LocalBinder : Binder() {

      fun updateNotification() {
         if (!error) {
            notification.setContentText(
               "Tracking %s  Alarm %s".format(
                  if (track.trackingEnabled) "on" else "off",
                  if (alarm.anchorAlarmSet) "on" else "off"
               )
            )

            notificationManager.notify(NOTIFICATION_ID, notification.build())
         }
      }

      fun setTracking(tr: Boolean) {
         Log.i(SkipperLocTag, "Set tracking: %s".format(if (tr) "on" else "off"))

         track.setTracking(tr)
         updateNotification()
      }

      fun setAnchorAlarm(active: Boolean = false, loc: Location? = null, diameter: Float = 100.0f) {
         Log.i(SkipperLocTag, "Set anchor alarm: %s".format(if (active) "on" else "off"))

         alarm.setAnchorAlarm(active, loc, diameter)
         updateNotification()
      }

      fun getTrack(): List<Location> = track.getTrack()

      fun clearTrack() {
         setTracking(false)

         Log.i(SkipperLocTag, "Clear tracking")
         track.clearTrack()

         sendLocation(null, true)
      }
   }


   private val ctx = this
   private val binder = LocalBinder()

   private val track: Track = Track()
   private val alarm: Alarm = Alarm(ctx)

   private var error = false
   private lateinit var notification: NotificationCompat.Builder
   private lateinit var notificationManager: NotificationManager

   private lateinit var mediaPlayer: MediaPlayer


   fun errorNotification(msg: String) {
      error = true
      notification.setContentText("Error: $msg")
      notificationManager.notify(NOTIFICATION_ID, notification.build())
   }


   override fun onCreate() {
      super.onCreate()
      Log.i(SkipperLocTag, "Create service")

      mediaPlayer = MediaPlayer.create(ctx, R.raw.piepser)

      val notificationChannel = NotificationChannel(
         NOTIFICATION_CHANNEL_ID, "LOC_CHANNEL", NotificationManager.IMPORTANCE_DEFAULT
      )

      notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(notificationChannel)

      val pendingIntent = PendingIntent.getActivity(
         this,
         0,
         packageManager.getLaunchIntentForPackage(this.packageName),
         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val stopIntent = PendingIntent.getService(
         this,
         0,
         Intent(this, this::class.java).setAction(ACTION_STOP_UPDATES),
         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
         .setContentTitle("Skipper Location Update Service")
         .setContentText("Tracking off  Alarm off")
         .setContentIntent(pendingIntent)
         .setSmallIcon(R.drawable.anchor_white)
         .addAction(R.drawable.step_icon, "Stop Service", stopIntent)
         .setOngoing(true)
         .setCategory(NotificationCompat.CATEGORY_SERVICE)
         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
         .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

      startForeground(NOTIFICATION_ID, notification.build())
      Log.i(SkipperLocTag, "Service created")
   }

   @SuppressLint("MissingPermission")
   override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
      Log.i(SkipperLocTag, "Received start id: $startId  action: ${intent.action}")

      if (intent.action == ACTION_STOP_UPDATES) {
         Log.i(SkipperLocTag, "Got stop request")
         stopSelf()
      } else {
         Log.i(SkipperLocTag, "Start location updates")

         val mgr = getSystemService(LOCATION_SERVICE) as LocationManager

         val provider = "gps"
         val providers = mgr.getProviders(true)

         if (provider in providers && mgr.isProviderEnabled(provider))
            mgr.requestLocationUpdates(provider, 3000L, 0f, this)
         else {
            errorNotification("GPS unavailable")
            Log.e(SkipperLocTag, "No GPS provider available")
         }
      }

      return START_STICKY
   }

   override fun onBind(intent: Intent?): IBinder {
      Log.i(SkipperLocTag, "Received binding $intent")
      return binder
   }

   override fun onUnbind(intent: Intent?): Boolean {
      Log.i(SkipperLocTag, "UnBinding received $intent")
      return super.onUnbind(intent)
   }

   override fun onDestroy() {
      Log.i(SkipperLocTag, "Shutting down LocationService")
      super.onDestroy()
   }

   private fun sendLocation(loc: Location?, trackUpdate: Boolean = false) {
      val intent = Intent(ACTION_LOCATION_BROADCAST)

      loc?.let { intent.putExtra("location", loc) }
      intent.putExtra("track", trackUpdate)

      sendBroadcast(intent)
   }

   override fun onLocationChanged(p0: Location) {
      alarm.checkForAnchorDrift(p0, mediaPlayer)

      val update = track.addLocation(p0)
      sendLocation(p0, update)
   }
}
