package com.fhj.dns

import kotlin.reflect.KClass

enum class MessageType {

    EXPOSURE(Message::class);
//    DISCOVERY(Message::class),
//    IMAGE(ImageMessage::class),
//    VIDEO(VideoMessage::class),
//    TEXT(TextMessage::class),
//    AUDIO(AudioMessage::class),
//    FILE(FileMessage::class),
//    /**
//     * 多种type一起发送
//     */
//    MULTI(FileMessage::class),
//    CLOSE(Message::class);

    var messageData: KClass<out Message>

    constructor(type: KClass<out Message>) {
        messageData = type
    }

}