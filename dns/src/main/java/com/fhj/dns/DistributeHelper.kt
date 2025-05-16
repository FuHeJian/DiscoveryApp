package com.fhj.dns

import com.fhj.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.InetAddress

object DistributeHelper {

    val allDiscoveryAddress = mutableSetOf<InetAddress>()

    val allExposureAddressOnUpdate = MutableStateFlow<Set<InetAddress>>(allDiscoveryAddress)

    fun onReceiveData(message: Message) {
        Logger.log("接收成功 title:${message}")
        handMessage(message)
    }

    fun handMessage(message: Message) {
        when {
            message.title == DnsHelper.EXPOSURE_TITLE -> {
                //TODO 处理暴露消息
                allDiscoveryAddress.add(message.address)
                allExposureAddressOnUpdate.value = allDiscoveryAddress
            }

            message.title == DnsHelper.DISCOVERY_TITLE -> {
                //TODO 处理发现消息
            }

            message.title == DnsHelper.CLOSE_TITLE -> {
                //TODO 处理关闭消息
                allDiscoveryAddress.remove(message.address)
                allExposureAddressOnUpdate.value = allDiscoveryAddress
            }

            else -> {
                //TODO 处理未知消息
            }
        }
    }
}