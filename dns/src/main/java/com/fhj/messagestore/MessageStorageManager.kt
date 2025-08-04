package com.fhj.messagestore

import com.fhj.byteparse.flatbuffers.Message
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap

object MessageStorageManager {

    val messagesCache = ConcurrentHashMap<String, MessagesCacheManager>(10)

    //按照user key 来区分和别人的聊天记录

    /**
     * 和某个用户的聊天记录
     * @param key 对方的key
     */
    fun getUserMessages(key: String): Queue<Message> {
        return messagesCache.getOrPut(key) { MessagesCacheManager(key) }.getMessages()
    }

    fun saveMessage(key: String, msgs: Message) {
        messagesCache.getOrPut(key) { MessagesCacheManager(key) }.saveMessage(msgs)
    }

}