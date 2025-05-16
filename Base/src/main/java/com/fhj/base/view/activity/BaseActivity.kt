package com.fhj.base.view.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.fhj.base.view.extensions.getViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false) // 内容延伸到状态栏
        super.onCreate(savedInstanceState)
        binding = getViewBinding(layoutInflater, null)
            ?: throw Exception("<ViewBinding>")
        setContentView(
            binding.root
        )


    }
}