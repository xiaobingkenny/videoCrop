package com.shijiusui.p.videocrop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.model.config.BoxingConfig
import com.shijiusui.p.videocrop.test.TestPlayerActivity
import com.shijiusui.p.videocrop.test.TestUIActivity

class MainActivity : Activity() {

    companion object{
        const val REQ_CODE_VIDEO = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.crop).setOnClickListener {


            val config = BoxingConfig(BoxingConfig.Mode.VIDEO)
            //config.withVideoDurationRes(R.mipmap.ic_launcher)

            Boxing.of(config).withIntent(this, BoxingActivity::class.java).start(this, REQ_CODE_VIDEO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQ_CODE_VIDEO -> {
                val result = Boxing.getResult(data)
                val path = result?.get(0)?.path
                Log.w("zzh", "onActivityResult path: $path")

                path?.apply {
//                    val intent = Intent(this@MainActivity, TestPlayerActivity::class.java)
//                    intent.putExtra(TestPlayerActivity.KEY_PATH, path)
//                    startActivity(intent)

                    val intent = Intent(this@MainActivity, TestUIActivity::class.java)
                    intent.putExtra(TestUIActivity.KEY_PATH, path)
                    startActivity(intent)
                }


            }
        }
    }
}