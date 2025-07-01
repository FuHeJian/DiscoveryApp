package com.fhj.byteparse.flatbuffers.cs

import android.net.Network
import com.fhj.logger.Logger
import io.ktor.network.sockets.Socket
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.util.internal.PlatformDependent
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.ProtocolFamily
import java.net.SocketOptions
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.MulticastChannel
import java.nio.channels.Pipe
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelector
import java.nio.channels.spi.SelectorProvider

object NettyUtil {

    lateinit var client: Bootstrap
    lateinit var clientCh: Channel

    lateinit var server: Bootstrap
    lateinit var serverCh: Channel

    fun setUdpConfig(config: UdpSocketConfig) {
        //强制private static final int JAVA_VERSION  设置成java8
        PlatformDependent::class.java.getDeclaredField("JAVA_VERSION").apply {
            isAccessible = true
            set(null, 8)
        }
        setClientConfig(config)
        setServerConfig(config)
    }

    fun setClientConfig(config: UdpSocketConfig) {
        val bootStrap = Bootstrap()
        val loop = NioEventLoopGroup()

        bootStrap.group(loop).channel(NioDatagramChannel::class.java)
            .option(
                ChannelOption.SO_REUSEADDR, true
            ).handler(object :  SimpleChannelInboundHandler<Object>() {
                override fun messageReceived(
                    ctx: ChannelHandlerContext?,
                    msg: Object?
                ) {
                    Logger.log("client接受到消息$msg")
                }

                override fun close(
                    ctx: ChannelHandlerContext?,
                    promise: ChannelPromise?
                ) {
                    super.close(ctx, promise)
                }
            })
        client = bootStrap
        clientCh = bootStrap.bind(config.port).sync().channel()
        (clientCh as? NioDatagramChannel)?.joinGroup(config.groupAddress,config.networkInterface)?.sync()
    }

    fun setServerConfig(config: UdpSocketConfig) {
        val bootStrap = Bootstrap()
        val loop = NioEventLoopGroup()
// 手动发送多播加入请求（非标准 API）
        bootStrap.group(loop).channel(NioDatagramChannel::class.java)
            .option(
                ChannelOption.SO_REUSEADDR, true
            ).handler(object : SimpleChannelInboundHandler<Object>() {
                override fun messageReceived(
                    ctx: ChannelHandlerContext?,
                    msg: Object?
                ) {
                    Logger.log("server接受到消息$msg")
                }

                override fun close(
                    ctx: ChannelHandlerContext?,
                    promise: ChannelPromise?
                ) {
                    super.close(ctx, promise)
                    Logger.log("server接受到消息 close")
                }

                override fun write(
                    ctx: ChannelHandlerContext?,
                    msg: Any?,
                    promise: ChannelPromise?
                ) {
                    super.write(ctx, msg, promise)
                }
            })
        server = bootStrap
        serverCh = bootStrap.bind(config.port).sync().channel()
        (serverCh as? NioDatagramChannel)?.joinGroup(config.groupAddress,config.networkInterface)?.sync()
    }

    fun send(data: ByteArray) {
        clientCh.writeAndFlush(Unpooled.wrappedBuffer(data)).sync()
    }

    fun byteBufferToByteBuf(){

    }

}

data class UdpSocketConfig(
    val networkInterface: NetworkInterface,
    val groupAddress: InetSocketAddress,
    val source: InetAddress,
    val port: Int
)