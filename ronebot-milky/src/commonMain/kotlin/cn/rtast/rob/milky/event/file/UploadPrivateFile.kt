/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/18/25, 9:00 PM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package cn.rtast.rob.milky.event.file

import cn.rtast.rob.milky.enums.internal.ApiStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 上传私聊文件
 */
@Serializable
public data class UploadPrivateFile(
    val data: PrivateFile?,
    val status: ApiStatus,
    val message: String?
) {
    @Serializable
    public data class PrivateFile(
        /**
         * 文件 ID
         */
        @SerialName("file_id")
        val fileId: String
    )
}