package com.shijiusui.p.videocrop.test

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.shijiusui.p.videocrop.R
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.downloader.ITXVodDownloadListener
import com.tencent.rtmp.downloader.TXVodDownloadManager
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo
import com.tencent.rtmp.ui.TXCloudVideoView

class TestPlayerActivity : Activity() {
    companion object{
        const val KEY_PATH = "path"
    }
    private lateinit var videoView: TXCloudVideoView

    private lateinit var vodPlayer : TXVodPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_player)
        videoView = findViewById(R.id.video_view)

        vodPlayer = TXVodPlayer(this)
        vodPlayer.setPlayerView(videoView)

        vodPlayer.setVodListener(object : ITXVodPlayListener {
            // 事件通知
            override fun onPlayEvent(player: TXVodPlayer, event: Int, param: Bundle?) {
                when(event){
                    // 播放事件--------->
                    TXLiveConstants.PLAY_EVT_PLAY_BEGIN ->{
                        // 用于 隐藏加载动画 等
                    }
                    TXLiveConstants.PLAY_EVT_PLAY_LOADING -> {
                        // 开始加载
                    }
                    TXLiveConstants.PLAY_EVT_VOD_LOADING_END -> {
                        // 隐藏加载
                    }
                    // 如果点播播放场景需要获取到毫秒级别的时间戳来加载字幕
                    TXLiveConstants.PLAY_EVT_PLAY_PROGRESS -> {
                        // 加载进度，单位毫秒
                        val durationMs = param?.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS)

                        // 播放进度，单位秒
                        val processMS = param?.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS)

//                        // 视频总长，单位秒
//                        val durationMs = param?.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS)
                    }

                    // 结束事件--------->
                    TXLiveConstants.PLAY_EVT_PLAY_END -> {
                        // 视频播放结束
                    }
                    TXLiveConstants.PLAY_ERR_NET_DISCONNECT -> {
                        // 网络断连,且经多次重连亦不能恢复,更多重试请自行重启播放
                    }
                    TXLiveConstants.PLAY_ERR_HLS_KEY -> {
                        // HLS 解密 key 获取失败
                    }

                    // 警告事件--------->
                    // 这些事件您可以不用关心，它只是用来告知您 SDK 内部的一些事件
                    TXLiveConstants.PLAY_WARNING_VIDEO_DECODE_FAIL -> {
                        // 当前视频帧解码失败
                    }

                    TXLiveConstants.PLAY_WARNING_AUDIO_DECODE_FAIL -> {
                        // 当前音频帧解码失败
                    }

                    TXLiveConstants.PLAY_WARNING_RECONNECT -> {
                        // 网络断连, 已启动自动重连 (重连超过三次就直接抛送 PLAY_ERR_NET_DISCONNECT 了)
                    }
                    TXLiveConstants.PLAY_WARNING_HW_ACCELERATION_FAIL -> {
                        // 硬解启动失败，采用软解
                    }

                    // 连接事件
                    // 连接服务器的事件，主要用于测定和统计服务器连接时间，您也无需关心
                    TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED -> {
                        // 播放器已准备完成，可以播放
                    }
                    TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME -> {
                        // 网络接收到首个可渲染的视频数据包（IDR）
                    }

                    // 分辨率事件
                    TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION -> {
                        // 视频分辨率改变
                        Log.w("zzh", "视频分辨率改变 :" + param?.getString(TXLiveConstants.EVT_DESCRIPTION) + " w:" + param?.getInt(
                            TXLiveConstants.EVT_PARAM1) + " h:" + param?.getInt(TXLiveConstants.EVT_PARAM2))
                    }
                    TXLiveConstants.PLAY_EVT_CHANGE_ROTATION -> {
                        // MP4 视频旋转角度
                        Log.w("zzh", "MP4 视频旋转角度 :" + param?.getString(TXLiveConstants.EVT_DESCRIPTION) + " " + param?.getInt(
                            TXLiveConstants.EVT_PARAM1))
                    }

                    // 视频信息
                    // 如果通过 FileId 方式播放且请求成功，SDK 会将一些请求信息通知到上层
                    TXLiveConstants.PLAY_EVT_GET_PLAYINFO_SUCC -> {
                        param?.getString(TXLiveConstants.EVT_PLAY_COVER_URL) // 视频封面地址
                        param?.getString(TXLiveConstants.EVT_PLAY_URL) // 视频播放地址
                        param?.getInt(TXLiveConstants.EVT_PLAY_DURATION) // 视频时长
                    }
                }
            }

            // 状态反馈
            // 通知每秒都会被触发一次
            // 目的是实时反馈当前的推流器状态，它就像汽车的仪表盘，可以告知您目前 SDK 内部的一些具体情况，以便您能对当前网络状况和视频信息等有所了解
            override fun onNetStatus(player: TXVodPlayer, param: Bundle) {
                Log.w("zzh", "状态反馈")
                // Bundle[{SERVER_IP=112.65.212.89, AUDIO_CACHE=480, VIDEO_DPS=187, VIDEO_FPS=0, VIDEO_WIDTH=640, VIDEO_HEIGHT=360, NET_SPEED=4046, CPU_USAGE=0/0%}]
                param.getString(TXLiveConstants.NET_STATUS_SERVER_IP)
            }

        })

        vodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION)
        vodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)

        val path = intent.getStringExtra(KEY_PATH) ?: "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
        vodPlayer.startPlay(path)

