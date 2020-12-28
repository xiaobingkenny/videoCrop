package com.shijiusui.p.videocrop

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.shijiusui.p.videocrop.view.CropVideoSquareView

class TestUIActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_ui)

        val view = findViewById<CropVideoSquareView>(R.id.crop_video)
        view.onSizeChangedListener = object : CropVideoSquareView.OnSizeChangedListener{
            override fun onSizeChanged(view: CropVideoSquareView) {
                Log.w("zzh", "crop:${view.cropRectLeft} ${view.cropRectRight} ${view.cropRectTop} ${view.cropRectBottom}")
            }
        }

        val l = view.onSizeChangedListener

    }
}