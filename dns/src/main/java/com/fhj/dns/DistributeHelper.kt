package com.fhj.dns

import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.TextMessage
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.logger.Logger
import com.fhj.user.OnlineStatus
import com.fhj.user.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import java.net.InetAddress

object DistributeHelper {

    val allDiscoveryAddress = mutableSetOf<InetAddress>()

    val allExposureAddressOnUpdate = MutableStateFlow<Set<InetAddress>>(allDiscoveryAddress)

    val messageOnReceive = NettyUtil.dispatchChannel.onEach {
        //首先经过处理在交给ui,发现消息不传递
        when (it.type()) {
            MessageType.DISCOVERY -> {
                UserManager.addUser(it.fromUser()).onlineStatus = OnlineStatus.ONLINE
            }

            MessageType.CLOSE -> {//离线状态
                UserManager.getUser(it.fromUser())?.onlineStatus = OnlineStatus.OFFLINE
            }

            MessageType.Text -> {
                val t = TextMessage.getRootAsTextMessage(it.byteBuffer)
                Logger.log(
                    "text message  ${
                        t.text()
                    }->${it.id()}"
                )
            }

            else -> {
                Logger.log("unknow message")
            }
        }
    }
}