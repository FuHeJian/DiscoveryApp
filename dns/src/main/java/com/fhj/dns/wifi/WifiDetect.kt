package com.fhj.dns.wifi

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.core.content.PermissionChecker
import com.fhj.logger.Logger
import kotlinx.coroutines.job
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.Inet4Address
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

object WifiDetect {
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Throws(IllegalArgumentException::class)
    suspend fun registerWifiDetect(context: Context): Inet4Address? {
        val re =
            PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) == PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_WIFI_STATE
            ) == PermissionChecker.PERMISSION_GRANTED
        if (re)
            return suspendCancellableCoroutine<Inet4Address?> { con ->
                WifiUtil.INSTANCE.currentWifiName(context) {
                    if (it == null) {
//                        con.resume(null)
//                        return@currentWifiName
                    }
                    if (it != null) {
                        var ipAddress: Inet4Address? = null
                        for (address in it) {
                            if (address.address is Inet4Address) {
                                ipAddress = address.address as Inet4Address
                                break
                            }
                        }
                        con.resume(ipAddress)
                    }
                }
            }
        else {//没有权限
            throw IllegalArgumentException("没有权限")
        }

    }

}