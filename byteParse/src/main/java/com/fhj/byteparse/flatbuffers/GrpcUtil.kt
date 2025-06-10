package com.fhj.byteparse.flatbuffers

import android.os.MessageQueue
import com.fhj.byteparse.flatbuffers.ext.MessageMake
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import com.fhj.logger.Logger
import io.grpc.InsecureServerCredentials
import io.grpc.ServerBuilder
import io.grpc.okhttp.OkHttpServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.net.ServerSocketFactory
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume

class ChatChannel(address: String, port: Int, val discoveryMessage: Message,socketFactory: ServerSocketFactory) {

    var channel: ManagedChannel =
        ManagedChannelBuilder.forAddress(address, port).enableRetry().build()
    var currentStream: StreamObserver<Message>? = null
    val messageQueue = ConcurrentHashMap<String, (MessageState) -> Unit>()
    var chatService: io.grpc.Server = OkHttpServerBuilder
        .forPort(port, InsecureServerCredentials.create())
        .socketFactory(socketFactory)
        .addService(object : ChatServiceGrpc.ChatServiceImplBase() {
            override fun chat(
                request: Message?,
                responseObserver: StreamObserver<Message?>?
            ) {
                Logger.log("onNext" + request?.toString())
                responseObserver?.onNext(request)
            }
        })
        .build()
    var chatServiceGrpc: ChatServiceGrpc.ChatServiceStub = ChatServiceGrpc.newStub(channel)

    init {
        startListen()
    }

    /**
     * 关闭通信
     */
    fun close() {
        channel.shutdown()
        chatService.shutdown()
    }

    /**
     * 开始收发消息
     */
    fun startListen() {
        chatService.start()
    }

    /**
     * 返回消息是否发送成功
     */
    suspend fun chat(message: Message): Boolean {
        return coroutineScope {
            if (startObserverAndDiscovery())
                return@coroutineScope suspendCoroutineUninterceptedOrReturn<Boolean> { con ->
                    currentStream?.onNext(message)
                    this.launch {
                        con.resume(message.whenStateChange().state == MessageStateType.SUCCESS)
                    }
                }
            else return@coroutineScope false
        }
    }

    /**
     * 发现设备并开始通信
     */
    suspend fun startObserverAndDiscovery(): Boolean {
        //为null则重新通信
        return suspendCoroutineUninterceptedOrReturn<Boolean> { con ->
            if (currentStream == null)
                chatServiceGrpc.chat(discoveryMessage, object : StreamObserver<Message> {
                    override fun onNext(value: Message?) {
                        currentStream = this
                        value?.let {
                            messageQueue[it.getKey()]?.invoke(MessageState(MessageStateType.SUCCESS))
                        }
                        con.resume(true)
                    }

                    override fun onError(t: Throwable?) {
                        currentStream = null
                        con.resume(false)
                    }

                    override fun onCompleted() {
                        currentStream = null
                        con.resume(false)
                    }
                })
            else con.resume(true)
        }

    }

    suspend fun Message.whenStateChange(): MessageState {
        return suspendCoroutineUninterceptedOrReturn { con ->
            messageQueue.putIfAbsent(getKey()) {
                //收到消息
                con.resume(it)
                messageQueue.remove(getKey())
            }
        }
    }

    fun Message.getKey() = "${fromUser().ip()}_${id()}"
}

enum class MessageStateType {
    SENDING,
    SUCCESS,
    FAIL,
    CANCEL
}

data class MessageState(val state: MessageStateType, var progress: Int = 0)