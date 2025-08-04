package com.fhj.byteparse.flatbuffers.cs

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.ext.MessageMake
import com.fhj.byteparse.flatbuffers.ext.copy
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.byteparse.flatbuffers.ext.isSystemMessageType
import com.fhj.byteparse.flatbuffers.ext.log
import com.fhj.logger.Logger
import com.squareup.kotlinpoet.UNIT
import io.ktor.client.plugins.logging.Logging
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.util.internal.PlatformDependent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

object NettyUtil {

    lateinit var client: Bootstrap
    lateinit var clientCh: Channel

    lateinit var server: Bootstrap
    lateinit var serverCh: Channel

    lateinit var groupAddress: InetSocketAddress

    val dispatchChannel = MutableSharedFlow<Message>()
    val IOSCOPE = CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { a, b ->
        Logger.log("fatal!! $a $b")
    })

    fun setUdpConfig(config: UdpSocketConfig) {
        //强制private static final int JAVA_VERSION  设置成java8
        PlatformDependent::class.java.getDeclaredField("JAVA_VERSION").apply {
            isAccessible = true
            set(null, 8)
        }
        groupAddress = config.groupAddress
        setClientConfig(config)
//        setServerConfig(config)
    }

    fun setClientConfig(config: UdpSocketConfig) {
        val bootStrap = Bootstrap()
        val loop = NioEventLoopGroup()

        bootStrap.group(loop).channel(NioDatagramChannel::class.java)
            .option(
                ChannelOption.SO_REUSEADDR, true
            ).handler(object :
                SimpleChannelInboundHandler<io.netty.channel.socket.DatagramPacket>() {
                override fun messageReceived(
                    ctx: ChannelHandlerContext?,
                    msg: io.netty.channel.socket.DatagramPacket?
                ) {
                    //过滤发送消息
                    msg?.also {
                        var m = Message.getRootAsMessage(ByteBuffer.wrap(it.content().array()))
                        if (m.fromUser().ip() != config.source) {
                            if (!m.isSystemMessageType()) {
                                m.log("接收")
                                if (m.status() == MessageStatus.SENDING) {//对方发送的
                                    m = m.copy(
                                        fromUser = m.toUser(),
                                        toUser = m.fromUser(),
                                        status = MessageStatus.SUCCESS,
                                    )
                                    m.log("发送")
                                    //在回给他
                                    send(m)
                                }
                            }
                            dispatchMessage(m)
                        }
                    }
                }

                override fun close(
                    ctx: ChannelHandlerContext?,
                    promise: ChannelPromise?
                ) {
                    super.close(ctx, promise)
                    Logger.log("client接受到消息 close")
                }
            })
        client = bootStrap
        clientCh = bootStrap.bind(config.port).sync().channel()
        (clientCh as? NioDatagramChannel)?.joinGroup(config.groupAddress, config.networkInterface)
            ?.sync()
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
                    Logger.log("server准备写入 write")
                }

            })
        server = bootStrap
        serverCh = bootStrap.bind(config.port).sync().channel()
        (serverCh as? NioDatagramChannel)?.joinGroup(config.groupAddress, config.networkInterface)
            ?.sync()
    }

    fun send(data: Message) {
        //超时发送
        if (data.isSystemMessageType()) {
            clientCh.writeAndFlush(
                io.netty.channel.socket.DatagramPacket(
                    Unpooled.wrappedBuffer(data.byteBuffer),
                    groupAddress
                )
            )
        } else {
            IOSCOPE.launch {
                flow<Unit> {
                    _sendImpl(data)
                }.timeout(3.seconds).retry(5) {
                    false
                }.catch {
                    //发送失败，直接分发失败消息
                    dispatchMessage(data.copy(status = MessageStatus.FAILED))
                }.collect()
            }
        }
    }

    private suspend fun _sendImpl(data: Message): Unit {
        suspendCoroutine { con ->
            //由于不使用DatagramPacket包装，会调用socket.write,但是udp是无连接，无法执行write，只能receive和send，所以发送其他形式数据对象会报错
            clientCh.writeAndFlush(
                io.netty.channel.socket.DatagramPacket(
                    Unpooled.wrappedBuffer(data.byteBuffer),
                    groupAddress
                )
            ).addListener {
                //发送完成
                con.resume(UNIT)
            }
        }
    }

    private fun _sendImpl() {

    }

    fun dispatchMessage(msg: Message) {
        IOSCOPE.launch {
            dispatchChannel.emit(msg)
        }
    }

}

data class UdpSocketConfig(
    val networkInterface: NetworkInterface,
    val groupAddress: InetSocketAddress,
    val source: String,
    val port: Int
)