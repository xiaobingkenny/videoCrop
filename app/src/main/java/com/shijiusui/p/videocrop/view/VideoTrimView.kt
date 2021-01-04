package com.shijiusui.p.videocrop.view

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qiniu.pili.droid.shortvideo.PLVideoFrame
import com.shijiusui.p.videocrop.R
import com.shijiusui.p.videocrop.api.IMediaFile
import com.shijiusui.p.videocrop.utils.ScreenUtil
import java.lang.ref.WeakReference
import kotlin.math.ceil

class VideoTrimView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :  FrameLayout(context, attrs, defStyleAttr){


    private var mHandlerLeft: View// 左右两边的黄色滑块儿
    private var mHandlerRight: View// 左右两边的黄色滑块儿

    private var mTopLine: View? = null  // 上下的黄色线
    private var mBottomLine: View? = null// 上下的黄色线

    private var mProgressLine: View? = null// 进度条 白色竖线

    private lateinit var mFrameListView: RecyclerView

    private var mHandlerWidth = 0


    private var mSelectedBeginMs: Long = 0  // 选择的开始和结束时间
    private var mSelectedEndMs: Long = 0// 选择的开始和结束时间


    private var mFrameWidth = 0  // 每帧的宽高
    private var mFrameHeight = 0// 每帧的宽高


    private var mMinLength = 0 // 最大宽度
    private var mMaxLength = 0 // 最小宽度

    private lateinit var plMediaFile: IMediaFile   // 视频媒体文件句柄
    private var mVideoFrameCount = 0                // 视频总帧数
    private var mVideoDurationMs: Long = 0               // 视频总时长

    private var mPreviewFrameIntervalMs = 0f                   // 预览帧间隔时间


    private var mFrameListAdapter: FrameListAdapter? =
        null

    init{
        LayoutInflater.from(context).inflate(R.layout.view_video_trim, this, true)

        mTopLine = findViewById(R.id.activity_video_trim_line_top)
        mBottomLine = findViewById(R.id.activity_video_trim_line_bottom)
        mProgressLine = findViewById(R.id.activity_video_trim_line_progress)

        mHandlerLeft = findViewById(R.id.activity_video_trim_hand_left)
        mHandlerRight = findViewById(R.id.activity_video_trim_hand_right)

        mHandlerLeft.measure(0, 0)
        mHandlerWidth = mHandlerLeft.measuredWidth
    }

    fun initMediaInfo(plMediaFile: IMediaFile) {
        this.plMediaFile = plMediaFile

        mVideoDurationMs = plMediaFile.durationMs

        mPreviewFrameIntervalMs =
            if (mVideoDurationMs > mMaxTime) mMaxTime.toFloat() / mFrameInterval else mVideoDurationMs.toFloat() / mFrameInterval
        mVideoFrameCount = plMediaFile.getVideoFrameCount(false)

        initData()
        initWidth()
        bindAction()
    }

