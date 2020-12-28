package com.shijiusui.p.videocrop.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class CropVideoSquareView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private val maskColor : Int = 0xc0_000000.toInt()

    private val activeColor : Int = 0xFFEE2284.toInt()
    private var aspect : Float = 1f // w/h 默认-1(不控制比例)


    private val paint : Paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
    }

    var lastMeasuredWidth : Int = 0
    var lastMeasuredHeight : Int = 0

    var cornerMaxWLength = 0f
    var cornerMaxHLength = 0f
    var cornerMinWLength = 0f
    var cornerMinHLength = 0f

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

    var lastRectLeft : Float = 0f
    var lastRectRight : Float = 0f
    var lastRectTop : Float = 0f
    var lastRectBottom : Float = 0f

    private fun isBaseWidth() : Boolean {
        if(aspect <= 0){
            return lastMeasuredWidth < lastMeasuredHeight
        }
        if(lastMeasuredHeight == 0){
            // divide by zero
            return true
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
            calculateMask()

            recordRect()
            onSizeChangedListener?.onSizeChanged(this)
        }
    }

    private fun recordRect(){
        lastRectLeft = cropRectLeft
        lastRectRight = cropRectRight
        lastRectTop = cropRectTop
        lastRectBottom = cropRectBottom
    }

    private fun calculateInit(){
        val isBaseWidth = isBaseWidth()
        if(isBaseWidth){
            // 最小值
            minWLength = lastMeasuredWidth / 2f
            if(aspect > 0){
                minHLength = minWLength / aspect
            }else{
                minHLength = minWLength
            }

            // 四个角拖动 暂时一样长
            cornerMaxWLength = lastMeasuredWidth/6f
            cornerMaxHLength = cornerMaxWLength
        }else{
            // 最小值
            minHLength = lastMeasuredHeight / 2f
            if(aspect > 0){
                minWLength = minHLength * aspect
            }else{
                minWLength = minHLength
            }

            // 四个角拖动 暂时一样长
            cornerMaxHLength = lastMeasuredHeight / 6f
            cornerMaxWLength = cornerMaxHLength
        }

        if(true){
            minWLength = 100f
            minHLength = 100f
        }

        cornerMinWLength = minWLength / 3
        cornerMinHLength = minHLength / 3

        // 修正
        if(cornerMinWLength > cornerMaxWLength){
            cornerMinWLength = cornerMaxWLength
        }
        if(cornerMinHLength > cornerMaxHLength){
            cornerMinHLength = cornerMaxHLength
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

    private fun calculateMask(){
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
    }

    private fun drawMask(paint: Paint, canvas: Canvas){
        paint.color = maskColor
        paint.style = Paint.Style.FILL

        canvas.drawRect(maskTopRect, paint)
        canvas.drawRect(maskLeftRect, paint)
        canvas.drawRect(maskRightRect, paint)
        canvas.drawRect(maskBottomRect, paint)
    }

    private fun drawBorder(paint: Paint, canvas: Canvas){
        paint.strokeWidth = dp1
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        canvas.drawRect(cropRectLeft, cropRectTop, cropRectRight, cropRectBottom, paint)
    }

    private fun drawColor(paint: Paint, canvas: Canvas){
        paint.color = Color.LTGRAY and 0x33_000000
        paint.style = Paint.Style.FILL
        canvas.drawRect(cropRectLeft, cropRectTop, cropRectRight, cropRectBottom, paint)
    }

    override fun onDraw(canvas: Canvas) {

        drawMask(paint, canvas)
        drawColor(paint, canvas)
        drawBorder(paint, canvas)
        drawLine(paint, canvas)
        drawCorner(paint, canvas)
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

    private fun drawLine(paint: Paint, canvas: Canvas){
        paint.strokeWidth = 1f
        paint.color = Color.WHITE // Color.RED
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

    companion object{
        // 左上
        const val L_T_V = 0
        const val L_T_H = 1

        // 右上
        const val R_T_H = 2
        const val R_T_V = 3

        // 右下
        const val R_B_V = 4
        const val R_B_H = 5

        // 左下
        const val L_B_H = 6
        const val L_B_V = 7
    }

    private val cornerLayerIndexArray = IntArray(8) // 激活的在前
    private var cornerLayerCurPtr = 0

    private fun addHead(layerIndex : Int){
        var cur = cornerLayerCurPtr
        while(cur > 0){
            cornerLayerIndexArray[cur] = cornerLayerIndexArray[cur - 1]
            cur--
        }
        cornerLayerIndexArray[0] = layerIndex
        cornerLayerCurPtr++
    }

    private fun addTail(layerIndex : Int){
        cornerLayerIndexArray[cornerLayerCurPtr] = layerIndex
        cornerLayerCurPtr++
    }

    private fun recordIndex(active : Boolean, index : Int){
        if(active) addHead(index) else addTail(index)
    }

    private fun calculateCorner() {
        var cornerWLength = (cropRectRight - cropRectLeft) / 3
        if(cornerWLength < cornerMinWLength){
            cornerWLength = cornerMinWLength
        }else if(cornerWLength > cornerMaxWLength){
            cornerWLength = cornerMaxWLength
        }
        var cornerHLength = (cropRectBottom - cropRectTop) / 3
        if(cornerHLength < cornerMinHLength){
            cornerHLength = cornerMinHLength
        }else if(cornerHLength > cornerMaxHLength){
            cornerHLength = cornerMaxHLength
        }

        // 重置
        cornerLayerIndexArray.fill(-1)
        cornerLayerCurPtr = 0

        var active = isMove || (isTop && !isRight)
        recordIndex(active, L_T_H)
        leftTopHLineColor = if(active) activeColor else Color.WHITE
        leftTopHLineStartX = cropRectLeft - halfDp3
        leftTopHLineStartY = cropRectTop
        leftTopHLineEndX = cropRectLeft + cornerWLength
        leftTopHLineEndY = cropRectTop

        active = isMove || (isLeft && !isBottom)
        recordIndex(active, L_T_V)
        leftTopVLineColor = if(active) activeColor else Color.WHITE
        leftTopVLineStartX = cropRectLeft
        leftTopVLineStartY = cropRectTop - halfDp3
        leftTopVLineEndX = cropRectLeft
        leftTopVLineEndY = cropRectTop + cornerHLength

        active = isMove || (isTop && !isLeft)
        recordIndex(active, R_T_H)
        rightTopHLineColor = if(active) activeColor else Color.WHITE
        rightTopHLineStartX = cropRectRight + halfDp3
        rightTopHLineStartY = cropRectTop
        rightTopHLineEndX = cropRectRight - cornerWLength
        rightTopHLineEndY = cropRectTop

        active = isMove || (isRight && !isBottom)
        recordIndex(active, R_T_V)
        rightTopVLineColor = if(active) activeColor else Color.WHITE
        rightTopVLineStartX = cropRectRight
        rightTopVLineStartY = cropRectTop - halfDp3
        rightTopVLineEndX = cropRectRight
        rightTopVLineEndY = cropRectTop + cornerHLength

        // 左下
        active = isMove || (isBottom && !isRight)
        recordIndex(active, L_B_H)
        leftBottomHLineColor = if(active) activeColor else Color.WHITE
        leftBottomHLineStartX = cropRectLeft - halfDp3
        leftBottomHLineStartY = cropRectBottom
        leftBottomHLineEndX = cropRectLeft + cornerWLength
        leftBottomHLineEndY = cropRectBottom

        active = isMove || (isLeft && !isTop)
        recordIndex(active, L_B_V)
        leftBottomVLineColor = if(active) activeColor else Color.WHITE
        leftBottomVLineStartX = cropRectLeft
        leftBottomVLineStartY = cropRectBottom + halfDp3
        leftBottomVLineEndX = cropRectLeft
        leftBottomVLineEndY = cropRectBottom - cornerHLength

        active = isMove || (isBottom && !isLeft)
        recordIndex(active, R_B_H)
        rightBottomHLineColor = if(active) activeColor else Color.WHITE
        rightBottomHLineStartX = cropRectRight + halfDp3
        rightBottomHLineStartY = cropRectBottom
        rightBottomHLineEndX = cropRectRight - cornerWLength
        rightBottomHLineEndY = cropRectBottom

        active = isMove || (isRight && !isTop)
        recordIndex(active, R_B_V)
        rightBottomVLineColor = if(active) activeColor else Color.WHITE
        rightBottomVLineStartX = cropRectRight
        rightBottomVLineStartY = cropRectBottom + halfDp3
        rightBottomVLineEndX = cropRectRight
        rightBottomVLineEndY = cropRectBottom - cornerHLength
    }

    private fun drawCorner(canvas: Canvas, cornerIndex : Int){
        when(cornerIndex){
            L_T_H -> {
                paint.color = leftTopHLineColor
                canvas.drawLine(
                    leftTopHLineStartX,
                    leftTopHLineStartY,
                    leftTopHLineEndX,
                    leftTopHLineEndY,
                    paint
                )
            }

            L_T_V -> {
                paint.color = leftTopVLineColor
                canvas.drawLine(
                    leftTopVLineStartX,
                    leftTopVLineStartY,
                    leftTopVLineEndX,
                    leftTopVLineEndY,
                    paint
                )
            }

            R_T_H -> {
                paint.color = rightTopHLineColor
                canvas.drawLine(
                    rightTopHLineStartX,
                    rightTopHLineStartY,
                    rightTopHLineEndX,
                    rightTopHLineEndY,
                    paint
                )
            }

            R_T_V -> {
                paint.color = rightTopVLineColor
                canvas.drawLine(
                    rightTopVLineStartX,
                    rightTopVLineStartY,
                    rightTopVLineEndX,
                    rightTopVLineEndY,
                    paint
                )
            }

            L_B_H -> {
                paint.color = leftBottomHLineColor
                canvas.drawLine(
                    leftBottomHLineStartX,
                    leftBottomHLineStartY,
                    leftBottomHLineEndX,
                    leftBottomHLineEndY,
                    paint
                )
            }

            L_B_V -> {
                paint.color = leftBottomVLineColor
                canvas.drawLine(
                    leftBottomVLineStartX,
                    leftBottomVLineStartY,
                    leftBottomVLineEndX,
                    leftBottomVLineEndY,
                    paint
                )
            }

            R_B_H -> {
                paint.color = rightBottomHLineColor
                canvas.drawLine(
                    rightBottomHLineStartX,
                    rightBottomHLineStartY,
                    rightBottomHLineEndX,
                    rightBottomHLineEndY,
                    paint
                )
            }

            R_B_V -> {
                paint.color = rightBottomVLineColor
                canvas.drawLine(
                    rightBottomVLineStartX,
                    rightBottomVLineStartY,
                    rightBottomVLineEndX,
                    rightBottomVLineEndY,
                    paint
                )
            }
        }
    }

    private fun drawCorner(paint: Paint, canvas: Canvas){
        paint.strokeWidth = dp3

        for(index in (7 downTo 0)){
            drawCorner(canvas, cornerLayerIndexArray[index])
        }
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

    var lastTouchX = 0f
    var lastTouchY = 0f
    private fun handleActionDown(event: MotionEvent){
        lastTouchX = event.x
        lastTouchY = event.y

        val rectOverW = (cropRectRight - cropRectLeft) / 6
        val rectOverH = (cropRectBottom - cropRectTop) / 6

        val cropXRange = cropRectLeft..cropRectRight
        val cropYRange = cropRectTop..cropRectBottom
        if(lastTouchX in cropXRange && lastTouchY in cropYRange){
            if(lastTouchX <= cropRectLeft + rectOverW){
                isLeft = true
            }else if(lastTouchX >= cropRectRight - rectOverW){
                isRight = true
            }

            if(lastTouchY <= cropRectTop + rectOverH){
                isTop = true
            }else if(lastTouchY >= cropRectBottom - rectOverH){
                isBottom = true
            }

            isMove = !isLeft && !isRight && !isTop && !isBottom

            return
        }

        val cropOverXRange = (cropRectLeft - rectOverW)..(cropRectRight + rectOverW)
        val cropOverYRange = (cropRectTop - rectOverH)..(cropRectBottom + rectOverH)
        if(lastTouchX in cropOverXRange && lastTouchY in cropOverYRange){
            if(lastTouchX <= cropRectLeft){
                isLeft = true
            } else if(lastTouchY >= cropRectRight){
                isRight = true
            }

            if(lastTouchY <= cropRectTop){
                isTop = true
            }else if(lastTouchY >= cropRectBottom){
                isBottom = true
            }
        }

    }

    private fun updateUI(){
        calculateLine()
        calculateCorner()
        calculateMask()
        invalidate()
    }

    private fun handleActionMove(event: MotionEvent){
        val currentX = event.x
        val currentY = event.y

        val slideX = currentX - lastTouchX
        val slideY = currentY - lastTouchY

        if(isMove){
            // 拖拽模式
            handleMove(slideX, slideY)

            updateUI()
            lastTouchX = currentX
            lastTouchY = currentY

        }else{
            lockSlide(slideX, slideY)

            if(isSlideLeft){
                slideLeft(slideX, slideY)
                updateUI()
                lastTouchX = currentX
                lastTouchY = currentY
            }else if(isSlideRight){
                slideRight(slideX, slideY)
                updateUI()
                lastTouchX = currentX
                lastTouchY = currentY
            }else if(isSlideTop){
                slideTop(slideX, slideY)
                updateUI()
                lastTouchX = currentX
                lastTouchY = currentY
            }else if(isSlideBottom){
                slideBottom(slideX, slideY)
                updateUI()
                lastTouchX = currentX
                lastTouchY = currentY
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

        if(lastRectLeft.isNotEquals(cropRectLeft) || lastRectRight.isNotEquals(cropRectRight) || lastRectTop.isNotEquals(cropRectTop) || lastRectBottom.isNotEquals(cropRectBottom)) {
            recordRect()
            onSizeChangedListener?.onSizeChanged(this)
        }
    }

    private fun Float.isNotEquals(other : Float) : Boolean {
        return abs(this - other) > 1e-8
    }

    public interface OnSizeChangedListener {
        fun onSizeChanged(view : CropVideoSquareView)
    }

    var onSizeChangedListener : OnSizeChangedListener? = null

}