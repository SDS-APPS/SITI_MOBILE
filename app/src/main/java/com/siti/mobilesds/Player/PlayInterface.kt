package com.siti.mobilesds.Player

interface PlayInterface {
    fun onPlay(url : String, drm : Int, token: String?)
}