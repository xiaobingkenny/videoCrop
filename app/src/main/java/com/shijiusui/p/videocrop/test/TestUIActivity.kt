package com.shijiusui.p.videocrop.test

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import com.shijiusui.p.videocrop.R
import com.shijiusui.p.videocrop.api.*
import com.shijiusui.p.videocrop.api.impl.MediaFileImpl
import com.shijiusui.p.videocrop.api.impl.PlayerTXImpl
import com.shijiusui.p.videocrop.api.impl.VideoViewTXImpl
import com.shijiusui.p.videocrop.view.CropVideoSquareView
import com.shijiusui.p.videocrop.view.VideoTrimView
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.ui.TXCloudVideoView

class TestUIActivity : Activity() {
    companion object{
        const val KEY_PATH = "path"
    }
    var isStarted = false
    var isPlaying = false
    var pro = 0f
    var lastPro = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_ui)



        val path = intent.getStringExtra(KEY_PATH) ?: return

        val videoView : IVideoView = VideoViewTXImpl(findViewById<TXCloudVideoView>(R.id.video_view))
        val vodPlayer : IPlayer = PlayerTXImpl(this)
        vodPlayer.setPlayerView(videoView)
        vodPlayer.setRenderMode(RenderMode.ADJUST_RESOLUTION)
        vodPlayer.setRenderRotation(RenderRotation.PORTRAIT)
        vodPlayer.isLoop = true
        vodPlayer.setAutoPlay(false)

        var timeCount = 0
        vodPlayer.setOnPlayerProgressListener(object : OnPlayerProgressListener{
            override fun onProgress(player: IPlayer, progressMs: Int) {
                //if(timeCount++ % 10 == 0)
                Log.w("zzh", "onProgress: $progressMs")
            }
        })

