package com.fhj.dns

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.User
import com.fhj.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.InetAddress

object DistributeHelper {

    val allDiscoveryAddress = mutableSetOf<InetAddress>()

    val allExposureAddressOnUpdate = MutableStateFlow<Set<InetAddress>>(allDiscoveryAddress)

    val allMessages = MutableSharedFlow<Message>()

    fun onReceiveData(message: Message) {
        Logger.log("接收成功 title:${message}")
        handMessage(message)
    }

    fun handMessage(message: Message) {
        allMessages.emit(message)
        when {
//            message.title == DnsHelper.EXPOSURE_TITLE -> {
//                //TODO 处理暴露消息
//                allDiscoveryAddress.add(message.address)
//                allExposureAddressOnUpdate.value = allDiscoveryAddress
//            }
//
//            message.title == DnsHelper.DISCOVERY_TITLE -> {
//                //TODO 处理发现消息
//            }
//
//            message.title == DnsHelper.CLOSE_TITLE -> {
//                //TODO 处理关闭消息
//                allDiscoveryAddress.remove(message.address)
//                allExposureAddressOnUpdate.value = allDiscoveryAddress
//            }

            else -> {
                //TODO 处理未知消息
            }
        }
    }

    /**
     * 只收取指定User对象的消息
     */
    fun registerWithUser(user: User){

    }
}