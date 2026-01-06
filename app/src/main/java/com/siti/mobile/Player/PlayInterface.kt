package com.siti.mobile.Player

interface PlayInterface {
    fun onPlay(url : String, drm : Int, token: String?)
}