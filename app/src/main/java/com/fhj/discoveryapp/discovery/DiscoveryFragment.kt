package com.fhj.discoveryapp.discovery

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.fhj.base.view.fragment.BaseFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhj.base.view.extensions.dp
import com.fhj.base.view.extensions.getScreenWidth
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.discoveryapp.R
import com.fhj.discoveryapp.chat.ChatActivity
import com.fhj.discoveryapp.databinding.DiscoveryFragmentBinding
import com.fhj.discoveryapp.discovery.adapter.DiscoveryAdapter
import com.fhj.discoveryapp.discovery.adapter.DiscoveryAdapterItem
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.dns.wifi.WifiDetect
import com.fhj.user.UserManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiscoveryFragment : BaseFragment<DiscoveryFragmentBinding>() {

    var adapter = DiscoveryAdapter()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindingCreated() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.recyclerView.adapter = adapter
        //binding.recyclerView绘制分割线
        binding.recyclerView.addItemDecoration(MaterialDividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL).apply{
            dividerColor = requireContext().getColor(R.color.divider_line)
            dividerThickness = 1.dp()
            isLastItemDecorated = false
            this.dividerInsetStart = 15.dp()
            this.dividerInsetEnd = getScreenWidth()
        })
        //设置item的点击
        adapter.setOnItemClickListener(object : DiscoveryAdapter.ItemClickListener {
            override fun onItemClick(item: DiscoveryAdapterItem) {
                startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("touser",item.user.user.getKey())
                })
            }
        })

        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                DistributeHelper.messageOnReceive.collect {
                    adapter.addItem(DiscoveryAdapterItem(it, UserManager.getUser(it.fromUser())!!))
                }
            }
        }

        lifecycle.coroutineScope.launch(Dispatchers.IO) {

            WifiDetect.registerWifiDetect(requireContext())?.let {
                DnsHelper.setInterface(it)
            }

        }

        lifecycle.coroutineScope.launch {
            while (true){
                DnsHelper.discovery()
                delay(1000)
            }

        }

    }


}