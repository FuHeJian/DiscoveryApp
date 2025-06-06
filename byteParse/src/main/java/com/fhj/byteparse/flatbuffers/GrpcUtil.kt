package com.fhj.byteparse.flatbuffers

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import com.fhj.logger.Logger
import java.security.acl.Owner

class ChatChannel(address: String, port: Int,val owner: User) {
    var channel: ManagedChannel = ManagedChannelBuilder.forAddress(address, port).build()
    var chatServiceGrpc: ChatServiceGrpc.ChatServiceStub = ChatServiceGrpc.newStub(channel)

    init {
        startListen()
    }

    fun close() {
        channel.shutdown()
    }

    /**
     * 开始收发消息
     */
    fun startListen() {

    }

    fun chat(message: Message) {
        chatServiceGrpc.chat(message, object : io.grpc.stub.StreamObserver<Message> {
            override fun onNext(value: Message?) {
                Logger.log("onNext" + value?.toString())
            }

            override fun onError(t: Throwable?) {
                Logger.log("onNext$t")
            }

            override fun onCompleted() {
                Logger.log("onCompleted$message")
            }
        })
    }

}