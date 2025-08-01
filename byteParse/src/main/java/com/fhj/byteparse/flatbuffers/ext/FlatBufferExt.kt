package com.fhj.byteparse.flatbuffers.ext

import com.fhj.byteparse.flatbuffers.AudioMessage
import com.fhj.byteparse.flatbuffers.FileMessage
import com.fhj.byteparse.flatbuffers.ImageMessage
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.TextMessage
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.VideoMessage
import com.fhj.logger.Logger
import com.google.flatbuffers.FlatBufferBuilder
import com.google.flatbuffers.Table

fun UserMake(device: String, deviceSerial: String, name: String, ip: String): User {
    return FlatBufferBuilder(0).run {
        finish(
            UserMakeOffset(
                this,
                device = device,
                deviceSerial = deviceSerial,
                name = name,
                ip = ip
            )
        )
        User.getRootAsUser(this.dataBuffer())
    }
}

fun UserMakeOffset(
    builder: FlatBufferBuilder,
    data: User? = null,
    device: String = data?.device() ?: "",
    deviceSerial: String = data?.deviceSerial() ?: "",
    name: String = data?.name() ?: "",
    ip: String = data?.ip() ?: ""
): Int {
    return builder.run {
        val deviceOffset = this.createString(device)
        val deviceSerialOffset = this.createString(deviceSerial)
        val nameOffset = this.createString(name)
        val ipOffset = this.createString(ip)
        User.createUser(this, deviceOffset, deviceSerialOffset, nameOffset, ipOffset)
    }
}

fun TextMessageMake(
    builder: FlatBufferBuilder, data: TextMessage? = null, text: String = data?.text() ?: ""
): Int {
    return TextMessage.createTextMessage(builder, builder.createString(text))
}

fun ImageMessageMake(
    builder: FlatBufferBuilder,
    data: ImageMessage? = null,
    imageName: String = data?.imageName() ?: "",
    imageSize: Long = data?.imageSize() ?: 0,
    imageWidth: Long = data?.imageWidth() ?: 0,
    imageHeight: Long = data?.imageHeight() ?: 0,
    imageStream: ByteArray = data?.imageStreamAsByteBuffer()?.array() ?: byteArrayOf()
): Int {
    return ImageMessage.createImageMessage(
        builder,
        builder.createString(imageName),
        imageSize,
        imageWidth,
        imageHeight,
        ImageMessage.createImageStreamVector(builder, imageStream)
    )
}

fun AudioMessageMake(
    builder: FlatBufferBuilder,
    data: AudioMessage? = null,
    audioDuration: Long = data?.audioDuration() ?: 0,
    audioBitrate: Long = data?.audioBitrate() ?: 0,
    audioSampleRate: Long = data?.audioSampleRate() ?: 0,
    audioChannels: Long = data?.audioChannels() ?: 0,
    audioSize: Long = data?.audioSize() ?: 0,
    audioStream: ByteArray = data?.audioStreamAsByteBuffer()?.array() ?: byteArrayOf()
): Int {
    return AudioMessage.createAudioMessage(
        builder,
        audioDuration,
        audioBitrate,
        audioSampleRate,
        audioChannels,
        audioSize,
        AudioMessage.createAudioStreamVector(builder, audioStream)
    )
}

fun VideoMessageMake(
    builder: FlatBufferBuilder,
    data: VideoMessage? = null,
    videoName: String = data?.videoName() ?: "",
    videoSize: Long = data?.videoSize() ?: 0,
    videoType: String = data?.videoType() ?: "",
    videoDuration: Long = data?.videoDuration() ?: 0,
    videoWidth: Long = data?.videoWidth() ?: 0,
    videoHeight: Long = data?.videoHeight() ?: 0,
    videoBitrate: Long = data?.videoBitrate() ?: 0,
    videoFps: Long = data?.videoFps() ?: 0,
    videoCover: ByteArray = data?.videoCoverAsByteBuffer()?.array() ?: byteArrayOf(),
    videoStream: ByteArray = data?.videoStreamAsByteBuffer()?.array() ?: byteArrayOf()
): Int {
    return VideoMessage.createVideoMessage(
        builder,
        builder.createString(videoName),
        videoSize,
        builder.createString(videoType),
        videoDuration,
        videoWidth,
        videoHeight,
        videoBitrate,
        videoFps,
        VideoMessage.createVideoCoverVector(builder, videoCover),
        VideoMessage.createVideoStreamVector(builder, videoStream)
    )
}

