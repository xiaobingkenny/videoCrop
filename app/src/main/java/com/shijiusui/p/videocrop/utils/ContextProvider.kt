package com.shijiusui.p.videocrop.utils

import android.app.Application
import android.content.Context

class ContextProvider {
    companion object{
        private lateinit var context : Application
        fun init(application: Application){
            this.context = application
        }

        fun get() : Context{
            return context
        }
    }
}