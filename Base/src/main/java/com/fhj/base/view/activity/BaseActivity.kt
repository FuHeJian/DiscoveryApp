package com.fhj.base.view.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import com.fhj.logger.Logger

abstract class BaseActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

//        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        window.statusBarColor = Color.TRANSPARENT
//        WindowCompat.setDecorFitsSystemWindows(window, false) // 内容延伸到状态栏
//        super.onCreate(savedInstanceState)
//        binding = getViewBinding(layoutInflater, null)
//            ?: throw Exception("<ViewBinding>")
//        setContentView(
//            binding.root
//        )

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainview = (window.decorView.findViewById<View>(android.R.id.content) as? ViewGroup)?.children?.firstOrNull()
        if (mainview==null)return
        ViewCompat.setOnApplyWindowInsetsListener(mainview){a,insets->

            Logger.log("insets:$insets")

            mainview.setPadding(0,100,0,0)

            mainview.doOnLayout {
                enableEdgeToEdge()
                mainview.requestLayout()
            }

            WindowInsetsCompat.CONSUMED
        }
        mainview.requestApplyInsets()
        mainview.requestLayout()
    }
}