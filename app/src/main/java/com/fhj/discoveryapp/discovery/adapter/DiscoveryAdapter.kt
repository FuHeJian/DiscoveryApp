package com.fhj.discoveryapp.discovery.adapter

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.ext.compare
import com.fhj.user.UserData

data class DiscoveryAdapterItem(val currentMessage: Message?, val user: UserData) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoveryAdapterItem
        if (currentMessage!=null && currentMessage.compare(other.currentMessage) == false) return false
        if (user != other.user) return false

        return true
    }

}