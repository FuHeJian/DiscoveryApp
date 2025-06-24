package com.fhj.dns

import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.User
import com.fhj.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.InetAddress

object DistributeHelper {

    val allDiscoveryAddress = mutableSetOf<InetAddress>()

    val allExposureAddressOnUpdate = MutableStateFlow<Set<InetAddress>>(allDiscoveryAddress)

}