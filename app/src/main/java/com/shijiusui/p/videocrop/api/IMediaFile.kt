package com.shijiusui.p.videocrop.api

import com.qiniu.pili.droid.shortvideo.PLVideoFrame

interface IMediaFile {
    fun getVideoFrameCount(keyFrame: Boolean): Int
    fun getVideoFrameByTime(timeMs: Long, keyFrame: Boolean): PLVideoFrame?

    val videoHeight: Int
    val videoWidth: Int
    val videoRotation: Int
    val durationMs: Long
}