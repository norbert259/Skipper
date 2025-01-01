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
 * File: trackpoint.kt
 * Last modified: 01/01/2025, 14:01
 *
 */

package com.nksoftware.library.location


import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase


@Entity
data class TrackPoint(
   @PrimaryKey(autoGenerate = true) val uid: Int = 0,
   @ColumnInfo(name = "name") val name: String,
   @ColumnInfo(name = "time") val time: Long? = null,
   @ColumnInfo(name = "latitude") val locLat: Float,
   @ColumnInfo(name = "longitude") val locLon: Float,
   @ColumnInfo(name = "altitude") val altitude: Int? = null,
)


@Dao
interface TrackPointDao {
   @Query("SELECT * FROM TrackPoint")
   fun getAll(): List<TrackPoint>

   @Query("SELECT * FROM TrackPoint WHERE name LIKE :name")
   fun findByName(name: String): List<TrackPoint>

   @Insert
   fun insertAll(tps: List<TrackPoint>)

   @Delete
   fun delete(tp: TrackPoint)

   @Query("DELETE FROM TrackPoint WHERE name LIKE :name")
   fun delete(name: String)

   @Query("DELETE FROM TrackPoint")
   fun deleteAll()
}


@Database(entities = [TrackPoint::class], version = 1)
abstract class TrackDatabase : RoomDatabase() {
   abstract fun userDao(): TrackPointDao
}
