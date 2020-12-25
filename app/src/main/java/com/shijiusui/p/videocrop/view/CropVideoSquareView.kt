package com.shijiusui.p.videocrop.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class CropVideoSquareView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private val maskColor : Int = 0xc0_000000.toInt()

    private val activeColor = Color.WHITE //0xFFEE2284
    private var aspect : Float = 1f // w/h 默认-1(不控制比例)


    private val paint : Paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
    }

    var lastMeasuredWidth : Int = 0
    var lastMeasuredHeight : Int = 0

    var cornerWLength = 0f
    var cornerHLength = 0f
    var minWLength = 0f
    var minHLength = 0f

    private val dp3 = 8f
    private val halfDp3 = dp3 / 2

    // 区域矩形线宽
    private val dp1 = 4f

    // 裁剪区域
    var cropRectLeft : Float = 0f
    var cropRectRight : Float = 0f
    var cropRectTop : Float = 0f
    var cropRectBottom : Float = 0f

    private fun isBaseWidth() : Boolean {
        if(aspect <= 0){
            return lastMeasuredWidth < lastMeasuredHeight
        }
        return lastMeasuredWidth / lastMeasuredHeight < aspect
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if(width != lastMeasuredWidth || height != lastMeasuredHeight){
            lastMeasuredWidth = measuredWidth
            lastMeasuredHeight = measuredHeight
            calculateInit()
            calculateLine()
            calculateCorner()
        }
    }

    private fun calculateInit(){
        val isBaseWidth = isBaseWidth()
        if(isBaseWidth){
            // 四个角拖动 暂时一样长
            cornerWLength = lastMeasuredWidth/6f
            cornerHLength = cornerWLength

            minWLength = lastMeasuredWidth / 2f
            if(aspect > 0){
                minHLength = minWLength / aspect
            }else{
                minHLength = minWLength
            }

        }else{
            // 四个角拖动 暂时一样长
            cornerHLength = lastMeasuredHeight / 6f
            cornerWLength = cornerHLength

            minHLength = lastMeasuredHeight / 2f
            if(aspect > 0){
                minWLength = minHLength * aspect
            }else{
                minWLength = minHLength
            }

        }

        val width: Float
        val height: Float
        if(aspect <= 0){
            width = lastMeasuredWidth.toFloat()
            height = lastMeasuredHeight.toFloat()
        }else if(isBaseWidth){
            width = lastMeasuredWidth.toFloat()
            height = width / aspect
        }else{
            height = lastMeasuredHeight.toFloat()
            width = height * aspect
        }

        cropRectLeft = (lastMeasuredWidth - width) / 2f
        cropRectTop = (lastMeasuredHeight - height) / 2f

        cropRectRight = cropRectLeft + width
        cropRectBottom = cropRectTop + height
    }

    private val maskTopRect = Rect()
    private val maskLeftRect = Rect()
    private val maskRightRect = Rect()
    private val maskBottomRect = Rect()

    override fun onDraw(canvas: Canvas) {
        paint.color = maskColor
        paint.style = Paint.Style.FILL

        maskTopRect.left = 0
        maskTopRect.top = 0
        maskTopRect.right = lastMeasuredWidth
        maskTopRect.bottom = cropRectTop.toInt()

        maskLeftRect.left = 0
        maskLeftRect.top = cropRectTop.toInt()
        maskLeftRect.right = cropRectLeft.toInt()
        maskLeftRect.bottom = cropRectBottom.toInt()

        maskRightRect.left = cropRectRight.toInt()
        maskRightRect.top = cropRectTop.toInt()
        maskRightRect.right = lastMeasuredWidth
        maskRightRect.bottom = cropRectBottom.toInt()

        maskBottomRect.left = 0
        maskBottomRect.top = cropRectBottom.toInt()
        maskBottomRect.right = lastMeasuredWidth
        maskBottomRect.bottom = lastMeasuredHeight


        canvas.drawRect(maskTopRect, paint)
        canvas.drawRect(maskLeftRect, paint)
        canvas.drawRect(maskRightRect, paint)
        canvas.drawRect(maskBottomRect, paint)

        paint.strokeWidth = dp1
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        canvas.drawRect(cropRectLeft, cropRectTop, cropRectRight, cropRectBottom, paint)

        drawLine(canvas)
        drawCorner(canvas)
    }

    private var line1StartX = 0f
    private var line1StartY = 0f
    private var line1EndX = 0f
    private var line1EndY = 0f

    private var line2StartX = 0f
    private var line2StartY = 0f
    private var line2EndX = 0f
    private var line2EndY = 0f

    private var line3StartX = 0f
    private var line3StartY = 0f
    private var line3EndX = 0f
    private var line3EndY = 0f

    private var line4StartX = 0f
    private var line4StartY = 0f
    private var line4EndX = 0f
    private var line4EndY = 0f

    private fun calculateLine(){
        val oneThirdWCropSide = (cropRectRight - cropRectLeft) / 3
        val oneThirdHCropSide = (cropRectBottom - cropRectTop) / 3

        line1StartX = cropRectLeft + oneThirdWCropSide
        line1StartY = cropRectTop
        line1EndX = line1StartX
        line1EndY = cropRectBottom

        line2StartX = cropRectLeft + oneThirdWCropSide * 2
        line2StartY = cropRectTop
        line2EndX = line2StartX
        line2EndY = cropRectBottom

        line3StartX = cropRectLeft
        line3StartY = cropRectTop + oneThirdHCropSide
        line3EndX = cropRectRight
        line3EndY = line3StartY

        line4StartX = cropRectLeft
        line4StartY = cropRectTop + oneThirdHCropSide * 2
        line4EndX = cropRectRight
        line4EndY = line4StartY
    }

    private fun drawLine(canvas: Canvas){
        paint.strokeWidth = 1f
        paint.color = Color.RED

        // calculateLine()

        canvas.drawLine(line1StartX, line1StartY, line1EndX, line1EndY, paint)
        canvas.drawLine(line2StartX, line2StartY, line2EndX, line2EndY, paint)
        canvas.drawLine(line3StartX, line3StartY, line3EndX, line3EndY, paint)
        canvas.drawLine(line4StartX, line4StartY, line4EndX, line4EndY, paint)
    }

    // 左上 横
    private var leftTopHLineColor = Color.WHITE
    private var leftTopHLineStartX = 0f
    private var leftTopHLineStartY = 0f
    private var leftTopHLineEndX = 0f
    private var leftTopHLineEndY = 0f

    private var leftTopVLineColor = Color.WHITE
    private var leftTopVLineStartX = 0f
    private var leftTopVLineStartY = 0f
    private var leftTopVLineEndX = 0f
    private var leftTopVLineEndY = 0f

    // 右上 横
    private var rightTopHLineColor = Color.WHITE
    private var rightTopHLineStartX = 0f
    private var rightTopHLineStartY = 0f
    private var rightTopHLineEndX = 0f
    private var rightTopHLineEndY = 0f

    private var rightTopVLineColor = Color.WHITE
    private var rightTopVLineStartX = 0f
    private var rightTopVLineStartY = 0f
    private var rightTopVLineEndX = 0f
    private var rightTopVLineEndY = 0f

    // 左下 横
    private var leftBottomHLineColor = Color.WHITE
    private var leftBottomHLineStartX = 0f
    private var leftBottomHLineStartY = 0f
    private var leftBottomHLineEndX = 0f
    private var leftBottomHLineEndY = 0f

    private var leftBottomVLineColor = Color.WHITE
    private var leftBottomVLineStartX = 0f
    private var leftBottomVLineStartY = 0f
    private var leftBottomVLineEndX = 0f
    private var leftBottomVLineEndY = 0f

    // 右下 横
    private var rightBottomHLineColor = Color.WHITE
    private var rightBottomHLineStartX = 0f
    private var rightBottomHLineStartY = 0f
    private var rightBottomHLineEndX = 0f
    private var rightBottomHLineEndY = 0f

    private var rightBottomVLineColor = Color.WHITE
    private var rightBottomVLineStartX = 0f
    private var rightBottomVLineStartY = 0f
    private var rightBottomVLineEndX = 0f
    private var rightBottomVLineEndY = 0f

    private fun calculateCorner(){

        leftTopHLineColor = if(isMove || (isTop && !isRight)) activeColor else Color.WHITE
        leftTopHLineStartX = cropRectLeft - halfDp3
        leftTopHLineStartY = cropRectTop
        leftTopHLineEndX = cropRectLeft + cornerWLength
        leftTopHLineEndY = cropRectTop

        leftTopVLineColor = if(isMove || (isLeft && !isBottom)) activeColor else Color.WHITE
        leftTopVLineStartX = cropRectLeft
        leftTopVLineStartY = cropRectTop
        leftTopVLineEndX = cropRectLeft
        leftTopVLineEndY = cropRectTop + cornerHLength

        rightTopHLineColor = if(isMove || (isTop && !isLeft)) activeColor else Color.WHITE
        rightTopHLineStartX = cropRectRight + halfDp3
        rightTopHLineStartY = cropRectTop
        rightTopHLineEndX = cropRectRight - cornerWLength
        rightTopHLineEndY = cropRectTop

        rightTopVLineColor = if(isMove || (isRight && !isBottom)) activeColor else Color.WHITE
        rightTopVLineStartX = cropRectRight
        rightTopVLineStartY = cropRectTop
        rightTopVLineEndX = cropRectRight
        rightTopVLineEndY = cropRectTop + cornerHLength

        // 左下
        leftBottomHLineColor = if(isMove || (isBottom && !isRight)) activeColor else Color.WHITE
        leftBottomHLineStartX = cropRectLeft - halfDp3
        leftBottomHLineStartY = cropRectBottom
        leftBottomHLineEndX = cropRectLeft + cornerWLength
        leftBottomHLineEndY = cropRectBottom

        leftBottomVLineColor = if(isMove || (isLeft && !isTop)) activeColor else Color.WHITE
        leftBottomVLineStartX = cropRectLeft
        leftBottomVLineStartY = cropRectBottom
        leftBottomVLineEndX = cropRectLeft
        leftBottomVLineEndY = cropRectBottom - cornerHLength

        rightBottomHLineColor = if(isMove || (isBottom && !isLeft)) activeColor else Color.WHITE
        rightBottomHLineStartX = cropRectRight + halfDp3
        rightBottomHLineStartY = cropRectBottom
        rightBottomHLineEndX = cropRectRight - cornerWLength
        rightBottomHLineEndY = cropRectBottom

        rightBottomVLineColor = if(isMove || (isRight && !isTop)) activeColor else Color.WHITE
        rightBottomVLineStartX = cropRectRight
        rightBottomVLineStartY = cropRectBottom
        rightBottomVLineEndX = cropRectRight
        rightBottomVLineEndY = cropRectBottom - cornerHLength
    }

    private fun drawCorner(canvas: Canvas){
        paint.strokeWidth = dp3

        // calculateCorner()

        paint.color = Color.RED //leftTopHLineColor
        canvas.drawLine(
            leftTopHLineStartX,
            leftTopHLineStartY,
            leftTopHLineEndX,
            leftTopHLineEndY,
            paint
        )

        paint.color = Color.RED //leftTopVLineColor
        canvas.drawLine(
            leftTopVLineStartX,
            leftTopVLineStartY,
            leftTopVLineEndX,
            leftTopVLineEndY,
            paint
        )

        paint.color = Color.RED //rightTopHLineColor
        canvas.drawLine(
            rightTopHLineStartX,
            rightTopHLineStartY,
            rightTopHLineEndX,
            rightTopHLineEndY,
            paint
        )

        paint.color = Color.RED //rightTopVLineColor
        canvas.drawLine(
            rightTopVLineStartX,
            rightTopVLineStartY,
            rightTopVLineEndX,
            rightTopVLineEndY,
            paint
        )

        paint.color = Color.RED //leftBottomHLineColor
        canvas.drawLine(
            leftBottomHLineStartX,
            leftBottomHLineStartY,
            leftBottomHLineEndX,
            leftBottomHLineEndY,
            paint
        )

        paint.color = Color.RED //leftBottomVLineColor
        canvas.drawLine(
            leftBottomVLineStartX,
            leftBottomVLineStartY,
            leftBottomVLineEndX,
            leftBottomVLineEndY,
            paint
        )

        paint.color = Color.RED //rightBottomHLineColor
        canvas.drawLine(
            rightBottomHLineStartX,
            rightBottomHLineStartY,
            rightBottomHLineEndX,
            rightBottomHLineEndY,
            paint
        )

        paint.color = Color.RED //rightBottomVLineColor
        canvas.drawLine(
            rightBottomVLineStartX,
            rightBottomVLineStartY,
            rightBottomVLineEndX,
            rightBottomVLineEndY,
            paint
        )
    }

    // 拖动模式
    var isMove = false

    // 改变大小模式
    var isLeft = false
    var isRight = false
    var isTop = false
    var isBottom = false

    var isSlideLeft = false
    var isSlideRight = false
    var isSlideTop = false
    var isSlideBottom = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> handleActionDown(event)
            MotionEvent.ACTION_MOVE -> handleActionMove(event)
            MotionEvent.ACTION_UP -> handleActionUpOrCancel(event)
            MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel(event)
        }

        return true
    }

