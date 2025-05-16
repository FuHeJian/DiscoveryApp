package com.fhj.dns

import com.fhj.dns.DnsHelper.HEADER_SIZE
import java.net.DatagramPacket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

data class Message(var title: String, var address: InetAddress) {

    companion object {

        val charset = charset("UTF-8")
            .newDecoder()
            .onMalformedInput(CodingErrorAction.IGNORE)
            .onUnmappableCharacter(CodingErrorAction.IGNORE)

        fun parse(data: DatagramPacket): Message {
            if (checkHeader(data.data)) {
                val title = charset.decode(ByteBuffer.wrap(data.data.copyOfRange(0, HEADER_SIZE)))
                    .toString()
                val add = InetAddress.getByAddress(data.data.copyOfRange(10, 14))
                return Message(title, add)
            } else {
                throw IllegalArgumentException("Illegal Header")
            }
        }

        fun checkHeader(header: ByteArray?) = header != null && header.size == HEADER_SIZE
    }

}