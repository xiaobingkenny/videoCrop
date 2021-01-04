package com.shijiusui.p.videocrop

import android.app.Application
import android.widget.ImageView
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.loader.IBoxingCallback
import com.bilibili.boxing.loader.IBoxingMediaLoader
import com.bumptech.glide.Glide
import com.shijiusui.p.videocrop.utils.ContextProvider

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.init(this)
        BoxingMediaLoader.getInstance().init(object : IBoxingMediaLoader{
            override fun displayThumbnail(
                img: ImageView,
                absPath: String,
                width: Int,
                height: Int
            ) {

                GlideApp.with(img).load(absPath).into(img)
            }

            override fun displayRaw(
                img: ImageView,
                absPath: String,
                width: Int,
                height: Int,
                callback: IBoxingCallback?
            ) {
                GlideApp.with(img).load(absPath).into(img)
            }

        })
    }
}