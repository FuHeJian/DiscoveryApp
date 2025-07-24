package com.fhj.id

import com.fhj.byteparse.flatbuffers.User
import java.util.concurrent.atomic.AtomicLong

object MessageIdManager {

    private val cache = mutableMapOf<User, AtomicLong>()

    fun getNextId(user: User) = cache[user]?.addAndGet(1)?:run {
        cache.putIfAbsent(user, AtomicLong(1))!!.get()
    }

    fun getUserList() = cache.keys.toList()

}