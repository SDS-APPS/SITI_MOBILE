package com.siti.mobilesds.lco

interface LCOCheckCallback {
    fun onSuccess(response: LCOCheckResponse)
    fun onFailure(t: Throwable)
}
