/**
 * Copyright © 2020-2021 Meowcat Studio <studio@meowcat.org> and contributors.
 *
 * Licensed under the GNU Lesser General Public License version 2.1 or later,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/LGPL-2.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
   java
   kotlin("jvm") version "1.4.30"
   maven
   `maven-publish`
}

group = "org.meowcat"
version = "0.1.1"

repositories {
   mavenCentral()
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

dependencies {
   compileOnly("io.ktor:ktor-client-websockets:1.5.0")
   compileOnly("io.ktor:ktor-client-cio:1.5.0")
   compileOnly("com.squareup.okhttp3:okhttp:4.9.0")
   compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
}
