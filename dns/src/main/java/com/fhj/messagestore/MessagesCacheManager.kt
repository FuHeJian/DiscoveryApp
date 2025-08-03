package com.fhj.messagestore

import com.fhj.byteparse.flatbuffers.Message
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class MessagesCacheManager(key: String) {
    val messages = ConcurrentLinkedQueue<Message>()

    fun getMessages(): Queue<Message> {
        return messages
    }

    fun saveMessage(msgs:List<Message>){
        messages.addAll(msgs)
    }

    suspend fun loadMore(): List<Message> {

        return emptyList()
    }
}