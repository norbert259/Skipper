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
 * File: build.gradle.kts
 * Last modified: 01/01/2025, 14:01
 *
 */

plugins {
   alias(libs.plugins.android.library)
   alias(libs.plugins.kotlin.android)
   alias(libs.plugins.kotlin.compose)
   alias(libs.plugins.devtools.ksp)
}

android {
   namespace = "com.nksoftware.library"
   compileSdk = 35

   defaultConfig {
      minSdk = 31

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      consumerProguardFiles("consumer-rules.pro")
   }

   buildTypes {
      release {
         isMinifyEnabled = false
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
   }
   kotlinOptions {
      jvmTarget = "11"
   }
}

dependencies {
   implementation(libs.androidx.core.ktx)
   implementation(libs.androidx.appcompat)
   implementation(libs.material)

   implementation(libs.androidx.activity.compose)
   implementation(platform(libs.androidx.compose.bom))

   implementation(libs.androidx.ui)
   implementation(libs.androidx.ui.graphics)
   implementation(libs.androidx.ui.tooling.preview)
   implementation(libs.androidx.material3)
   implementation(libs.material.icons.extended)

   implementation(libs.androidx.room.runtime)
   ksp(libs.androidx.room.compiler)

   implementation(libs.osmdroid.android)
   implementation(libs.philjay.mpandroidchart)
   implementation(libs.commons.suncalc)

   testImplementation(libs.junit)
   androidTestImplementation(libs.androidx.junit)
   androidTestImplementation(libs.androidx.espresso.core)
}