package com.fhj.dns

import android.content.Context
import com.fhj.logger.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.DatagramPacket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import kotlin.coroutines.resume
import kotlin.math.max


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

    fun setInterface(address: InetAddress) {
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
            isInitSuccess = true
            Logger.log("设置成功 ${NETINTERFACE}")
        } catch (e: Exception) {
            Logger.log("设置失败 ${e}")
            isInitSuccess = false
        }
    }


    fun destroy() {
        socket.leaveGroup(GROUP_ADDRESS)
        socket.close()
    }

    suspend fun discovery() {
        if (!isInitSuccess) return
        val packet = DatagramPacket(ByteArray(HEADER_SIZE), HEADER_SIZE)
        suspendCancellableCoroutine {
            // 阻塞接收数据
            it.resume(socket.receive(packet))
        }
        val msg = Message.parse(packet)

        val local = wifiAddress.address
        if (!msg.address.address.contentEquals(local)) DistributeHelper.onReceiveData(msg)
    }

    suspend fun exposure() {
        if (!isInitSuccess) return
        val da = wifiAddress.address
        val data = EXPOSURE_HEADER.plus(da)
        Logger.log("发送成功 local ${byteToIp(da.take(4).toByteArray())}")
        /**
         * 设置发送数据和远程地址
         */
        val edp = DatagramPacket(data, data.size, ADDRESS, PORT)
        socket.send(edp)
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