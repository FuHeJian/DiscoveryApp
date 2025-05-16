package com.fhj.discoveryapp.discovery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhj.discoveryapp.databinding.DiscoveryAdapterNormalItemBinding

object DiscoveryAdapterDiffCallback :
    DiffUtil.ItemCallback<DiscoveryAdapter.DiscoveryAdapterItem>() {
    override fun areItemsTheSame(
        oldItem: DiscoveryAdapter.DiscoveryAdapterItem,
        newItem: DiscoveryAdapter.DiscoveryAdapterItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DiscoveryAdapter.DiscoveryAdapterItem,
        newItem: DiscoveryAdapter.DiscoveryAdapterItem
    ): Boolean {
        return oldItem == newItem
    }
}

class DiscoveryAdapter :
    ListAdapter<DiscoveryAdapter.DiscoveryAdapterItem, DiscoveryAdapter.ViewHolder>(
        DiscoveryAdapterDiffCallback
    ) {

    class ViewHolder(val itemView: DiscoveryAdapterNormalItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

    }

    class DiscoveryAdapterItem(var ip: String = "", var state: String = "") {
        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DiscoveryAdapterNormalItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.itemView.ip.text = getItem(position)?.let { it.ip + " ---- " + it.state }
    }
}