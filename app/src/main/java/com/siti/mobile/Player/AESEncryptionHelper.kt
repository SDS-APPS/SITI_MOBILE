package com.siti.mobile.Player

import KeyHelper
import android.util.Log
import kotlinx.coroutines.coroutineScope
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AESEncryptionHelper(val ipAddress : String ,val port : String) {

    private lateinit var secretKey: SecretKey
    private lateinit var cipher: Cipher
    val algorithm = "AES"

    fun encrypt(plainText : ByteArray) : ByteArray{
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(plainText)
    }

    suspend fun decrypt(plainText: ByteArray) : ByteArray?  = coroutineScope{
        var result : ByteArray? = null;
        try{
            result = cipher.doFinal(plainText)
        }catch (e : BadPaddingException){
            Log.w(TAG, "${e.message}")
        }
         result
    }

    fun decryptThread(plainText: ByteArray) : ByteArray?  {
        var result : ByteArray? = null;
        try{
            result = cipher.doFinal(plainText)
        }catch (e : BadPaddingException){
            Log.w(TAG, "${e.message}")
        }
        return result
    }

    fun startDecryptionAES() {
        try {
            val keyHelper = KeyHelper(ipAddress)
            val algorithm = "AES"
            val keyString = keyHelper.getFirstKey {newKey ->
                Log.w("KeyDecryption", "NewKey: $newKey")
                cipher = Cipher.getInstance("AES")
                secretKey = SecretKeySpec(newKey.toByteArray(), algorithm)
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }
            secretKey = SecretKeySpec(keyString.toByteArray(), algorithm)

            cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }
}