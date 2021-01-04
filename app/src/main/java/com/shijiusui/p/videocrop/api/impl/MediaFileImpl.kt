package com.shijiusui.p.videocrop.api.impl

import com.qiniu.pili.droid.shortvideo.PLMediaFile
import com.qiniu.pili.droid.shortvideo.PLVideoFrame
import com.shijiusui.p.videocrop.api.IMediaFile

class MediaFileImpl(path : String) : IMediaFile {
    val pl : PLMediaFile = PLMediaFile(path)

    override fun getVideoFrameCount(keyFrame: Boolean): Int {
        return pl.getVideoFrameCount(keyFrame)
    }

    override fun getVideoFrameByTime(timeMs: Long, keyFrame: Boolean): PLVideoFrame? {
        return pl.getVideoFrameByTime(timeMs, keyFrame)
    }

    override val videoHeight: Int
        get() = pl.videoHeight

    override val videoWidth: Int
        get() = pl.videoWidth

    override val videoRotation: Int
        get() = pl.videoRotation

    override val durationMs: Long
        get() = pl.durationMs

}