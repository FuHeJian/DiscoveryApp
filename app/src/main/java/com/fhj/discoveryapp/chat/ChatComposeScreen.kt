package com.fhj.discoveryapp.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fhj.byteparse.flatbuffers.Message
import com.fhj.byteparse.flatbuffers.ext.getKey
import com.fhj.byteparse.flatbuffers.ext.getMessageInfo
import com.fhj.dns.DistributeHelper
import com.fhj.dns.DnsHelper
import com.fhj.user.UserManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.util.*

private data class UiMessage(
    val message: Message,
    val isMe: Boolean,
    val isLoading: Boolean,
    val animId: String? = null // 用于动画唯一标识
)

@Composable
fun ChatComposeScreen(toUserKey: String) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val messages = remember { mutableStateListOf<UiMessage>() }
    val colors = MaterialTheme.colors
    val me = remember { DnsHelper.me }
    val currentUser = UserManager.getUser(me)
    val toUser = UserManager.getUser(toUserKey)?.user
    var animatingMsgId by remember { mutableStateOf<String?>(null) }

    // 收集SharedFlow<Message>，只展示与toUserKey相关的消息
    LaunchedEffect(toUserKey) {
        DistributeHelper.messageOnReceive.filter {
            it.toUser() !=null && it.fromUser().getKey() == toUserKey
        }.collect { msg ->
            val idx = messages.indexOfFirst { it.isLoading && it.message.id() == msg.id() }
            if (idx != -1) {//接收我怕发送出去的消息
                messages[idx] = messages[idx].copy(isLoading = false)
            } else {//对方发送回来的新消息
                messages.add(UiMessage(msg, false, false))
            }
        }
    }

    Scaffold(
        backgroundColor = colors.background,
        topBar = {
            TopAppBar(
                backgroundColor = colors.surface,
                elevation = 0.dp,
                title = {
                    Text("Chat", color = colors.onPrimary, fontSize = 20.sp)
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = colors.secondary)
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                input = input,
                onInputChange = { input = it },
                onSend = {
                    if (input.text.isNotBlank() && currentUser != null && toUser != null) {
                        val msg = currentUser.sendText(input.text, toUser)
                        val animId = UUID.randomUUID().toString()
                        messages.add(UiMessage(msg, true, true, animId))
                        animatingMsgId = animId
                        input = TextFieldValue("")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 70.dp)
                .background(colors.background)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                items(messages.reversed()) { uiMsg ->
                    val isAnimating = uiMsg.animId != null && uiMsg.animId == animatingMsgId
                    ChatBubbleAnimated(
                        message = uiMsg.message,
                        isMe = uiMsg.isMe,
                        isLoading = uiMsg.isLoading,
                        animate = isAnimating,
                        onAnimEnd = { if (isAnimating) animatingMsgId = null }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubbleAnimated(
    message: Message,
    isMe: Boolean,
    isLoading: Boolean,
    animate: Boolean,
    onAnimEnd: () -> Unit
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
        isLoading = isLoading,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = offsetY.value
            }
    )
}

@Composable
fun ChatBubble(message: Message, isMe: Boolean, isLoading: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colors
    val bubbleColor = if (isMe) colors.primary else colors.surface
    val textColor = if (isMe) colors.onPrimary else colors.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp).padding(end = 6.dp),
                    color = colors.secondary,
                    strokeWidth = 2.dp
                )
            }
            Box(
                modifier = modifier
                    .background(bubbleColor, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.getMessageInfo(),
                    color = textColor,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
fun BottomBar(input: TextFieldValue, onInputChange: (TextFieldValue) -> Unit, onSend: () -> Unit) {
    val colors = MaterialTheme.colors
    Surface(
        color = colors.surface,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("输入消息...", color = colors.onSurface.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .weight(1f)
                    .background(colors.surface, shape = RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colors.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    textColor = colors.onSurface
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSend,
                backgroundColor = colors.primary,
                contentColor = colors.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
            }
        }
    }
} 