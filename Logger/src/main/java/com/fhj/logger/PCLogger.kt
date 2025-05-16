package com.fhj.logger

object PCLogger:LoggerDelegate {
    override fun log(tag: String, msg: String) {
        println("[$tag] $msg")
    }
}