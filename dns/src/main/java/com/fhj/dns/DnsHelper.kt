package com.fhj.dns

import android.os.Build
import androidx.annotation.RequiresApi
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.byteparse.flatbuffers.cs.UdpSocketConfig
import com.fhj.byteparse.flatbuffers.ext.MessageMake
import com.fhj.byteparse.flatbuffers.ext.TextMessageMake
import com.fhj.byteparse.flatbuffers.ext.UserMake
import com.fhj.id.MessageIdManager
import com.fhj.logger.Logger
import kotlinx.coroutines.Dispatchers
import java.net.DatagramPacket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.channels.MulticastChannel
import javax.net.ServerSocketFactory


object DnsHelper {

    /**
     * 暴漏设备
     */
    val EXPOSURE_TITLE = "EXPOSURE"

    /**
     * 发现设备
     */
    val DISCOVERY_TITLE = "DISCOVERY"

    /**
     * 断开连接
     */
    val CLOSE_TITLE = "DISCOVERY"

    /**
     * 未知内容
     */
    val UNKNOW_TITLE = "UNKNOW"

    val EXPOSURE_HEADER = EXPOSURE_TITLE.toByteArray().let(::titleToByte)
    val DISCOVERY_HEADER = DISCOVERY_TITLE.toByteArray().let(::titleToByte)
    val CLOSE_HEADER = DISCOVERY_TITLE.toByteArray().let(::titleToByte)

    fun titleToByte(arr: ByteArray) = ByteArray(TITLE_SIZE) {
        return@ByteArray if (it < arr.size) arr[it] else -1
    }

    /**
     * 其他设备的端口
     */
    var PORT = 8888
    val GROUP_IP = "239.255.42.99"
    val ADDRESS = InetSocketAddress(GROUP_IP, PORT)

    val GROUP_ADDRESS = ADDRESS

    val NETINTERFACE = NetworkInterface.getByName("wlan0")

    val TITLE_SIZE = maxOf(EXPOSURE_TITLE.length, DISCOVERY_TITLE.length, CLOSE_TITLE.length)

    var HEADER_SIZE = TITLE_SIZE + 4
    var isInitSuccess = false

    lateinit var wifiAddress: InetAddress
    lateinit var me: User

    val scope = Dispatchers.IO

    /**
     * 加入组播后，MulticastSocket设置了SocketOptions.SO_REUSEADDR选项，
     *
     * 支持多个socket监听同一个端口
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun setInterface(address: InetAddress) {
//        try {
//            if (!isMulticastSupported()) throw RuntimeException("不支持多播")
//            wifiAddress = address
//
//            HEADER_SIZE = TITLE_SIZE + (wifiAddress.address?.size ?: 0)
//            NettyUtil.setUdpConfig(UdpSocketConfig(NETINTERFACE,GROUP_ADDRESS,address,PORT))
//            isInitSuccess = true
//            Logger.log("设置成功 ${NETINTERFACE}")
//            if (!discovery()) Logger.log("开启失败 ${NETINTERFACE}")
//        } catch (e: RuntimeException) {
//            Logger.log("设置失败 ${e}")
//            isInitSuccess = false
//        }

        if (!isMulticastSupported()) throw RuntimeException("不支持多播")
        wifiAddress = address

        HEADER_SIZE = TITLE_SIZE + (wifiAddress.address?.size ?: 0)
        NettyUtil.setUdpConfig(UdpSocketConfig(NETINTERFACE, GROUP_ADDRESS, byteToIp(wifiAddress.address), PORT))
        isInitSuccess = true
        me = UserMake(Build.MODEL, Build.FINGERPRINT,wifiAddress.toString(), byteToIp(wifiAddress.address))
        if (!discovery()) Logger.log("开启失败 ${NETINTERFACE}")
    }

    fun chat(message: Message) {
        //如果from是当前用户，则为发送事件，如果当前from为远程用户，则为接收事件
        val fromUser = message.fromUser()
        val toUser = message.toUser()
    }

    suspend fun discovery(): Boolean {
        if (!isInitSuccess) return false
        val da = wifiAddress
        /**
         * 设置发送数据和远程地址
         */
        NettyUtil.send(MessageMake(
            MessageType.DISCOVERY,
            -1,
            me,
            null,
            status = MessageStatus.SENDING,
            unionDataType = MessageData.NONE,
        ))
        return true
    }

    fun byteToIp(bytes: ByteArray): String {
        Logger.log("size=" + bytes.size)
        val ip = StringBuilder()
        for (i in bytes.indices) {
            ip.append(bytes[i].toInt() and 0xFF)
            if (i < bytes.size - 1) {
                ip.append(".")
            }
        }
        return ip.toString()
    }

    fun isMulticastSupported(): Boolean {
        return NETINTERFACE.supportsMulticast()
    }

}