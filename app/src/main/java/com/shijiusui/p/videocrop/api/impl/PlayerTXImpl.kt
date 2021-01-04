package com.shijiusui.p.videocrop.api.impl

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.shijiusui.p.videocrop.api.*
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayer

class PlayerTXImpl(context : Context) : IPlayer {
    private var isManual = false
    private var isStatusPlaying = false

    override val isManualPlaying: Boolean
        get() = isManual

    private val vodPlayer : TXVodPlayer = TXVodPlayer(context)

    private fun updateStatus(isPlaying : Boolean){
        if(isStatusPlaying == isPlaying) return
        isStatusPlaying = isPlaying
        onPlayerStatusListener?.onStatus(isStatusPlaying)
    }

    init {
        vodPlayer.setVodListener(object : ITXVodPlayListener{
            override fun onPlayEvent(player: TXVodPlayer, event: Int, param: Bundle) {
                if(player.isPlaying != isStatusPlaying){
                    Log.w("zzh", "$event player.isPlaying:" + player.isPlaying + " isStatusPlaying:" + isStatusPlaying)
                }

                when(event){
                    TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED, TXLiveConstants.PLAY_EVT_PLAY_BEGIN -> {
                        // ing
                        updateStatus(true)
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_END -> {
                        // end
                        updateStatus(false)
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_LOADING -> {
                        // loading
                    }
                    TXLiveConstants.PLAY_EVT_VOD_LOADING_END -> {
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_PROGRESS -> {
                        param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS).apply {
                            Log.w("zzh", "isPlaying:" + player.isPlaying + " statusPlaying:" + isStatusPlaying)
                            onPlayerProgressListener?.onProgress(this@PlayerTXImpl, this)
                        }
                    }
                    else ->{
                        Log.w("zzh", "event: $event")
                    }
                }

                if(event < 0){
                    stopPlay(true)
                    // pause
                    updateStatus(false)
                }
            }

            override fun onNetStatus(player: TXVodPlayer, status: Bundle) {

            }

        })
    }

    private var onPlayerProgressListener : OnPlayerProgressListener? = null
    private var onPlayerStatusListener : OnPlayerStatusListener? = null


    override val currentPosition: Float
        get() = vodPlayer.currentPlaybackTime

    override val isPlaying: Boolean
        get() = vodPlayer.isPlaying

    override var isLoop: Boolean
        get() = vodPlayer.isLoop
        set(value) {
            vodPlayer.isLoop = value
        }

    override fun setPlayerView(videoView: IVideoView) {
        if(videoView is VideoViewTXImpl){
            vodPlayer.setPlayerView(videoView.view)
        }else{
            throw IllegalArgumentException("need VideoViewTXImpl instance")
        }
    }

    override fun setRenderMode(renderMode: RenderMode) {
        when(renderMode){
            RenderMode.ADJUST_RESOLUTION -> vodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION)
        }
    }

    override fun setRenderRotation(renderRotation: RenderRotation) {
        when(renderRotation){
            RenderRotation.PORTRAIT -> vodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)
        }
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        vodPlayer.setAutoPlay(autoPlay)
    }

    override fun setStartTime(startTime: Float) {
        vodPlayer.setStartTime(startTime)
    }

    override fun startPlay(playUrl: String) {
        val ret = vodPlayer.startPlay(playUrl)
        if(ret == 0){
            isManual = true
            updateStatus(true)
        }
    }

    override fun stopPlay(isNeedClearLastImg: Boolean) {
        vodPlayer.stopPlay(isNeedClearLastImg)
        isManual = false
        updateStatus(false)
    }

    override fun pause() {
        vodPlayer.pause()
        updateStatus(false)
    }

    override fun resume() {
        vodPlayer.resume()
        updateStatus(true)
    }


    override fun seek(time: Float) {
        vodPlayer.seek(time)
    }

    override fun manualPause() {
        vodPlayer.pause()
        isManual = false
        updateStatus(false)
    }

    override fun manualResume() {
        vodPlayer.resume()
        isManual = true
        updateStatus(true)
    }

    override fun setOnPlayerProgressListener(listener: OnPlayerProgressListener) {
        this.onPlayerProgressListener = listener
    }

    override fun setOnPlayerStatusListener(listener: OnPlayerStatusListener) {
        this.onPlayerStatusListener = listener
    }


}