/*
 * Copyright © 2025 RTAkland & 小满1221
 * Date: 5/18/25, 4:38 AM
 * Open Source Under Apache-2.0 License
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package cn.rtast.rob.milky.segment

import cn.rtast.rob.milky.enums.ImageSubType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface ReceiveSegment {
    public val type: ReceiveSegmentType
}

@Serializable
public data class RTextSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.text
) : ReceiveSegment {
    @Serializable
    public data class Data(
        val text: String
    )
}

@Serializable
public data class RAtSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.at
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("user_id")
        val userId: Long
    )
}

@Serializable
public data class RFaceSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.face
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("face_id")
        val faceId: String
    )
}

@Serializable
public data class RReplySegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.reply
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("message_seq")
        val messageSeq: Long
    )
}

@Serializable
public data class RImageSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.image
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("resource_id")
        val resourceId: String,
        val summary: String?,
        @SerialName("sub_type")
        val imageSubType: ImageSubType
    )
}

@Serializable
public data class RRecordSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.record
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("forward_id")
        val forwardId: String,
        /**
         * 秒
         */
        val duration: Int
    )
}

@Serializable
public data class RVideoSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.video
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("resource_id")
        val resourceId: String
    )
}

@Serializable
public data class RForwardSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.forward
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("forward_id")
        val forwardId: String
    )
}

@Serializable
public data class RMarketFaceSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.market_face
) : ReceiveSegment {
    @Serializable
    public data class Data(
        val url: String
    )
}

@Serializable
public data class RLightAppSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.light_app
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("app_name")
        val appName: String,
        @SerialName("json_payload")
        val jsonPayload: String
    )
}

@Serializable
public data class RXMLSegment(
    val data: Data,
    override val type: ReceiveSegmentType = ReceiveSegmentType.xml
) : ReceiveSegment {
    @Serializable
    public data class Data(
        @SerialName("service_id")
        val serviceId: Int,
        @SerialName("xml_payload")
        val xmlPayload: String
    )
}