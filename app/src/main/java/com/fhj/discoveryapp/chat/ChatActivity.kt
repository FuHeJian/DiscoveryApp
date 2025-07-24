package com.fhj.discoveryapp.chat

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.fhj.base.view.activity.BaseActivity
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.discoveryapp.databinding.ChatFragmentBinding
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.user.UserManager
import kotlinx.coroutines.launch

class ChatActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userkey = intent.getStringExtra("touser")?:""
        val binding = ChatFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                DistributeHelper.messageOnReceive.collect {
                    binding.chatText.text = it.getMessageInfo()
                }
            }
        }
        binding.sendButton.setOnClickListener {
            binding.textInputLayout.editText?.text?.let {
                UserManager.getUser(DnsHelper.me)?.sendText(it.toString(), UserManager.getUser(userkey)?.user!!)
            }
        }
    }
}