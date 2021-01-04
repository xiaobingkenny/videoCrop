package com.shijiusui.p.videocrop.api

interface OnPlayerProgressListener {
    fun onProgress(player : IPlayer, progressMs : Int)
}