package com.siti.mobilesds.Utils

interface ChannelSelectedCallback {
    fun onChannelSelected(position : Int, foundedInOriginalChannelData : Boolean)
}