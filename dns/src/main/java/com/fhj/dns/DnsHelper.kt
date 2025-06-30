package com.fhj.dns

import android.os.Build
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.byteparse.flatbuffers.cs.UdpSocketConfig
import com.fhj.byteparse.flatbuffers.ext.MessageMake
import com.fhj.byteparse.flatbuffers.ext.TextMessageMake
import com.fhj.byteparse.flatbuffers.ext.UserMake
import com.fhj.logger.Logger
import kotlinx.coroutines.Dispatchers
import java.net.Inet4Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
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
    val ADDRESS = Inet4Address.getByName(GROUP_IP)

    val GROUP_ADDRESS = ADDRESS

    val NETINTERFACE = NetworkInterface.getByName("wlan0")

    val TITLE_SIZE = maxOf(EXPOSURE_TITLE.length, DISCOVERY_TITLE.length, CLOSE_TITLE.length)

    var HEADER_SIZE = TITLE_SIZE + 4

    lateinit var socket: MulticastSocket
    var isInitSuccess = false

    lateinit var wifiAddress: InetAddress

    val currentUser: User by lazy {
        assert(::wifiAddress.isLateinit)
        UserMake(Build.DEVICE, "test", wifiAddress.toString())
    }

    val scope = Dispatchers.IO

    /**
     * 加入组播后，通过grpc通信，MulticastSocket设置了SocketOptions.SO_REUSEADDR选项，
     *
     * 支持多个socket监听同一个端口，所以可以再使用grpc来通信
     *
     */
    suspend fun setInterface(address: InetAddress) {
        try {
            if (!isMulticastSupported()) throw RuntimeException("不支持多播")
            wifiAddress = address

            HEADER_SIZE = TITLE_SIZE + (wifiAddress.address?.size ?: 0)
            /**
             * 设置本地端口，应为要同时监听远程发送过来的消息，所以端口要设置的一样
             *
             * 最好不要绑定具体ip，因为wifi有两个地址一个ipv4和ipv6,指定networkInterface即可
             */
            socket = MulticastSocket(PORT)
            socket.networkInterface = NETINTERFACE
            socket.joinGroup(GROUP_ADDRESS)
            NettyUtil.setUdpConfig(UdpSocketConfig(socket.channel,NETINTERFACE,GROUP_ADDRESS,address,PORT))
            isInitSuccess = true
            Logger.log("设置成功 ${NETINTERFACE}")
            if (!discovery()) Logger.log("开启失败 ${NETINTERFACE}")
        } catch (e: Exception) {
            Logger.log("设置失败 ${e}")
            isInitSuccess = false
        }
    }

    fun chat(message: Message) {
        //如果from是当前用户，则为发送事件，如果当前from为远程用户，则为接收事件
        val fromUser = message.fromUser()
        val toUser = message.toUser()
    }

    fun destroy() {
        socket.leaveGroup(GROUP_ADDRESS)
        socket.close()
    }

    private suspend fun discovery(): Boolean {
        if (!isInitSuccess) return false
        return
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