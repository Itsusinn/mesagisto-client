package org.meowcat.forward.client

import io.ktor.http.cio.websocket.Frame
import java.nio.ByteBuffer

internal fun now() = System.currentTimeMillis()

internal const val pingText = "[ping]"
internal val pingBuffer = ByteBuffer.wrap(pingText.toByteArray())
internal val pingFrame = Frame.Text(pingText)
