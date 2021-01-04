package com.shijiusui.p.videocrop.api

interface IPlayer {
    /**
     * 获取当前播放位置
     */
    val currentPosition: Float
    val isManualPlaying : Boolean
    val isPlaying: Boolean
    var isLoop: Boolean

    fun setPlayerView(videoView: IVideoView)

    /**
     * 设置图像平铺模式.
     */
    fun setRenderMode(renderMode: RenderMode)

    /**
     * 设置图像渲染角度.
     */
    fun setRenderRotation(renderRotation: RenderRotation)

    /**
     * 设置点播是否startPlay后自动开始播放。默认自动播放
     */
    fun setAutoPlay(autoPlay: Boolean)

    fun setStartTime(startTime: Float)


    fun startPlay(playUrl: String)

    fun stopPlay(isNeedClearLastImg: Boolean)

    fun manualPause()

    fun manualResume()

    fun pause()

    fun resume()

    /**
     * 跳转到视频流指定时间点.
     * @param time - 视频流时间点,小数点后为毫秒
     */
    fun seek(time: Float)

    fun setOnPlayerProgressListener(listener: OnPlayerProgressListener)
    fun setOnPlayerStatusListener(listener: OnPlayerStatusListener)

}