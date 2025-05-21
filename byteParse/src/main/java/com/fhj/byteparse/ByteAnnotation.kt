package com.fhj.byteparse

/**
 * @param length:字节长度，max:表示不限制长度，一直解析到eof为止
 */
@Target(AnnotationTarget.FIELD)
annotation class ByteParseProperty(val length:Int)

@Target(AnnotationTarget.CLASS)
annotation class ByteParseTarget()