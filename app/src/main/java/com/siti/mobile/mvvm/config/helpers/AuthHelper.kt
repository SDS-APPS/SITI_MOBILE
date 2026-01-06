package com.siti.mobile.mvvm.config.helpers

import android.util.Log
import com.siti.mobile.Log.FirestoreLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

const val TAG = "AuthHelperLog"

class AuthHelper {
    private var auth : FirebaseAuth = Firebase.auth

    private fun isUserLoggedIn() = auth.currentUser != null

    fun signInAnonymously()
    {
        auth.signInAnonymously().addOnSuccessListener {
            Log.v(TAG, "Sign in with firebase sucess")
            Log.v(TAG, it.toString())
        }.addOnFailureListener {
            Log.v(TAG, "Failure signin Firebase")
            Log.v(TAG, it.toString())
        }
    }

    fun signOut() {
        FirestoreLog().unregisterLoggedIn()
        if(isUserLoggedIn()) auth.signOut()
    }
}