//    private fun isInRect(x : Float, y : Float, xRange : ClosedFloatingPointRange<Float>, yRange: ClosedFloatingPointRange<Float>) : Boolean{
//        return x in xRange && y in yRange
//    }

    var downX = 0f
    var downY = 0f
    private fun handleActionDown(event: MotionEvent){
        downX = event.x
        downY = event.y

        val rectOverW = (cropRectRight - cropRectLeft) / 6
        val rectOverH = (cropRectBottom - cropRectTop) / 6

        val cropXRange = cropRectLeft..cropRectRight
        val cropYRange = cropRectTop..cropRectBottom
        if(downX in cropXRange && downY in cropYRange){
            if(downX <= cropRectLeft + rectOverW){
                isLeft = true
            }else if(downX >= cropRectRight - rectOverW){
                isRight = true
            }

            if(downY <= cropRectTop + rectOverH){
                isTop = true
            }else if(downY >= cropRectBottom - rectOverH){
                isBottom = true
            }

            isMove = !isLeft && !isRight && !isTop && !isBottom

            return
        }

        val cropOverXRange = (cropRectLeft - rectOverW)..(cropRectRight + rectOverW)
        val cropOverYRange = (cropRectTop - rectOverH)..(cropRectBottom + rectOverH)
        if(downX in cropOverXRange && downY in cropOverYRange){
            if(downX <= cropRectLeft){
                isLeft = true
            } else if(downY >= cropRectRight){
                isRight = true
            }

            if(downY <= cropRectTop){
                isTop = true
            }else if(downY >= cropRectBottom){
                isBottom = true
            }
        }

    }

    private fun updateUI(){
        calculateLine()
        calculateCorner()
        invalidate()
    }

    private fun handleActionMove(event: MotionEvent){
        val moveX = event.x
        val moveY = event.y

        val slideX = moveX - downX
        val slideY = moveY - downY

        if(isMove){
            // 拖拽模式
            handleMove(slideX, slideY)

            updateUI()
            downX = moveX
            downY = moveY

        }else{
            lockSlide(slideX, slideY)

            if(isSlideLeft){
                slideLeft(slideX, slideY)
                updateUI()
                downX = moveX
                downY = moveY
            }else if(isSlideRight){
                slideRight(slideX, slideY)
                updateUI()
                downX = moveX
                downY = moveY
            }else if(isSlideTop){
                slideTop(slideX, slideY)
                updateUI()
                downX = moveX
                downY = moveY
            }else if(isSlideBottom){
                slideBottom(slideX, slideY)
                updateUI()
                downX = moveX
                downY = moveY
            }else{
                // no code
            }
        }
    }

    private fun slideTop(slideX: Float, slideY: Float){
        // 固定底部，移动顶部
        val fixedBottom = cropRectBottom

        cropRectTop += slideY
        if(cropRectTop < 0) cropRectTop = 0f

        var newHeight = fixedBottom - cropRectTop
        if(newHeight < minHLength){
            newHeight = minHLength
            cropRectTop = fixedBottom - newHeight
        }

        if(aspect <= 0){
            updateW4NonFixedRatio(slideX)
        }else{
            var newWidth = newHeight * aspect

            if(newWidth < minWLength){
                newWidth = minWLength

                // 修正h
                newHeight = newWidth / aspect
                cropRectTop = fixedBottom - newHeight
            }

            val fixedWidth = updateW4FixedRatio(newWidth)
            if(fixedWidth != null){
                // 修正h
                newHeight = fixedWidth / aspect
                cropRectTop = fixedBottom - newHeight
            }
        }
    }

    private fun updateW4FixedRatio(newWidth: Float) : Float?{
        if(isLeft){
            val fixedRight = cropRectRight
            cropRectLeft = fixedRight - newWidth

            if(cropRectLeft < 0){
                cropRectLeft = 0f

                return fixedRight - cropRectLeft
            }
        }else if(isRight){
            val fixedLeft = cropRectLeft
            cropRectRight = fixedLeft + newWidth

            if(cropRectRight > lastMeasuredWidth){
                cropRectRight = lastMeasuredWidth.toFloat()

                return cropRectRight - fixedLeft
            }
        }else {
            val oldWidth = cropRectRight - cropRectLeft
            var t = (newWidth - oldWidth) / 2
            if(cropRectLeft - t < 0){
                t = cropRectLeft
            }
            if(cropRectRight + t > lastMeasuredWidth){
                t = lastMeasuredWidth - cropRectRight
            }

            cropRectLeft -= t
            cropRectRight += t

            return cropRectRight - cropRectLeft
        }

        return null
    }

    private fun updateW4NonFixedRatio(slideX: Float){
        if(isLeft){
            cropRectLeft += slideX
            if(cropRectLeft < 0) cropRectLeft = 0f

            var newWidth = cropRectRight - cropRectLeft

            if(newWidth < minWLength){
                newWidth = minWLength
                cropRectLeft = cropRectRight - newWidth
            }
        }else if(isRight){
            cropRectRight += slideX
            if(cropRectRight > lastMeasuredWidth) cropRectRight = lastMeasuredWidth.toFloat()

            var newWidth = cropRectRight - cropRectLeft

            if(newWidth < minWLength){
                newWidth = minWLength
                cropRectRight = cropRectLeft + newWidth
            }
        }else {
            // 上中 no code
        }
    }

    private fun slideBottom(slideX: Float, slideY: Float){
        // 固定顶部，移动底部
        val fixedTop = cropRectTop

        cropRectBottom += slideY
        if(cropRectBottom > lastMeasuredHeight) cropRectBottom = lastMeasuredHeight.toFloat()

        var newHeight = cropRectBottom - fixedTop
        if(newHeight < minHLength){
            newHeight = minHLength
            cropRectBottom = fixedTop + newHeight
        }

        if(aspect <= 0){
            updateW4NonFixedRatio(slideX)
        }else {
            var newWidth = newHeight * aspect

            if(newWidth < minWLength){
                newWidth = minWLength

                // 修正h
                newHeight = newWidth / aspect
                cropRectBottom = fixedTop + newHeight
            }

            val fixedWidth = updateW4FixedRatio(newWidth)
            if(fixedWidth != null){
                // 修正h
                newHeight = fixedWidth / aspect
                cropRectBottom = fixedTop + newHeight
            }
        }
    }

    /**
     * 在未锁定宽高比情况下，向左右拖动时，同时根据纵向移动距离更新上下边
     */
    private fun updateH4NonFixedRatio(slideY: Float){
        if(isTop){
            cropRectTop += slideY
            if(cropRectTop < 0) cropRectTop = 0f

            var newHeight = cropRectBottom - cropRectTop

            if(newHeight < minHLength){
                newHeight = minHLength
                cropRectTop = cropRectBottom - newHeight
            }
        } else if(isBottom){
            cropRectBottom += slideY
            if(cropRectBottom > lastMeasuredHeight) cropRectBottom = lastMeasuredHeight.toFloat()

            var newHeight = cropRectBottom - cropRectTop

            if(newHeight < minHLength){
                newHeight = minHLength
                cropRectBottom = cropRectTop + newHeight
            }
        } else {
            // 左中 纵向不变 no code
        }
    }

    private fun slideRight(slideX: Float, slideY: Float){
        // 固定左边，移动右边
        val fixedLeft = cropRectLeft

        cropRectRight += slideX
        if(cropRectRight > lastMeasuredWidth){
            cropRectRight = lastMeasuredWidth.toFloat()
        }

        // 已w为主，先计算w，再调整h
        var newWidth = cropRectRight - fixedLeft

        if(newWidth < minWLength){
            newWidth = minWLength
            cropRectRight = fixedLeft + newWidth
        }

        if(aspect <= 0){
            updateH4NonFixedRatio(slideY) // 纵向缩放
        }else{
            var newHeight = newWidth / aspect
            if(newHeight < minHLength){
                newHeight = minHLength

                // 修正w
                newWidth = newHeight * aspect
                cropRectRight = fixedLeft + newWidth
            }

            val fixedHeight = updateH4FixedRatio(newHeight)
            if(fixedHeight != null){
                // 修正w
                newWidth = fixedHeight * aspect
                cropRectRight = fixedLeft + newWidth
            }
        }
    }

    /**
     * 根据新的高度{@see newHeight}，调整上下边
     * @return 返回需要修正后高度值（因为根据newHeight修改后的上下边，可能出现越界，这是会返回一个修复后的值）
     */
    private fun updateH4FixedRatio(newHeight : Float) : Float? {
        // 计算top or bottom
        if(isTop){
            cropRectTop = cropRectBottom - newHeight

            // 再次修正
            if(cropRectTop < 0){
                cropRectTop = 0f
                return cropRectBottom - cropRectTop
            }
        }else if(isBottom){
            cropRectBottom = cropRectTop + newHeight

            if(cropRectBottom > lastMeasuredHeight){
                cropRectBottom = lastMeasuredHeight.toFloat()

                return cropRectBottom - cropRectTop
            }
        }else{
            // ???????????? 这种情况是否成立？
            val oldHeight = cropRectBottom - cropRectTop
            var t = (newHeight - oldHeight) / 2
            if(cropRectTop - t < 0){
                t = cropRectTop
            }
            if(cropRectBottom + t > lastMeasuredHeight){
                t = lastMeasuredHeight - cropRectBottom
            }
            cropRectTop -= t
            cropRectBottom += t

            return cropRectBottom - cropRectTop
        }

        return null
    }

    private fun slideLeft(slideX: Float, slideY: Float){
        // 固定右边，移动左边
        val fixedRight = cropRectRight

        cropRectLeft += slideX
        if(cropRectLeft < 0)  cropRectLeft = 0f

        // 已w为主，先计算w，再调整h
        var newWidth = fixedRight - cropRectLeft

        if(newWidth < minWLength){
            newWidth = minWLength
            cropRectLeft = fixedRight - newWidth
        }

        if(aspect <= 0){
            updateH4NonFixedRatio(slideY) // 纵向缩放
        }else{
            // 已w为主
            var newHeight = newWidth / aspect
            if(newHeight < minHLength){
                newHeight = minHLength

                // 修正w
                newWidth = newHeight * aspect
                cropRectLeft = fixedRight - newWidth
            }

            val fixedHeight = updateH4FixedRatio(newHeight)
            if(fixedHeight != null){
                // 修正w
                newWidth = fixedHeight * aspect
                cropRectLeft = fixedRight - newWidth
            }

//            // 计算top or bottom
//            if(isTop){
//                cropRectTop = cropRectBottom - newHeight
//
//                // 再次修正
//                if(cropRectTop < 0){
//                    cropRectTop = 0f
//                    newHeight = cropRectBottom - cropRectTop
//
//                    newWidth = newHeight * aspect
//                    cropRectLeft = fixedRight - newWidth
//                }
//            }else if(isBottom){
//                cropRectBottom = cropRectTop + newHeight
//
//                if(cropRectBottom > lastMeasuredHeight){
//                    cropRectBottom = lastMeasuredHeight.toFloat()
//                    newHeight = cropRectBottom - cropRectTop
//
//                    newWidth = newHeight * aspect
//                    cropRectLeft = fixedRight - newWidth
//                }
//            }else{
//                val oldHeight = cropRectBottom - cropRectTop
//                var t = (newHeight - oldHeight) / 2
//                if(cropRectTop - t < 0){
//                    t = cropRectTop
//                }
//                if(cropRectBottom + t > lastMeasuredHeight){
//                    t = lastMeasuredHeight - cropRectBottom
//                }
//                cropRectTop -= t
//                cropRectBottom += t
//
//                newHeight = cropRectBottom - cropRectTop
//
//                newWidth = newHeight * aspect
//                cropRectLeft = fixedRight - newWidth
//            }
        }
    }

    private fun lockSlide(slideX: Float, slideY: Float){
        if(isLeft && (isTop || isBottom)){
            // 左边

            // 之前没有锁定过三种情况的任意一种
            if(!isSlideLeft && !isSlideTop && !isSlideBottom){
                val absX = abs(slideX)
                val absY = abs(slideY)

                val isHMove = absX > absY

                if(isHMove){
                    if(absX > 10){
                        isSlideLeft = true
                    }
                }else{
                    if(absY > 10){
                        if(isTop){
                            isSlideTop = true
                        }else{
                            isSlideBottom = true
                        }
                    }
                }
            }
        } else if(isRight && (isTop || isBottom)){
            // 右边

            // 之前没有锁定过三种情况的任意一种
            if(!isSlideRight && !isSlideTop && !isSlideBottom){
                val absX = abs(slideX)
                val absY = abs(slideY)

                val isHMove = absX > absY

                if(isHMove){
                    if(absX > 10){
                        isSlideRight = true
                    }
                }else{
                    if(absY > 10){
                        if(isTop){
                            isSlideTop = true
                        }else{
                            isSlideBottom = true
                        }
                    }
                }
            }
        }else if(isLeft && !isSlideLeft){
            isSlideLeft = true
        }else if(isRight && !isSlideRight){
            isSlideRight = true
        }else if(isTop && !isSlideTop){
            isSlideTop = true
        }else if(isBottom && !isSlideBottom){
            isSlideBottom = true
        }
    }

    private fun handleMove(slideX : Float, slideY : Float){
        var moveX = slideX
        var moveY = slideY

        // 修正
        if(cropRectLeft + moveX < 0){
            moveX += (0 - (cropRectLeft + moveX))
        }
        if(cropRectRight + moveX > lastMeasuredWidth){
            moveX -= ((cropRectRight + moveX) - lastMeasuredWidth)
        }
        cropRectLeft += moveX
        cropRectRight += moveX

        // 修正
        if(cropRectTop + moveY < 0){
            moveY += (0 - (cropRectTop + moveY))
        }
        if(cropRectBottom + moveY > lastMeasuredHeight){
            moveY -= ((cropRectBottom + moveY) - lastMeasuredHeight)
        }

        cropRectTop += moveY
        cropRectBottom += moveY
    }

    private fun handleActionUpOrCancel(event: MotionEvent){
        isLeft = false
        isRight = false
        isTop = false
        isBottom = false
        isMove = false
        isSlideLeft = false
        isSlideRight = false
        isSlideTop = false
        isSlideBottom = false

        updateUI()
    }

}