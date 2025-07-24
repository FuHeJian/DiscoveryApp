package com.fhj.discoveryapp

import android.app.Dialog
import android.content.Context
import android.os.Bundle

class BaseDialog(c: Context): Dialog(c) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}