package com.fhj.dns

import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.byteparse.flatbuffers.ext.isSystemMessageType
import com.fhj.id.MessageIdManager
import com.fhj.messagestore.MessageStorageManager
import com.fhj.messagestore.MessagesCacheManager
import com.fhj.user.OnlineStatus
import com.fhj.user.UserManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach

object DistributeHelper {

    val messageOnReceive = NettyUtil.dispatchChannel
        .filter { //只接收发给我的消息
            it.toUser() == null || it.toUser().getKey() == DnsHelper.me.getKey()
        }.onEach {
            //首先经过处理在交给ui,发现消息不传递
            when (it.type()) {
                MessageType.DISCOVERY -> {
                    UserManager.addUser(it.fromUser()).onlineStatus = OnlineStatus.ONLINE
                }

                MessageType.CLOSE -> {//离线状态
                    UserManager.getUser(it.fromUser())?.onlineStatus = OnlineStatus.OFFLINE
                }

                else -> {

                }
            }
        }

    val userMessageOnReceive = messageOnReceive.filter {
        !it.isSystemMessageType()
    }.onEach {
        //保存数据
        MessageStorageManager.saveMessage(it.fromUser().getKey(), it)
        //更新id
        MessageIdManager.updateId(it.fromUser(), it.id())
    }

    val systemMessageOnReceive = messageOnReceive.filter {
        it.isSystemMessageType()
    }
}