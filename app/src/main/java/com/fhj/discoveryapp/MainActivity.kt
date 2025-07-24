package com.fhj.discoveryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.fhj.base.view.activity.BaseActivity
import com.fhj.discoveryapp.databinding.MainActivityBinding
import com.fhj.discoveryapp.discovery.adapter.DiscoveryAdapterItem
import com.fhj.dns.DistributeHelper
import com.fhj.user.UserManager
import kotlinx.coroutines.launch

class MainActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MainActivityBinding.inflate(layoutInflater).root)
    }

}