package com.fhj.discoveryapp.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fhj.discoveryapp.ui.theme.DiscoveryAppTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toUserKey = intent.getStringExtra("touser") ?: ""
        setContent {
            DiscoveryAppTheme {
                ChatComposeScreen(toUserKey = toUserKey)
            }
        }
    }
}