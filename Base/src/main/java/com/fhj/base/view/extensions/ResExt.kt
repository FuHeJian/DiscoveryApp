package com.fhj.base.view.extensions

import android.content.res.Resources

fun Int.dp(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

fun Int.px(): Int {
    return (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

//获取屏幕宽度
fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}
