package com.fhj.byteparse.flatbuffers.ext

import com.fhj.byteparse.flatbuffers.AudioMessage
import com.fhj.byteparse.flatbuffers.FileMessage
import com.fhj.byteparse.flatbuffers.ImageMessage
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.TextMessage
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.VideoMessage
import com.google.flatbuffers.FlatBufferBuilder

fun UserMake(device: String, deviceSerial:String,name: String, ip: String): User {
    return FlatBufferBuilder(0).run {
        finish(UserMakeOffset(this, device, deviceSerial,name, ip))
        User.getRootAsUser(this.dataBuffer())
    }
}

fun UserMakeOffset(builder: FlatBufferBuilder, device: String, deviceSerial:String,name: String, ip: String): Int {
    return builder.run {
        val deviceOffset = this.createString(device)
        val deviceSerialOffset = this.createString(deviceSerial)
        val nameOffset = this.createString(name)
        val ipOffset = this.createString(ip)
        User.createUser(this, deviceOffset, deviceSerialOffset,nameOffset, ipOffset)
    }
}

fun TextMessageMake(
    builder: FlatBufferBuilder, text: String
): Int {
    return TextMessage.createTextMessage(builder, builder.createString(text))
}

fun ImageMessageMake(
    builder: FlatBufferBuilder,
    imageName: String,
    imageSize: Long,
    imageWidth: Long,
    imageHeight: Long,
    imageStream: ByteArray
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
    audioDuration: Long,
    audioBitrate: Long,
    audioSampleRate: Long,
    audioChannels: Long,
    audioSize: Long,
    audioStream: ByteArray
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
    videoName: String,
    videoSize: Long,
    videoType: String,
    videoDuration: Long,
    videoWidth: Long,
    videoHeight: Long,
    videoBitrate: Long,
    videoFps: Long,
    videoCover: ByteArray,
    videoStream: ByteArray
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
    fileName: String,
    fileSize: Long,
    fileType: String,
    fileData: ByteArray
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
        val fromUserOffset = UserMakeOffset(this, fromUser.device(), fromUser.deviceSerial(),fromUser.name(), fromUser.ip())
        val toUserOffset = if (toUser == null) 0 else UserMakeOffset(
            this,
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

fun Message.compare(message: Message?) = if(message==null) false else this.id() == message.id() && this.fromUser().compare(message.fromUser())
fun User.compare(user: User) = this.ip() == user.ip() && this.deviceSerial() == this.deviceSerial()
fun User.getKey() = "${ip()}-${deviceSerial()}}"
fun Message.getMessageInfo():String{
    return when(this.dataType()){
        MessageData.TextMessage ->{
            (data(TextMessage()) as TextMessage).text()
        }
        MessageData.ImageMessage ->{
            "[图片 ${(data(ImageMessage()) as ImageMessage).imageName()}]"
        }
        MessageData.AudioMessage ->{
            "[音频]"
        }
        MessageData.VideoMessage ->{
            "[视频 ${(data(VideoMessage()) as VideoMessage).videoName()}]"
        }

        else -> ""

    }
}