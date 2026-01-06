package com.siti.mobile.lco

interface LCOCheckCallback {
    fun onSuccess(response: LCOCheckResponse)
    fun onFailure(t: Throwable)
}
