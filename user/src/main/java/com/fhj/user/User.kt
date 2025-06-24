package com.fhj.user

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.User
import com.fhj.dns.DnsHelper

class Owner(val user: User) {

    fun chat(message: Message){
        DnsHelper.chat(message)
    }

}