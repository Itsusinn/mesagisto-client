/**
 * Copyright 2020-2021 Meowcat Studio <studio@meowcat.org> and contributors.
 *
 * Licensed under the GNU Lesser General Public License version 3,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       https://opensource.org/licenses/LGPL-3.0
 */

plugins {
   java
   kotlin("jvm") version "1.4.30"
   maven
   `maven-publish`
}

group = "org.meowcat"
version = "0.1.0"

repositories {
   mavenCentral()
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

dependencies {
   compileOnly(Dependency.Ktor.Client.WebSocket)
   compileOnly(Dependency.Ktor.Client.CIO)
   compileOnly(Dependency.Okhttp)
   compileOnly(Dependency.KotlinX.Coroutine)
   compileOnly("io.github.microutils:kotlin-logging-jvm:2.0.2")
}

object Dependency {
   object KotlinX {
      private const val group = "org.jetbrains.kotlinx"
      const val Coroutine = "$group:kotlinx-coroutines-core:${Versions.Coroutine}"
   }
   object Ktor {
      private const val group = "io.ktor"
      const val Core = ""
      object Client {
         const val WebSocket = "$group:ktor-client-websockets:${Versions.Ktor}"
         const val CIO = "$group:ktor-client-cio:${Versions.Ktor}"
      }
   }
   const val Okhttp = "com.squareup.okhttp3:okhttp:${Versions.Okhttp}"
}
object Versions {
   const val Kotlin = "1.4.30"
   const val Okhttp = "4.9.0"
   const val Ktor = "1.5.0"
   const val Coroutine = "1.4.1"
}
