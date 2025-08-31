/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/18/25, 3:38 AM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package cn.rtast.rob.milky.api.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 发送群公告
 */
@Serializable
internal data class SendGroupAnnouncementAPI(
    @SerialName("group_id")
    val groupId: Long,
    val content: String,
    /**
     * 公告附带图像文件 URI，支持 `file://` `http(s)://` `base64://` 三种格式
     */
    @SerialName("image_uri")
    val imageURI: String?
)