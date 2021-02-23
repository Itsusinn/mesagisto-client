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

@file:Suppress("NOTHING_TO_INLINE")
package org.meowcat.forward.client

import io.ktor.client.call.* // ktlint-disable no-wildcard-imports
import io.ktor.client.features.websocket.* // ktlint-disable no-wildcard-imports
import io.ktor.http.cio.websocket.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.EOFException
import java.util.concurrent.atomic.AtomicBoolean

fun DefaultClientWebSocketSession.wrap(): KtorWebsocket = KtorWebsocket(this)

class KtorWebsocket(
   private val inner: DefaultClientWebSocketSession
) : ClientWebSocketSession, DefaultWebSocketSession by inner {
   override val call: HttpClientCall = inner.call

   private val closeSignal = AtomicBoolean(false)

   fun isClosed(): Boolean = closeSignal.get()

   private var aliveSignal = now()
   private fun isAlive() = now() - aliveSignal < 30_000

   init {
      launch {
         while (true) {
            if (isClosed()) return@launch
            if (!isAlive()) {
               close()
               break
            }
            send(pingFrame)
            delay(20_000)
         }
      }
      // launch a coroutine listening on receiving frames
      launch {
         try {
            for (frame in incoming) {
               if (isClosed()) return@launch
               aliveSignal = now()
               when (frame) {
                  is Frame.Text -> {
                     try {
                        if (frame.buffer.equals(pingBuffer)) {
                           aliveSignal = now()
                           continue
                        }
                        textFrameHandler?.invoke(frame)
                     } catch (e: Throwable) {
                        uncaughtErrorHandler?.invoke(e)
                     }
                  }
                  is Frame.Pong -> {
                     // logger.info { "receiving pong frame " }
                  }
                  else -> { TODO("Maybe it is needed to impl") }
               }
            }
         } catch (e: ClosedReceiveChannelException) {
            close(CloseReason(CloseReason.Codes.GOING_AWAY, "Cannot receive frame"))
         } catch (e: EOFException) {
            close(CloseReason(CloseReason.Codes.GOING_AWAY, "Cannot receive frame"))
         } catch (e: Throwable) {
            uncaughtErrorHandler?.invoke(e)
         }
      }
   }
   override suspend fun send(frame: Frame) {
      if (isClosed()) return
      try {
         outgoing.send(frame)
      } catch (e: Throwable) {
         close(CloseReason(CloseReason.Codes.GOING_AWAY, "Cannot send Frame"))
      }
   }

   var textFrameHandler: (suspend (Frame.Text) -> Unit)? = null
   inline fun textFrameHandler(
      noinline handler: suspend (Frame.Text) -> Unit
   ): KtorWebsocket {
      textFrameHandler = handler
      return this
   }

   var closeHandler: (suspend (CloseReason) -> Unit)? = null
   inline fun closeHandler(
      noinline handler: suspend (CloseReason) -> Unit
   ): KtorWebsocket {
      closeHandler = handler
      return this
   }

   suspend fun close(
      reason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, "")
   ) {
      if (isClosed()) return
      try {
         closeSignal.set(true)
         textFrameHandler = null
         uncaughtErrorHandler = null
         closeHandler?.invoke(reason)
         closeHandler = null
         inner.cancel()
      } catch (_: Throwable) { }
   }

   var uncaughtErrorHandler: ((Throwable) -> Unit)? = {
      // logger.warn(it) { "uncaughtError \n" + it.stackTrace }
   }
   inline fun uncaughtErrorHandler(
      noinline handler: (Throwable) -> Unit
   ) {
      uncaughtErrorHandler = {
         try {
            handler.invoke(it)
         } catch (e: Throwable) {
            // logger.warn(it) { "uncaughtError\n" + it.stackTrace }
         }
      }
   }
}
