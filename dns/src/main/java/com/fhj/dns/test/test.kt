package com.fhj.dns.test

import com.fhj.dns.DnsHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, t ->
        print("error ${t}")
        DnsHelper.destroy()
    }) {
        exposure()
        discovery()
    }
}

suspend fun discovery() {
    DnsHelper.discovery()
}

suspend fun exposure() {
    DnsHelper.exposure()
}