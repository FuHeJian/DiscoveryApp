package com.fhj.discoveryapp.discovery

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.fhj.base.view.fragment.BaseFragment
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.cs.NettyUtil
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.byteparse.flatbuffers.ext.isSystemMessageType
import com.fhj.discoveryapp.chat.ChatActivity
import com.fhj.discoveryapp.discovery.adapter.DiscoveryAdapterItem
import com.fhj.discoveryapp.ui.theme.AppColors
import com.fhj.discoveryapp.ui.theme.DiscoveryAppTheme
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.dns.wifi.WifiDetect
import com.fhj.logger.Logger
import com.fhj.user.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class DiscoveryFragment :
    BaseFragment<com.fhj.discoveryapp.databinding.DiscoveryFragmentBinding>() {

    override fun onBindingCreated() {
        // 设置Compose内容
        binding.compose.setContent {
            DiscoveryAppTheme {
                DiscoveryComposeScreen()
            }
        }

        // 初始化网络发现
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            WifiDetect.registerWifiDetect(requireContext())?.let {
                DnsHelper.setInterface(it)
            }
        }

        // 开始发现服务
        lifecycle.coroutineScope.launch {
            while (true) {
                DnsHelper.discovery()
                delay(1000)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun DiscoveryComposeScreen() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val discoveryItems = remember { mutableStateListOf<DiscoveryAdapterItem>() }

    // 收集消息并更新发现列表
    LaunchedEffect(Unit) {
        DistributeHelper.userMessageOnReceive.collect { message ->
            val user = UserManager.getUser(message.fromUser())
            if (user != null) {
                val item = DiscoveryAdapterItem(message, user)
                if (discoveryItems.contains(item)) {
                    val index = discoveryItems.indexOf(item)
                    discoveryItems[index] = item
                }
            }
        }
    }
    LaunchedEffect(Unit){
        DistributeHelper.systemMessageOnReceive.collect { message ->
            val user = UserManager.getUser(message.fromUser())
            if (user != null) {
                val item = DiscoveryAdapterItem(message, user)
                if (discoveryItems.contains(item)) {
                    if (item.currentMessage!!.type() == MessageType.CLOSE)
                        discoveryItems.remove(item)
                } else {
                    if (item.currentMessage!!.type() == MessageType.DISCOVERY)
                        discoveryItems.add(item)
                }
            }
        }
    }


    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface.copy(alpha = 0.95f),
                    titleContentColor = colors.onSurface,
                    actionIconContentColor = colors.secondary
                ),
                title = {
                    Text(
                        "发现",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: 刷新 */ }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    IconButton(onClick = { /* TODO: 设置 */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (discoveryItems.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "正在搜索附近的用户...",
                        color = colors.secondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            // 用户列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colors.background), // 添加背景色
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(discoveryItems) { item ->
                    DiscoveryItem(
                        item = item,
                        onClick = {
                            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                                putExtra("touser", item.user.user.getKey())
                            })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoveryItem(
    item: DiscoveryAdapterItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.user.user.device() ?: "U",
                    color = colors.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 用户信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.user.user.name()}",
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.currentMessage?.getMessageInfo() ?: "暂无消息",
                    color = colors.secondary,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            // 状态和时间
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 时间（这里可以添加实际时间）
                Text(
                    text = "刚刚",
                    color = colors.secondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 状态指示器
                when (item.currentMessage?.status()) {
                    MessageStatus.SENDING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "发送中",
                                color = colors.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    MessageStatus.SUCCESS -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "已发送",
                            modifier = Modifier.size(16.dp),
                            tint = colors.primary
                        )
                    }

                    else -> {
                        // 未读消息数量或其他状态
                        if (item.currentMessage != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}