//        vodPlayer.seek()
//        vodPlayer.pause()
//        vodPlayer.resume()
//        vodPlayer.snapshot {
//            if(it != null){
//                // 获取到截图bitmap
//            }
//        }
//        vodPlayer.setRate(1.2) 设置1.2倍速播放

//        缓存的文件可能会被系统图库扫描到，如果您不希望缓存的文件出现在系统图库中，您可以在缓存目录下新建一个名为“.nomedia”的空文件。系统图库发现该文件存在后，会跳过扫描此目录
//        val config = TXVodPlayConfig()
//        config.setCacheFolderPath("....")
//        config.setMaxCacheItems(10) // 指定本地最多缓存多少文件，避免缓存太多数据
//        vodPlayer.setConfig(config)

//        播放视频 A: 如果将 autoPlay 设置为 true， 那么 startPlay 调用会立刻开始视频的加载和播放
//        val urlA = ...
//        vodPlayerA.setAutoPlay(true)
//        vodPlayerA.startPlay(urlA)

//        在播放视频 A 的同时，预加载视频 B，做法是将 true 设置为 false
//        val urlB = ""
//        vodPlayerB.setAutoPlay(false)
//        vodPlayerB.startPlay(urlB)

//        等到视频 A 播放结束，自动（或者用户手动切换到）视频 B 时，调用 resume 函数即可实现立刻播放
//        onPlayEvent(player : TXVodPlayer, event : Int, param : Bundle){
//            if(event == PLAY_EVT_PLAY_END) {
//              vodPlayerA.stop()
//              vodPlayerB.setPlayerView(videoView)
//              vodPlayerB.resume()
//        }

//        vodPlayer.stopPlay(true)
//        vodPlayer.enableHardwareDecode(true)
//        vodPlayer.startPlay()

//        sdk支持hls的多码率格式，方便用户切换不同码率的播放流，
//        在收到play_evt_play_begin事件后，可以通过下面方法获取多码率数组
//        val supportedBitrates : ArrayList<TXBitrateItem> = vodPlayer.supportedBitrates
//        在播放中，可以随时通过 vodPlayer.bitrateIndex = ... 切换码率，切换过程中，会重新拉取另一条流的数据，隐藏会有稍许卡顿
//        sdk对腾讯云的多码率文件做过优化，可以做到切换无卡顿


        // 离线下载
        val downloadManager = TXVodDownloadManager.getInstance()
        downloadManager.setDownloadPath("指定你的下载")

        val startDownloadUrl = downloadManager.startDownloadUrl("http://1253131631.vod2.myqcloud.com/26f327f9vodgzp1253131631/f4bdff799031868222924043041/playlist.m3u8")
        downloadManager.stopDownload(startDownloadUrl)
        downloadManager.deleteDownloadFile("url")

        downloadManager.setListener(object : ITXVodDownloadListener {
            override fun onDownloadStart(mediaInfo: TXVodDownloadMediaInfo?) {
                // 任务开始，表示 SDK 已经开始下载。
            }

            override fun onDownloadProgress(mediaInfo: TXVodDownloadMediaInfo?) {
                // 任务进度，下载过程中，SDK 会频繁回调此接口，您可以在这里更新进度显示
            }

            override fun onDownloadStop(mediaInfo: TXVodDownloadMediaInfo?) {
                // 任务停止，当您调用是 stopDownload 停止下载，收到此消息表示停止成功
            }

            override fun onDownloadFinish(mediaInfo: TXVodDownloadMediaInfo?) {
                // 下载完成，收到此回调表示已全部下载。此时下载文件可以给 TXVodPlayer 播放
            }

            override fun onDownloadError(mediaInfo: TXVodDownloadMediaInfo?, p1: Int, p2: String?) {
                // 下载错误，下载过程中遇到网络断开会回调此接口，同时下载任务停止。错误码位于TXVodDownloadManager中
            }

            override fun hlsKeyVerify(mediaInfo: TXVodDownloadMediaInfo?, p1: String?, p2: ByteArray?): Int {
                // 下载HLS，遇到加密的文件，将解密key给外部校验
                // 0 - 校验正确，继续下载；否则校验失败，抛出下载错误（dk获取失败）
                return 0
            }

        })

    }

    override fun onDestroy() {
        super.onDestroy()
        vodPlayer.stopPlay(true) // true 代表清除最后一帧画面
        videoView.onDestroy()
    }
}