package com.shijiusui.p.videocrop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.crop).setOnClickListener {
            val intent = Intent(this, VideoCropActivity::class.java)
            startActivity(intent)
        }
    }
}