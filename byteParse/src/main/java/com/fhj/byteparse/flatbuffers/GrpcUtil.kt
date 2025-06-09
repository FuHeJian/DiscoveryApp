package com.fhj.byteparse.flatbuffers

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import com.fhj.logger.Logger
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

class ChatChannel(address: String, port: Int) {
    var channel: ManagedChannel = ManagedChannelBuilder.forAddress(address, port).build()
    var chatService: io.grpc.Server = ServerBuilder
        .forPort(port)
        .addService(object : ChatServiceGrpc.ChatServiceImplBase() {
            override fun chat(
                request: Message?,
                responseObserver: StreamObserver<Message?>?
            ) {
                Logger.log("onNext" + request?.toString())
                responseObserver?.onNext(request)
                responseObserver?.onCompleted()
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

    fun chat(message: Message) {
        chatServiceGrpc.chat(message, object : StreamObserver<Message> {
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