//        vodPlayer.setVodListener(object : ITXVodPlayListener{
//            // 事件通知
//            override fun onPlayEvent(player: TXVodPlayer, event: Int, param: Bundle?) {
////                Log.w("zzh", "event: $event")
//                when(event) {
////                    TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED -> {
////                        // 播放器已准备完成，可以播放
////                        player.seek(10*1000)
////                    }
////                    // 播放事件--------->
////                    TXLiveConstants.PLAY_EVT_PLAY_BEGIN -> {
////                        // 用于 隐藏加载动画 等
////
////                    }
////                    TXLiveConstants.PLAY_EVT_PLAY_LOADING -> {
////                        // 开始加载
////                    }
////                    TXLiveConstants.PLAY_EVT_VOD_LOADING_END -> {
////                        // 隐藏加载
////                    }
//                    // 如果点播播放场景需要获取到毫秒级别的时间戳来加载字幕
//                    TXLiveConstants.PLAY_EVT_PLAY_PROGRESS -> {
//                        // 加载进度，单位毫秒
//                        val durationMs = param?.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS)
//
//                        // 播放进度，单位秒
//                        val processMS = param?.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS)
//                        processMS?.apply {
//                            pro = this/1000f
//                        }
//                        Log.w("zzh", "progress -> $processMS " + player.isPlaying)
//
////                        // 视频总长，单位秒
////                        val durationMs = param?.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS)
//                    }
//                }
//            }
//
//            override fun onNetStatus(player: TXVodPlayer, param: Bundle) {
//            }
//
//        })
//        vodPlayer.startPlay(path)

        val view = findViewById<CropVideoSquareView>(R.id.crop_video)
        view.onSizeChangedListener = object : CropVideoSquareView.OnSizeChangedListener{
            override fun onSizeChanged(view: CropVideoSquareView) {
                Log.w(
                    "zzh",
                    "crop:${view.cropRectLeft} ${view.cropRectRight} ${view.cropRectTop} ${view.cropRectBottom}"
                )
            }
        }

        val l = view.onSizeChangedListener

        val videoTrimView = findViewById<VideoTrimView>(R.id.video_trim)
        val plMediaFile : IMediaFile = MediaFileImpl(path)
        videoTrimView.initMediaInfo(plMediaFile)
        videoTrimView.onActionListener = object : VideoTrimView.OnActionListener {
            override fun play() {
                if(vodPlayer.isPlaying) {
                    vodPlayer.resume()
                }else{
                    vodPlayer.startPlay(path)
                }
            }

            override fun pause() {
                Log.w("zzh", "pause")
                vodPlayer.pause()
//                val needClearLastImg = false
//                vodPlayer.stopPlay(needClearLastImg)
            }

            override fun seek(time: Long) {
                Log.w("zzh", "seek:$time")
//                pro = time
                vodPlayer.seek(time/1000f)
//                videoTrimView.stop()
                //vodPlayer.pause()
            }
        }

        vodPlayer.setOnPlayerStatusListener(object : OnPlayerStatusListener{
            override fun onStatus(isPlaying: Boolean) {
                Log.w("zzh", "isPlaying....$isPlaying")
                if(vodPlayer.isManualPlaying){
                    videoTrimView.start(object : VideoTrimView.OnPlayProgressCallback{
                        override fun getPlayProgressMs(): Int {
                            return (vodPlayer.currentPosition * 1000).toInt()
                        }
                    })
                }else{
                    videoTrimView.stop()
                }
            }
        })

        val videoFrame = findViewById<View>(R.id.video_frame)
        videoFrame.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                videoFrame.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateCropLayoutParams(plMediaFile, videoFrame.measuredWidth, videoFrame.measuredHeight, videoView.view, view)
            }
        })

        val startBtn = findViewById<Button>(R.id.start)
        val stopBtn = findViewById<Button>(R.id.stop)
        startBtn.text = "开始"
        startBtn.setOnClickListener {
            if(isPlaying){
                isPlaying = false
                Log.w("zzh", "manualPause")
                vodPlayer.manualPause()
                startBtn.text = "继续"
            }else{
                if(isStarted) {
                    isPlaying = true
                    Log.w("zzh", "manualResume -> pro:$pro")
                    vodPlayer.manualResume()
                    startBtn.text = "暂停"
                }else{
                    isPlaying = true
                    isStarted = true
                    Log.w("zzh", "startPlay")
                    vodPlayer.setStartTime(0f)
                    vodPlayer.setAutoPlay(true)
                    vodPlayer.startPlay(path)
                    startBtn.text = "暂停"
                }
            }
        }

        stopBtn.setOnClickListener {
            if(lastPro == 0) {
                lastPro = (vodPlayer.currentPosition * 1000).toInt()

                vodPlayer.stopPlay(false)
                isStarted = false
                isPlaying = false
                startBtn.text = "开始"
                stopBtn.text = "恢复"
            }else{
                Log.w("zzh", "startPlay lastPro:$lastPro")
                vodPlayer.setStartTime(lastPro/1000f)
                vodPlayer.setAutoPlay(true)
                vodPlayer.startPlay(path)
                stopBtn.text = "停止"
                isStarted = true
                isPlaying = true
                startBtn.text = "暂停"
                lastPro = 0
            }
        }

        val seekBtn = findViewById<Button>(R.id.seek)
        seekBtn.setOnClickListener {
            pro += 1
            Log.w("zzh", "seek -> pro:$pro")
            vodPlayer.seek(pro)
        }
    }

    private fun updateCropLayoutParams(plMediaFile : IMediaFile, previewFrameWidth: Int, previewFrameHeight: Int, videoShowView : View, cropVideoView : View) {
        val previewLayoutRatio = previewFrameWidth.toFloat() / previewFrameHeight
        val videoWidth: Int
        val videoHeight: Int
        val videoRotation: Int = plMediaFile.videoRotation // 旋转角度
        Log.w("zzh", "videoRotation:$videoRotation")
        if (videoRotation == 0 || videoRotation == 180) {
            videoWidth = plMediaFile.videoWidth
            videoHeight = plMediaFile.videoHeight
        } else {
            videoWidth = plMediaFile.videoHeight
            videoHeight = plMediaFile.videoWidth
        }
        val videoRatio = videoWidth.toFloat() / videoHeight
        val mRealVideoWidth: Int
        val mRealVideoHeight: Int
        if (previewLayoutRatio < videoRatio) {
            mRealVideoWidth = previewFrameWidth
            mRealVideoHeight = previewFrameWidth * videoHeight / videoWidth
        } else {
            mRealVideoWidth = previewFrameHeight * videoWidth / videoHeight
            mRealVideoHeight = previewFrameHeight
        }
        val lp = FrameLayout.LayoutParams(mRealVideoWidth, mRealVideoHeight)
        lp.gravity = Gravity.CENTER
        videoShowView.layoutParams = lp
        cropVideoView.layoutParams = lp
    }
}