package com.fhj.discoveryapp

import android.os.Bundle
import com.fhj.base.view.activity.BaseActivity
import com.fhj.discoveryapp.databinding.MainActivityBinding

class MainActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MainActivityBinding.inflate(layoutInflater).root)
    }

}