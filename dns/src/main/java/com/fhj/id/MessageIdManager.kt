package com.fhj.id

import com.fhj.byteparse.flatbuffers.User
import java.util.concurrent.atomic.AtomicLong

object MessageIdManager {

    private val cache = mutableMapOf<User, AtomicLong>()

    fun getNextId(toUser: User) = cache[toUser]?.addAndGet(1) ?: run {
        cache.getOrPut(toUser) {
            AtomicLong(1)
        }.get()
    }

    fun updateId(fromUser: User, id: Long) {
        val v_from_id = cache.getOrPut(fromUser) {
            AtomicLong(1)
        }
        if (v_from_id.get() == id) return
        val v_target_id = maxOf(v_from_id.get(), id)
        v_from_id.set(v_target_id)
    }

    fun getUserList() = cache.keys.toList()

}