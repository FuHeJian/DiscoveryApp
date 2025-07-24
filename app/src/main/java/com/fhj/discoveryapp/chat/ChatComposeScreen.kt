package com.fhj.discoveryapp.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun ChatComposeScreen() {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val messages = remember {
        mutableStateListOf(
            Message("你好！", true),
            Message("你好，有什么可以帮你？", false),
            Message("请帮我推荐一首歌。", true),
            Message("推荐你听《Daydream》。", false)
        )
    }

    val colors = MaterialTheme.colors
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
                    if (input.text.isNotBlank()) {
                        messages.add(Message(input.text, true))
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
                items(messages.reversed()) { msg ->
                    ChatBubble(msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class Message(val text: String, val isMe: Boolean)

@Composable
fun ChatBubble(message: Message) {
    val colors = MaterialTheme.colors
    val bubbleColor = if (message.isMe) colors.primary else colors.surface
    val textColor = if (message.isMe) colors.onPrimary else colors.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.body1
            )
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
                Icon(Icons.Default.Send, contentDescription = "发送")
            }
        }
    }
} 