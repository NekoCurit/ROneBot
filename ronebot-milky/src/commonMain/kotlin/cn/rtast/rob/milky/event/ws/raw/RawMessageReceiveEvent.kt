/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/21/25, 11:04 PM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

@file:OptIn(ExperimentalUuidApi::class)

package cn.rtast.rob.milky.event.ws.raw

import arrow.core.Either
import cn.rtast.rob.entity.IGroupMessage
import cn.rtast.rob.entity.IPrivateMessage
import cn.rtast.rob.milky.actionable.CommonGroupEventActionable
import cn.rtast.rob.milky.actionable.GroupEssenceActionable
import cn.rtast.rob.milky.actionable.MessageActionable
import cn.rtast.rob.milky.enums.MessageScene
import cn.rtast.rob.milky.enums.internal.MilkyEvents
import cn.rtast.rob.milky.event.common.Friend
import cn.rtast.rob.milky.event.common.Group
import cn.rtast.rob.milky.event.common.GroupMember
import cn.rtast.rob.milky.event.group.GetGroupEssenceMessages
import cn.rtast.rob.milky.event.message.SendMessageResponse
import cn.rtast.rob.milky.exceptions.NotAGroupMessageException
import cn.rtast.rob.milky.milky.MessageChain
import cn.rtast.rob.milky.milky.MilkyAction
import cn.rtast.rob.milky.milky.messageChain
import cn.rtast.rob.milky.milky.text
import cn.rtast.rob.milky.segment.ReceiveSegment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 消息接收Json解析
 */
@Serializable
public data class RawMessageReceiveEvent(
    val data: IncomingMessage,
    @SerialName("event_type")
    val eventType: MilkyEvents,
) {
    @Serializable
    public data class IncomingMessage(
        /**
         * 好友信息
         */
        val friend: Friend?,
        /**
         * 群聊信息
         * 如果[messageScene]为[MessageScene.Friend]时必定为空
         * 如果[messageScene]为[MessageScene.Group]时必定不为空表示该群信息
         * 如果[messageScene]为[MessageScene.Temp]时可能为空表示临时会话发送者的所在的群信息
         */
        val group: Group?,
        /**
         * 好友 QQ 号或群号
         */
        @SerialName("peer_id")
        val peerId: Long,
        /**
         * 消息序列号
         */
        @SerialName("message_seq")
        val messageSeq: Long,
        /**
         * 发送者 QQ 号
         */
        @SerialName("sender_id")
        val senderId: Long,
        /**
         * 消息 Unix 时间戳（秒）
         */
        val time: Long,
        /**
         * 消息段列表
         */
        val segments: List<ReceiveSegment>,
        /**
         * 类型标识符
         */
        @SerialName("message_scene")
        val messageScene: MessageScene,
        /**
         * 群成员信息, 可能为空, 具体情况见[group]字段的注释
         */
        @SerialName("group_member")
        val groupMember: GroupMember?,
    ) : MessageActionable, CommonGroupEventActionable,
        GroupEssenceActionable, IGroupMessage, IPrivateMessage {
        @Transient
        lateinit var action: MilkyAction

        @Transient
        override var sessionId: Uuid? = null

        @JvmBlocking
        override suspend fun reply(message: MessageChain): Either<String, SendMessageResponse.SendMessage> {
            return when (messageScene) {
                MessageScene.Friend -> action.sendPrivateMessage(peerId, message)
                MessageScene.Group -> action.sendGroupMessage(peerId, message)
                MessageScene.Temp -> throw IllegalStateException()
            }
        }

        @JvmBlocking
        override suspend fun reply(text: Any): Either<String, SendMessageResponse.SendMessage> {
            val msg = messageChain { text(text) }
            return when (messageScene) {
                MessageScene.Friend -> action.sendPrivateMessage(peerId, msg)
                MessageScene.Group -> action.sendGroupMessage(peerId, msg)
                MessageScene.Temp -> throw IllegalStateException()
            }
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun reaction(faceId: String) {
            if (messageScene == MessageScene.Group) {
                action.reaction(peerId, messageSeq, faceId, true)
            }
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun unsetReaction(faceId: String) {
            if (messageScene == MessageScene.Group) {
                action.reaction(peerId, messageSeq, faceId, false)
            }
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun recall() {
            when (messageScene) {
                MessageScene.Group -> action.recallGroupMessage(peerId, messageSeq)
                else -> action.recallPrivateMessage(peerId, messageSeq)
            }
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun markAsRead() {
            action.markMessageAsRead(messageScene, peerId, messageSeq)
        }

        @JvmBlocking
        override suspend fun getGroupInfo(): Either<String, Group> {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            return action.getGroupInfo(group!!.groupId, true)
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun setEssence(messageSeq: Long) {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            action.setGroupEssenceMessage(group!!.groupId, messageSeq, true)
        }

        @JvmAsync
        @JvmBlocking
        override suspend fun unsetEssence(messageSeq: Long) {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            action.setGroupEssenceMessage(group!!.groupId, messageSeq, false)
        }

        @JvmBlocking
        override suspend fun getGroupEssenceMessages(): Either<String, GetGroupEssenceMessages.GroupEssenceMessages> {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            return action.getGroupEssenceMessages(group!!.groupId, 0, 0)
        }

        @JvmBlocking
        override suspend fun getGroupEssenceMessages(pageIndex: Int): Either<String, GetGroupEssenceMessages.GroupEssenceMessages> {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            return action.getGroupEssenceMessages(group!!.groupId, 0, pageIndex)
        }

        @JvmBlocking
        override suspend fun getGroupEssenceMessages(
            pageSize: Int,
            pageIndex: Int,
        ): Either<String, GetGroupEssenceMessages.GroupEssenceMessages> {
            if (messageScene != MessageScene.Group) throw NotAGroupMessageException()
            return action.getGroupEssenceMessages(group!!.groupId, pageSize, pageIndex)
        }
    }
}