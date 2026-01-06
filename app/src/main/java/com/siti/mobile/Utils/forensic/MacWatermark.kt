package com.siti.mobile.Utils.forensic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class MacWatermark {
    companion object {
        /**
         * Genera un Bitmap con una representación visual de una MAC address.
         * Cada bit se muestra como un bloque (con padding entre ellos).
         */
        fun generate(macAddress: String, blockSize: Int = 20, padding: Int = 2): Bitmap {
            val rows = 6 // 6 bytes
            val cols = 8 // 8 bits por byte

            val totalBlockSize = blockSize + padding
            val width = cols * totalBlockSize - padding
            val height = rows * totalBlockSize - padding

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            // Fondo blanco
            canvas.drawColor(Color.WHITE)

            val parts = macAddress.split(":")
            for ((rowIndex, part) in parts.withIndex()) {
                val byte = part.toInt(16)
                for (bitIndex in 0 until 8) {
                    val isOne = (byte and (1 shl (7 - bitIndex))) != 0
                    paint.color = if (isOne) Color.BLACK else Color.LTGRAY

                    val left = bitIndex * totalBlockSize
                    val top = rowIndex * totalBlockSize

                    canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        (left + blockSize).toFloat(),
                        (top + blockSize).toFloat(),
                        paint
                    )
                }
            }

            return bitmap
        }
    }
}
