/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/22/25, 12:14 AM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package cn.rtast.rob.milky.event.ws.raw

import cn.rtast.rob.milky.enums.internal.MilkyEvents
import cn.rtast.rob.milky.milky.MilkyAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 群名称变更Json解析
 */
@Serializable
public data class RawGroupNameChangeEvent(
    val data: GroupNameChange,
    @SerialName("event_type")
    val eventType: MilkyEvents
) {
    @Serializable
    public data class GroupNameChange(
        /**
         * 群号
         */
        @SerialName("group_id")
        val groupId: Long,
        /**
         * 新的群名称
         */
        val name: String,
        /**
         * 操作者 QQ 号
         */
        @SerialName("operator_id")
        val operatorId: Long
    ) {
        @Transient
        lateinit var action: MilkyAction
    }
}