fun FileMessageMake(
    builder: FlatBufferBuilder,
    data: FileMessage? = null,
    fileName: String = data?.fileName() ?: "",
    fileSize: Long = data?.fileSize() ?: 0,
    fileType: String = data?.fileType() ?: "",
    fileData: ByteArray = data?.fileDataAsByteBuffer()?.array() ?: byteArrayOf()
): Int {
    return FileMessage.createFileMessage(
        builder,
        builder.createString(fileName),
        fileSize,
        builder.createString(fileType),
        FileMessage.createFileDataVector(builder, fileData)
    )
}

/**
 * 构造Message对象
 */
fun MessageMake(
    type: Long,
    id: Long,
    fromUser: User,
    toUser: User?,
    status: Int,
    unionDataType: Byte,
    dataCreator: (builder: FlatBufferBuilder) -> Int = { builder: FlatBufferBuilder -> 0 }
): Message {

    return FlatBufferBuilder(0).run {
        val fromUserOffset = UserMakeOffset(
            this,
            data = null,
            fromUser.device(),
            fromUser.deviceSerial(),
            fromUser.name(),
            fromUser.ip()
        )
        val toUserOffset = if (toUser == null) 0 else UserMakeOffset(
            this,
            data = null,
            toUser.device(),
            toUser.deviceSerial(),
            toUser.name(),
            toUser.ip()
        )
        finish(
            Message.createMessage(
                this,
                type,
                id,
                fromUserOffset,
                toUserOffset,
                status,
                unionDataType,
                dataCreator(this)
            )
        )
        Message.getRootAsMessage(this.dataBuffer())
    }
}

fun Message.compare(message: Message?) =
    if (message == null) false else this.id() == message.id() && this.fromUser()
        .compare(message.fromUser())

fun Message.copy(
    type: Long = this.type(),
    id: Long = this.id(),
    fromUser: User = this.fromUser(),
    toUser: User? = this.toUser(),
    status: Int = this.status(),
    unionDataType: Byte = this.dataType(),
    dataCreator: (builder: FlatBufferBuilder) -> Int = this.copyData()
) = MessageMake(type, id, fromUser, toUser, status, unionDataType, dataCreator)

fun Message.copyData() = { _b: FlatBufferBuilder ->
    when (this.dataType()) {
        MessageData.TextMessage -> {
            val _d = (this.data(TextMessage()) as TextMessage)
            TextMessageMake(_b, data = _d)
        }

        MessageData.ImageMessage -> {
            val _d = (this.data(ImageMessage()) as ImageMessage)
            ImageMessageMake(_b, data = _d)
        }

        MessageData.AudioMessage -> {
            val _d = (this.data(AudioMessage()) as AudioMessage)
            AudioMessageMake(_b, data = _d)
        }

        MessageData.VideoMessage -> {
            val _d = (this.data(VideoMessage()) as VideoMessage)
            VideoMessageMake(_b, data = _d)
        }

        MessageData.FileMessage -> {
            val _d = (this.data(FileMessage()) as FileMessage)
            FileMessageMake(_b, data = _d)
        }

        else -> {
            0
        }
    }
}

fun Message.isSystemMessageType() =
    this.type() == MessageType.DISCOVERY || this.type() == MessageType.CLOSE

fun User.compare(user: User) = this.ip() == user.ip() && this.deviceSerial() == this.deviceSerial()
fun User.getKey() = "${ip()}-${deviceSerial()}}"

fun User?.getInfo() = this?.name() ?: ""

fun Message.log() {

    val prefix =
        "${this.fromUser().getInfo()} --> ${
            this.toUser().getInfo()
        }\n<id>:${this.id()}\n<content>: ${getMessageInfo()}"

    Logger.log(prefix)
}

fun Message.getMessageInfo(): String {

    if (this.data(Table()) == null) return ""
    return when (this.dataType()) {
        MessageData.TextMessage -> {
            (data(TextMessage()) as TextMessage).text()
        }

        MessageData.ImageMessage -> {
            "[图片 ${(data(ImageMessage()) as ImageMessage).imageName()}]"
        }

        MessageData.AudioMessage -> {
            "[音频]"
        }

        MessageData.VideoMessage -> {
            "[视频 ${(data(VideoMessage()) as VideoMessage).videoName()}]"
        }

        else -> ""

    }
}