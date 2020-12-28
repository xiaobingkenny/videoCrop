package com.shijiusui.p.videocrop

import android.app.Activity
import android.os.Bundle
import com.tencent.liteav.txcvodplayer.TXCVodVideoView
import com.tencent.rtmp.TXBitrateItem
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayConfig
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.ui.TXCloudVideoView

class VideoCropActivity : Activity() {
    private lateinit var videoView: TXCloudVideoView

    private lateinit var vodPlayer : TXVodPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_crop)
        videoView = findViewById(R.id.video_view)

        vodPlayer = TXVodPlayer(this)
        vodPlayer.setPlayerView(videoView)

        vodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION)
        vodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)
        vodPlayer.startPlay("http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4")

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


    }

    override fun onDestroy() {
        super.onDestroy()
        vodPlayer.stopPlay(true) // true 代表清除最后一帧画面
        videoView.onDestroy()
    }
}