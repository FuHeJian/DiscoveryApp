package com.fhj.discoveryapp.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.MessageStatus
import com.fhj.byteparse.flatbuffers.MessageType
import com.fhj.byteparse.flatbuffers.ext.compare
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.discoveryapp.ui.theme.AppColors
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.logger.Logger
import com.fhj.user.UserManager
import kotlinx.coroutines.flow.filter
import java.util.*

private data class UiMessage(
    val message: Message,
    val isMe: Boolean,
    val animId: String? = null // 用于动画唯一标识
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UiMessage) return false
        return other.message.compare(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview()
fun ChatComposeScreen(toUserKey: String = "") {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val messages = remember { mutableStateListOf<UiMessage>() }
    val me = remember { DnsHelper.me }
    val currentUser = UserManager.getUser(me)
    val toUser = UserManager.getUser(toUserKey)?.user
    var animatingMsgId by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var isTextFieldFocused by remember { mutableStateOf(false) }

    // 收集SharedFlow<Message>，只展示与toUserKey相关的消息
    LaunchedEffect(toUserKey) {
        DistributeHelper.userMessageOnReceive.filter {
            Logger.log("更新3333 ${it.fromUser().getKey() == toUserKey}")
            it.toUser() != null && it.fromUser().getKey() == toUserKey
        }.collect { msg ->
            val idx = messages.indexOfFirst { it.message.id() == msg.id() && it.isMe }
            Logger.log("更新0000 ${messages.find { msg.id() == it.message.id() }} ${idx}")
            if (idx != -1) {//接收我发送出去的消息
                messages[idx] = UiMessage(msg, messages[idx].isMe)
                Logger.log("更新1111 ${messages[idx]} ${messages[idx].message.status()}")
            } else if (messages.find { msg.id() == it.message.id() } == null) {//对方发送回来的新消息
                messages.add(UiMessage(msg, false))
                Logger.log("更新222 ${messages[idx]}")
            }
        }
    }

    // 当有新消息时自动滚动到顶部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // 当输入框获得焦点时滚动到顶部
    LaunchedEffect(isTextFieldFocused) {
        if (isTextFieldFocused && messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    val colors = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface.copy(alpha = 0.95f),
                    titleContentColor = colors.onSurface,
                    actionIconContentColor = colors.secondary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 头像占位符（可以替换为实际头像）
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                toUser?.name()?.firstOrNull()?.toString() ?: "U",
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                toUser?.name() ?: "用户",
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            )
                            Text(
                                "在线",
                                fontSize = 12.sp,
                                color = colors.secondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "通话"
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 底部输入栏 - 类似Tailwind风格
            Surface(
                color = colors.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加",
                            tint = colors.secondary
                        )
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("输入消息...") },
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .onFocusChanged { isTextFieldFocused = it.isFocused },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colors.background,
                            focusedContainerColor = colors.background,
                            unfocusedBorderColor = Color(AppColors.Outline),
                            focusedBorderColor = colors.primary,
                            unfocusedPlaceholderColor = colors.secondary,
                            focusedPlaceholderColor = colors.secondary
                        ),
                        maxLines = 3,

                        )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            if (input.text.isNotBlank() && currentUser != null && toUser != null) {
                                val msg = currentUser.sendText(input.text, toUser)
                                val animId = UUID.randomUUID().toString()
                                messages.add(UiMessage(msg, true, animId))
                                animatingMsgId = animId
                                input = TextFieldValue("")
                                // 发送后隐藏键盘
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (input.text.isNotBlank()) colors.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            if (input.text.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = "发送",
                            tint = if (input.text.isNotBlank()) Color.White else colors.secondary
                        )
                    }
                }
            }
        }
    ) { padding ->
        // 聊天内容区
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
                .clickable { keyboardController?.hide() }, // 点击空白区域隐藏键盘
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages.reversed()) { uiMsg ->
                val isAnimating = uiMsg.animId != null && uiMsg.animId == animatingMsgId
                ChatBubbleAnimated(
                    message = uiMsg.message,
                    isMe = uiMsg.isMe,
                    animate = isAnimating,
                    onAnimEnd = { if (isAnimating) animatingMsgId = null },
                    onRetry = {
                        // 重试发送消息
                        if (currentUser != null && toUser != null) {
                            val retryMsg =
                                currentUser.sendText(uiMsg.message.getMessageInfo(), toUser)
                            val animId = UUID.randomUUID().toString()
                            val v_s = UiMessage(retryMsg, true, animId)
                            if (messages.contains(v_s)) {
                                messages.remove(v_s)
                                messages.add(v_s)
                            } else {
                                messages.add(v_s)
                            }
                            animatingMsgId = animId
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubbleAnimated(
    message: Message,
    isMe: Boolean,
    animate: Boolean,
    onAnimEnd: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val scale = remember { Animatable(if (animate) 0.2f else 1f) }
    val offsetY = remember { Animatable(if (animate) 60f else 0f) }
    LaunchedEffect(animate) {
        if (animate) {
            scale.animateTo(1f, animationSpec = tween(400))
            offsetY.animateTo(0f, animationSpec = tween(400))
            onAnimEnd()
        }
    }
    ChatBubble(
        message = message,
        isMe = isMe,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = offsetY.value
            },
        onRetry = onRetry
    )
}

@Composable
fun ChatBubble(
    message: Message,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            // 其他用户头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.2f))
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "U",
                    fontSize = 12.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Logger.log("更新 ${message.status()}")
                when (message.status()) {
                    MessageStatus.SENDING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 6.dp),
                            color = colors.secondary,
                            strokeWidth = 2.dp
                        )
                    }

                    MessageStatus.FAILED -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    colors.error.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onRetry?.invoke()
                                }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "重试",
                                tint = colors.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    else -> {}
                }
                Box(
                    modifier = modifier
                        .background(
                            if (isMe) colors.primary else colors.surface,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = 260.dp)
                ) {
                    Text(
                        text = message.getMessageInfo(),
                        color = if (isMe) colors.onPrimary else colors.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 时间和状态信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    "09:30", // 这里可以替换为实际时间
                    fontSize = 12.sp,
                    color = colors.secondary
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (message.status() == 1) "已读" else "未读", // 根据消息状态显示
                        fontSize = 12.sp,
                        color = colors.secondary
                    )
                }
            }
        }
    }
} 