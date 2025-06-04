package com.fhj.byteparse

enum class ByteType(val sep: ByteArray){
    /**
     * 使用eof分割
     */
    NORMAL(byteArrayOf(-1))
}

/**
 * @param length:字节长度，max:表示不限制长度，一直解析到eof为止
 * @param byteType 如果字节长度是紧凑型的，则这里的byteType就会生效，用来填充和解析该字段的结束符号
 */
@Target(AnnotationTarget.FIELD)
annotation class ByteParseProperty(val length:Int,val byteType: ByteType = ByteType.NORMAL)

/**
 * 不实用固定的大小，而是根据内容大小判断所占字节大小
 */
val LENGTH_COMPACT = -1

@Target(AnnotationTarget.CLASS)
annotation class ByteParseTarget()