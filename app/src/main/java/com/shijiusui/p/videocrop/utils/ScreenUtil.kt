package com.shijiusui.p.videocrop.utils

import android.util.TypedValue

class ScreenUtil {

    companion object{

        var pixelWidth: Int = ContextProvider.get().resources.displayMetrics.widthPixels

        fun dip2px(dip : Float) : Float{
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ContextProvider.get().resources.displayMetrics)
        }
    }
}