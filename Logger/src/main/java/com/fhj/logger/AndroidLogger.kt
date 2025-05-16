package com.fhj.logger

import android.util.Log

object AndroidLogger:LoggerDelegate {
    override fun log(tag: String, msg: String) {
        Log.d(tag, msg)
    }
}