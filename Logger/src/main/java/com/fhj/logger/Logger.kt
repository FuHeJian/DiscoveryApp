package com.fhj.logger

import android.os.Build
import java.lang.System

enum class PlatForm {
    ANDROID,
    PC,
    UNKNOW
}

interface LoggerDelegate {
    fun log(tag: String, msg: String)
}

object Logger {
    private var currentPlatform = getPlatForm()

    private val logger = when (currentPlatform) {
        PlatForm.ANDROID -> {
            AndroidLogger
        }

        PlatForm.PC -> {
            PCLogger
        }

        PlatForm.UNKNOW -> {
            PCLogger
        }
    }

    private val TAG = logger::class.simpleName ?: "UNKNOW"

    private fun getPlatForm(): PlatForm {
        val pn = System.getProperty("os.name")
        val pcR = Regex(".*(Windows|Mac).*")
        return when {
            pn.contains(pcR) -> {
                PlatForm.PC
            }

            pn.contains("Linux") -> {
                PlatForm.ANDROID
            }

            else -> {
                PlatForm.UNKNOW
            }
        }
    }

    fun log(msg: Any?) {
        var prefix = ""
        if (logger is AndroidLogger && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            prefix = stackWalker.callerClass.name
        }
        logger.log(TAG, "from $prefix\n${msg?.toString() ?: "NULL MESSAGE"}")
    }


}