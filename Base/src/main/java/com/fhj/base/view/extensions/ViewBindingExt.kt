package com.fhj.base.view.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fhj.base.view.activity.BaseActivity
import com.fhj.base.view.fragment.BaseFragment
import java.lang.reflect.ParameterizedType

fun <T : ViewBinding> BaseFragment<T>.getViewBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
): T? {
    val type = this.javaClass.genericSuperclass
    try {
        if (type is ParameterizedType) {
            val c = type.actualTypeArguments[0] as Class<T>
            return c.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            ).invoke(null, inflater, container, false) as? T
        }
    } catch (e: Exception) {
        throw e
    }

    return null
}

fun <T : ViewBinding> BaseActivity<T>.getViewBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
): T? {
    val type = this.javaClass.genericSuperclass
    try {
        if (type is ParameterizedType) {
            val c = type.actualTypeArguments[0] as Class<T>
            return c.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            ).invoke(null, inflater, container, false) as? T
        }
    } catch (e: Exception) {
        throw e
    }

    return null
}