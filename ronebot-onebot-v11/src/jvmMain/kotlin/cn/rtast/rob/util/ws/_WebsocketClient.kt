/*
 * Copyright © 2025 RTAkland
 * Author: RTAkland
 * Date: 2025/3/23
 */

@file:Suppress("ClassName")
@file:OptIn(InternalROneBotApi::class)

package cn.rtast.rob.util.ws

import cn.rtast.rob.BotInstance
import cn.rtast.rob.annotations.InternalROneBotApi
import cn.rtast.rob.enums.internal.InstanceType
import cn.rtast.rob.onebot.OneBotAction
import cn.rtast.rob.onebot.OneBotListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

internal class _WebsocketClient(
    address: String,
    accessToken: String,
    private val listener: OneBotListener,
    private val autoReconnect: Boolean,
    private val botInstance: BotInstance,
    private val reconnectInterval: Long,
    private val executeDuration: Duration
) : WebSocketClient(URI(address), mapOf("Authorization" to "Bearer $accessToken")) {

    private var isConnected = false
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var remoteAddress: String? = null

    override fun onOpen(handshakedata: ServerHandshake) {
        remoteAddress = this@_WebsocketClient.remoteSocketAddress.address.toString()
        botInstance.action = OneBotAction(botInstance, InstanceType.Client)
        botInstance.logger.info("Websocket客户端成功连接到服务端 $remoteAddress")
        this.isConnected = true
        coroutineScope.launch { botInstance.messageHandler.onOpen(listener) }
    }

    override fun onMessage(message: String) {
        processIncomingMessage(botInstance, listener, message, executeDuration, botInstance.messageHandler)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        botInstance.logger.warn("Websocket客户端已从服务端断开连接: $remoteAddress")
        this.isConnected = false
        if (autoReconnect) startReconnect()
        coroutineScope.launch {
            botInstance.messageHandler.onClose(listener)
        }
    }

    override fun onError(ex: Exception) {
        botInstance.logger.error("Websocket客户端发生错误: $remoteAddress ${ex.message}")
        ex.printStackTrace()
        coroutineScope.launch { botInstance.messageHandler.onError(listener, ex) }
    }

    private fun startReconnect() {
        scheduler.schedule({
            try {
                botInstance.logger.info("正在重新连接到服务器...")
                reconnect()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }, reconnectInterval, TimeUnit.MILLISECONDS)
    }
}