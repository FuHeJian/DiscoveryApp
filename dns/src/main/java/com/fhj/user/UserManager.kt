package com.fhj.user

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.byteparse.flatbuffers.ext.MessageMake
import com.fhj.byteparse.flatbuffers.ext.TextMessageMake
import com.fhj.byteparse.flatbuffers.ext.compare
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.dns.DnsHelper
import com.fhj.dns.DnsHelper.me
import com.fhj.id.MessageIdManager

object UserManager {
    private val userMap = mutableMapOf<String, UserData>()

    fun getUser(user: User): UserData? {
        return userMap[user.getKey()]
    }

    fun getUser(userId: String): UserData? {
        return userMap[userId]
    }

    fun addUser(user: User) = userMap.getOrPut(user.getKey()) { UserData(user) }
    fun removeUser(user: User) {
        userMap.remove(user.getKey())
    }
}

class UserData(val user: User) {
    override fun equals(other: Any?): Boolean {
        if(other !is UserData)return false
        return user.compare(other.user)
    }

    var latestMessage: Message? = null

    var onlineStatus = OnlineStatus.OFFLINE

    fun sendText(msg:String,toUser: User):Message{
        val v_send = MessageMake(
            MessageType.Text,
            MessageIdManager.getNextId(user),
            me,
            toUser,
            status = MessageStatus.SENDING,
            unionDataType = MessageData.TextMessage,
        ){
            TextMessageMake(it,text = msg)
        }
        NettyUtil.send(v_send)
        return v_send
    }
}

enum class OnlineStatus {
    ONLINE,
    OFFLINE,
    AWAY
}