package com.fhj.discoveryapp.discovery

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.fhj.base.view.fragment.BaseFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhj.discoveryapp.databinding.DiscoveryFragmentBinding
import com.fhj.discoveryapp.discovery.adapter.DiscoveryAdapter
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.dns.wifi.WifiDetect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiscoveryFragment : BaseFragment<DiscoveryFragmentBinding>() {

    var adapter = DiscoveryAdapter()

    override fun onBindingCreated() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.recyclerView.adapter = adapter

        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                DistributeHelper.allExposureAddressOnUpdate.collect {
                    adapter.submitList(it.map {
                        DiscoveryAdapter.DiscoveryAdapterItem(
                            it.hostAddress ?: ""
                        )
                    })
                }
            }
        }

        lifecycle.coroutineScope.launch(Dispatchers.IO) {

            WifiDetect.registerWifiDetect(requireContext())?.let {
                DnsHelper.setInterface(it)
            }

        }

    }


}