    private fun bindAction(){
        mHandlerLeft.setOnTouchListener(object : OnTouchListener {
            private var xInView = 0f
            private var xInScreen = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_UP -> onActionListener?.play()//videoPlay()
                    MotionEvent.ACTION_DOWN -> {
                        //videoPause()
                        onActionListener?.pause()
                        xInView = mHandlerLeft.x
                        xInScreen = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updateHandlerLeftPosition(event.rawX - xInScreen)
                        calculateRange(-1)
                    }
                }
                return true
            }

            private fun updateHandlerLeftPosition(movedPosition: Float) {
                val lp = mHandlerLeft.layoutParams as LayoutParams
                if (xInView + mMinLength + movedPosition + mHandlerWidth > mHandlerRight.x) {
                    lp.leftMargin = (mHandlerRight.x - mHandlerWidth - mMinLength).toInt()
                } else if (xInView + movedPosition <= 0) {
                    lp.leftMargin = 0
                } else {
                    lp.leftMargin = (xInView + movedPosition).toInt()
                }
                mHandlerLeft.layoutParams = lp
                val topLineLp = mTopLine!!.layoutParams as LayoutParams
                topLineLp.leftMargin = lp.leftMargin + mHandlerWidth
                mTopLine!!.layoutParams = topLineLp
                val bottomLineLp = mBottomLine!!.layoutParams as LayoutParams
                bottomLineLp.leftMargin = lp.leftMargin + mHandlerWidth
                mBottomLine!!.layoutParams = bottomLineLp
                val progressLineLp = mProgressLine!!.layoutParams as LayoutParams
                progressLineLp.leftMargin = lp.leftMargin + mHandlerWidth
                mProgressLine!!.layoutParams = progressLineLp
            }
        })

        mHandlerRight.setOnTouchListener(object : OnTouchListener {
            private var xInView = 0f
            private var xInScreen = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_UP -> onActionListener?.play() //videoPlay()
                    MotionEvent.ACTION_DOWN -> {
                        //videoPause()
                        onActionListener?.pause()
                        xInView = mHandlerRight.x
                        xInScreen = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updateHandlerRightPosition(event.rawX - xInScreen)
                        calculateRange(1)
                    }
                }
                return true
            }

            private fun updateHandlerRightPosition(movedPosition: Float) {
                val lp = mHandlerRight.layoutParams as LayoutParams
                if (xInView + mHandlerWidth + movedPosition >= ScreenUtil.pixelWidth) {
                    lp.rightMargin = 0
                } else if (xInView - mHandlerWidth - mMinLength + movedPosition <= mHandlerLeft.x) {
                    lp.rightMargin =
                        (ScreenUtil.pixelWidth - mHandlerLeft.x - mHandlerWidth - mMinLength - mHandlerWidth).toInt()
                } else {
                    lp.rightMargin =
                        (ScreenUtil.pixelWidth - xInView - mHandlerWidth - movedPosition).toInt()
                }
                mHandlerRight.layoutParams = lp
                val topLineLp = mTopLine!!.layoutParams as LayoutParams
                topLineLp.rightMargin = lp.rightMargin + mHandlerWidth
                mTopLine!!.layoutParams = topLineLp
                val bottomLineLp = mBottomLine!!.layoutParams as LayoutParams
                bottomLineLp.rightMargin = lp.rightMargin + mHandlerWidth
                mBottomLine!!.layoutParams = bottomLineLp
                val progressLineLp = mProgressLine!!.layoutParams as LayoutParams
                progressLineLp.leftMargin =
                    (ScreenUtil.pixelWidth - lp.rightMargin - mHandlerWidth - ScreenUtil.dip2px(7f)).toInt()
                mProgressLine!!.layoutParams = progressLineLp
            }
        })

        mProgressLine!!.setOnTouchListener(object : OnTouchListener {
            private var xInView = 0f
            private var xInScreen = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        onActionListener?.play()
                    } //videoPlay()
                    MotionEvent.ACTION_DOWN -> {
                        //videoPause()
                        onActionListener?.pause()
                        xInView = mProgressLine!!.x
                        xInScreen = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> updateProgressPosition(event.rawX - xInScreen)
                }
                return true
            }

            private fun updateProgressPosition(movedPosition: Float) {
//                if (videoShowView == null) {
//                    return
//                }
                val time: Long
                val lp = mProgressLine!!.layoutParams as LayoutParams
                if (xInView + movedPosition <= mHandlerLeft.x + mHandlerWidth) {
                    lp.leftMargin = (mHandlerLeft.x + mHandlerWidth).toInt()
                    time = mSelectedBeginMs
                } else if (xInView + ScreenUtil.dip2px(7f) + movedPosition >= mHandlerRight.x) {
                    lp.leftMargin = (mHandlerRight.x - ScreenUtil.dip2px(7f)).toInt()
                    time = mSelectedEndMs
                } else {
                    val leftMargin = xInView + movedPosition
                    lp.leftMargin = leftMargin.toInt()
                    time =
                        ((leftMargin - mHandlerLeft.x - mHandlerWidth) * mTimeEveryWidth + mSelectedBeginMs).toLong()
                }
                mProgressLine!!.layoutParams = lp
                //videoShowView.seekTo(time, true)
                onActionListener?.seek(time)
            }
        })
    }

    private fun initWidth(){
        mMaxLength = (ScreenUtil.pixelWidth - mHandlerWidth * 2)

        mFrameWidth = mMaxLength / mFrameInterval
        mFrameHeight = ScreenUtil.dip2px(65f).toInt()

        mSelectedBeginMs = 0
        if (mVideoDurationMs >= mMaxTime) {
            mMinLength = mMaxLength / 3
            mTimeEveryWidth = mMaxTime / mMaxLength.toFloat()
            mSelectedEndMs = mMaxTime.toLong()
        } else {
            mMinLength = (mMaxLength * mMinTime / mVideoDurationMs).toInt()
            mTimeEveryWidth = mVideoDurationMs / mMaxLength.toFloat()
            mSelectedEndMs = mVideoDurationMs
        }
    }

    private fun initData(){
        mFrameListView = findViewById(R.id.activity_video_trim_frame_list)
        mFrameListAdapter = FrameListAdapter()
        mFrameListView.adapter = mFrameListAdapter
        mFrameListView.setItemViewCacheSize(getShowFrameCount())
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mFrameListView.layoutManager = layoutManager
        mFrameListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 滚动停止
                    Log.w("zzh", "scroll---> play")
                    onActionListener?.play()
                } else {
                    Log.w("zzh", "scroll---> pause")
                    onActionListener?.pause()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mListViewScrollX -= dx
                if (!isFirst) {
                    calculateRange(0)
                } else {
                    isFirst = false
                }
            }
        })
    }

    var onActionListener : OnActionListener? = null
    var progressCallback : OnPlayProgressCallback? = null

    interface OnActionListener{
        fun play()
        fun pause()
        fun seek(time: Long)
    }

    interface OnPlayProgressCallback{
        fun getPlayProgressMs() : Int
    }

    fun start(callback: OnPlayProgressCallback){
        this.progressCallback = callback
        post(mUpdateProgressRunnable)
    }

    fun stop(){
        this.progressCallback = null
        removeCallbacks(mUpdateProgressRunnable)
    }

    private val mUpdateProgressRunnable: Runnable = object : Runnable {
        override fun run() {
            removeCallbacks(this)
            updateProgress()
            postDelayed(this, mUpdateProgressTime.toLong())
        }
    }

    var timeCount = 0

    // 更新进度指示器
    private fun updateProgress() {
        if (progressCallback == null) {
            return
        }
        val time: Int = progressCallback!!.getPlayProgressMs()
        val progressLineLp = mProgressLine!!.layoutParams as LayoutParams
        val lp = mHandlerLeft.layoutParams as LayoutParams
        if (time > mSelectedEndMs || time < (mSelectedBeginMs - 100)) {
            // 播放到了选择区的最后
            progressLineLp.leftMargin = lp.leftMargin + mHandlerWidth
            onActionListener?.seek(mSelectedBeginMs)
        } else {
            var leftMargin =
                (lp.leftMargin + mHandlerWidth + (time - mSelectedBeginMs) / mTimeEveryWidth).toInt()
            if (leftMargin + ScreenUtil.dip2px(7f) > mHandlerRight.x) {
                leftMargin = (mHandlerRight.x - ScreenUtil.dip2px(7f)).toInt()
            }
            progressLineLp.leftMargin = leftMargin
        }
        mProgressLine!!.layoutParams = progressLineLp
        if(timeCount++ % 10 == 0){
            //Log.w("zzh", "progressLineLp.leftMargin: " + progressLineLp.leftMargin)
        }
    }

    // 列表滚动的距离
    private var isFirst = true
    private var mListViewScrollX = 0

    // 每个宽度的时长
    private var mTimeEveryWidth = 0f

    private fun calculateRange(type: Int) {
//        if (videoShowView == null) {
//            return
//        }
        mSelectedBeginMs = ((mHandlerLeft.x - mListViewScrollX) * mTimeEveryWidth).toLong()
        val ms: Float = (mHandlerRight.x - mHandlerLeft.x - mHandlerWidth) * mTimeEveryWidth
        mSelectedEndMs = (mSelectedBeginMs + ms).toLong()
//        Logger.getLogger().i(
//            TAG,
//            "mSelectedBeginMs: $mSelectedBeginMs,mSelectedEndMs: $mSelectedEndMs"
//        )

//        mSelectTimeTextView.setText("已选取：" + (Utils.div2(ms, 1000, 0).toString() + "秒"))
        val time: Long
        if (type < 0) {
            // 左边
            time = mSelectedBeginMs
        } else if (type == 0) {
            // 中间
            time =
                (mSelectedBeginMs + (mProgressLine!!.x - mHandlerWidth - mHandlerLeft.x) * mTimeEveryWidth).toLong()
        } else {
            // 右边
            time = mSelectedEndMs
        }
//        Logger.getLogger().i(
//            TAG,
//            "calculateRange-->time: $time"
//        )

//        videoShowView.seekTo(time)
        Log.w("zzh", "onScrolled---> seek : type:$type")
        onActionListener?.seek(time)
    }

    private fun getShowFrameCount(): Int {
        return if (mVideoDurationMs > mMaxTime) {
            ceil((mVideoDurationMs / mPreviewFrameIntervalMs)).toInt()
        } else {
            9
        }
    }

    private class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImageView: ImageView = itemView.findViewById(R.id.thumbnail)

    }

    private inner class FrameListAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ItemViewHolder {
            return ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.video_trim_item_frame,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val params: LayoutParams = LayoutParams(mFrameWidth, mFrameHeight)
            if (position == 0) {
                params.width = ScreenUtil.dip2px(39f).toInt()
            } else if (position == getShowFrameCount() + 1) {
                params.width = ScreenUtil.dip2px(39f).toInt()
            } else {
                params.width = mFrameWidth
            }
            holder.mImageView.layoutParams = params

            // there are 6 dark frames in begin and end sides
            if (position == 0 || position == getShowFrameCount() + 1) {
                return
            }
            val frameTime = ((position - 1) * mPreviewFrameIntervalMs).toLong()
            ImageViewTask(
                holder.mImageView,
                frameTime,
                mFrameWidth,
                mFrameHeight,
                plMediaFile
            ).execute()
        }

        override fun getItemCount(): Int {
            return if (getShowFrameCount() == 9) {
                getShowFrameCount() + 1
            } else {
                getShowFrameCount() + 2
            }
        }
    }

    private class ImageViewTask constructor(
        imageView: ImageView,
        frameTime: Long,
        frameWidth: Int,
        frameHeight: Int,
        mediaFile: IMediaFile
    ) :
        AsyncTask<Void?, Void?, PLVideoFrame?>() {
        private val mImageViewWeakReference: WeakReference<ImageView>
        private val mFrameTime: Long
        private val mFrameWidth: Int
        private val mFrameHeight: Int
        private val mMediaFile: IMediaFile

        override fun doInBackground(vararg v: Void?): PLVideoFrame? {
//            return mMediaFile.getVideoFrameByTime(mFrameTime, false, mFrameWidth, mFrameHeight);
            return mMediaFile.getVideoFrameByTime(mFrameTime, false)
        }

        override fun onPostExecute(frame: PLVideoFrame?) {
            super.onPostExecute(frame)
            val mImageView = mImageViewWeakReference.get() ?: return
            if (frame != null) {
                val rotation = frame.rotation
                val bitmap = frame.toBitmap()
                mImageView.setImageBitmap(bitmap)
                mImageView.rotation = rotation.toFloat()
            }
        }

        init {
            mImageViewWeakReference = WeakReference(imageView)
            mFrameTime = frameTime
            mFrameWidth = frameWidth
            mFrameHeight = frameHeight
            mMediaFile = mediaFile
        }
    }

    companion object {
        private const val TRIM_MIN_DEFAULT_TIME = 5000 //
        private const val TRIM_MAX_DEFAULT_TIME = 15000 //

        private const val mMinTime: Int = TRIM_MIN_DEFAULT_TIME       // 最短时间
        private const val mMaxTime:Int = TRIM_MAX_DEFAULT_TIME        // 最长时间

        // listview15s的帧数
        private const val mFrameInterval = 9                          // 帧数(最大15秒内显示帧数)

        private const val mUpdateProgressTime = 30                    // 30ms 刷新进度
    }
}