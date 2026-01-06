package com.siti.mobile.Utils.qr

import android.graphics.Bitmap
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder

object QRHelper {
    fun getQrCodeBitmap(inputValue : String) : Bitmap {
        val qrEncoder = QRGEncoder(inputValue, null, QRGContents.Type.TEXT, 5)
        return qrEncoder.bitmap
    }

}