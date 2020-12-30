package com.shijiusui.p.videocrop.test

class Test(name: String) {
    var name: String? = null
        private set

    private fun setName(name: String) {
        this.name = name
    }

    init {
        setName(name)
    }
}