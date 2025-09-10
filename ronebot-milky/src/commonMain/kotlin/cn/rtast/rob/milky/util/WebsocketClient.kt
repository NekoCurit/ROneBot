/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/18/25, 10:40 PM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

@file:OptIn(InternalROneBotApi::class)

package cn.rtast.rob.milky.util

import cn.rtast.rob.annotations.InternalROneBotApi
import cn.rtast.rob.event.dispatchEvent
import cn.rtast.rob.milky.BotInstance
import cn.rtast.rob.milky.MilkyBotFactory
import cn.rtast.rob.milky.event.ws.packed.RawMessageEvent
import cn.rtast.rob.milky.event.ws.packed.WebsocketConnectedEvent
import cn.rtast.rob.milky.event.ws.packed.WebsocketDisconnectedEvent
import cn.rtast.rob.milky.milky.dispatch
import cn.rtast.rob.milky.util.arrow.success
import cn.rtast.rob.util.ID
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal suspend fun BotInstance.connectToEventEndpoint() {
    val wsAddress = when {
        address.startsWith("http://") -> "ws://${address.removePrefix("http://")}/event"
        address.startsWith("https://") -> "wss://${address.removePrefix("https://")}/event"
        else -> throw IllegalArgumentException("$address 不是一个正确的URI")
    }
    val currentBotInstanceID = this.action.getLoginInfo().success().uin.ID
    MilkyBotFactory.botInstances[currentBotInstanceID] = this
    while (true) {
        try {
            httpClient.webSocket("$wsAddress${if (accessToken != null) "?access_token=$accessToken" else ""}") {
                this@connectToEventEndpoint.webSocketSession = this
                val connectedEvent = WebsocketConnectedEvent(action)
                this@connectToEventEndpoint.dispatchEvent(connectedEvent)
                listener.dispatch(connectedEvent)
                logger.info("Websocket连接成功")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val content = frame.readText()
                    launch {
                        logger.debug(content)
                        val rawMessageEvent = RawMessageEvent(action, content)
                        this@connectToEventEndpoint.dispatchEvent(rawMessageEvent)
                        listener.dispatch(rawMessageEvent)
                        handleDispatchEvent(content)
                    }
                }
                val disconnectedEvent = WebsocketDisconnectedEvent(action)
                this@connectToEventEndpoint.dispatchEvent(disconnectedEvent)
                listener.dispatch(disconnectedEvent)
                MilkyBotFactory.botInstances.remove(currentBotInstanceID)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
        delay(5000)
    }
}
