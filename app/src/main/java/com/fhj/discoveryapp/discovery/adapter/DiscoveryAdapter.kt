package com.fhj.discoveryapp.discovery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageData
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.TextMessage
import com.fhj.byteparse.flatbuffers.User
import com.fhj.byteparse.flatbuffers.ext.compare
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.discoveryapp.databinding.DiscoveryAdapterNormalItemBinding
import com.fhj.user.UserData

data class DiscoveryAdapterItem(val currentMessage: Message?, val user: UserData) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoveryAdapterItem

        if (currentMessage?.compare(other.currentMessage) == false) return false
        if (user != other.user) return false

        return true
    }
}

class DiscoveryAdapter :
    RecyclerView.Adapter<DiscoveryAdapter.ViewHolder>() {

    val currentList = ArrayList<DiscoveryAdapterItem>()

    override fun getItemCount() = currentList.size

    class ViewHolder(val itemView: DiscoveryAdapterNormalItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

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
        holder.itemView.run {
            val item = currentList[position]
            userName.text = "${item.user.user.name()}(${item.user.user.ip()})"
            latestMessage.text = "${item.currentMessage?.getMessageInfo()}"
            status.text = when (item.currentMessage?.status()) {
                MessageStatus.SENDING -> {
                    "正在发送"
                }

                else -> ""
            }

            root.setOnClickListener {
                itemClickListener?.onItemClick(item)
            }
        }

    }

    fun addItem(item: DiscoveryAdapterItem) {
        if (currentList.contains(item)) {
            updateItem(item)
            return
        }
        currentList.add(item)
        notifyItemInserted(currentList.size)
    }

    fun removeItem(item: DiscoveryAdapterItem) {
        val index = currentList.indexOf(item)
        if (index != -1) {
            currentList.remove(item)
            notifyItemRemoved(index)
        }
    }

    fun updateItem(item: DiscoveryAdapterItem) {
        val index = currentList.indexOf(item)
        if (index != -1 && currentList[index] != item) {
            currentList[index] = item
            notifyItemChanged(index)
        }
    }

    var itemClickListener: ItemClickListener? = null
    fun setOnItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    interface ItemClickListener {
        fun onItemClick(item: DiscoveryAdapterItem)
    }
}