package com.fhj.byteparse.flatbuffers.cs

import android.net.Network
import io.ktor.network.sockets.Socket
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ProtocolFamily
import java.nio.channels.DatagramChannel
import java.nio.channels.Pipe
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelector
import java.nio.channels.spi.SelectorProvider

object NettyUtil {

    lateinit var client:NioDatagramChannel

    fun setUdpConfig(config:UdpSocketConfig){
        val bootStrap = Bootstrap()
        val loop = NioEventLoopGroup()
        bootStrap.group(loop).channel(NioDatagramChannel::class.java).option(ChannelOption.SO_BROADCAST, true).handler(object:
            ChannelHandlerAdapter(){

            })
        val ch = bootStrap.bind(config.port).sync().channel()

    }

}

data class UdpSocketConfig(val channel: DatagramChannel,val networkInterface: NetworkInterface, val groupAddress: InetAddress, val source:InetAddress,val port:Int)