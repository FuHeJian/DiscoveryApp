package com.fhj.byteparse.flatbuffers.ext

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.User
import com.google.flatbuffers.FlatBufferBuilder
import java.util.Objects

fun UserMake(device: String, name: String, ip: String): User {
    return FlatBufferBuilder(0).run {
        finish(UserMakeOffset(this, device, name, ip))
        User.getRootAsUser(this.dataBuffer())
    }
}

fun UserMakeOffset(builder: FlatBufferBuilder, device: String, name: String, ip: String): Int {
    return builder.run {
        val deviceOffset = this.createString(device)
        val nameOffset = this.createString(name)
        val ipOffset = this.createString(ip)
        User.createUser(this, deviceOffset, nameOffset, ipOffset)
    }
}

fun MessageMake(
    type: Long,
    id: Long,
    fromUser: User,
    toUser: User,
    status: Int,
    data: Objects
): Message {

    return FlatBufferBuilder(0).run {
        val fromUserOffset = UserMakeOffset(this,fromUser.device(),fromUser.name(),fromUser.ip())
        val toUserOffset = UserMakeOffset(this,toUser.device(),toUser.name(),toUser.ip())
        val dataOffset = this.create(data.toString())
        finish(
            Message.createMessage(
                this,
                type,
                id,
                fromUserOffset,
                toUserOffset,
                status,
                dataOffset
            )
        )
        Message.getRootAsMessage(this.dataBuffer())
    }
}