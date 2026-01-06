package com.siti.mobile.Log

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.siti.mobile.Utils.APP_NAME_COLLECTION
import com.siti.mobile.Utils.KEY_USERNAME
import com.siti.mobile.Utils.sharedPrefFile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class FirestoreLog {
    val db = FirebaseFirestore.getInstance()
    val mAuth = FirebaseAuth.getInstance();
    val DOCUMENT_LOGS = "LOGS";
    val USERS_LOGGED_IN = "USERS_LOGGED_IN";
    val CHANNELS_VIEW = "CHANNELS_VIEW";
    val USERS_CHANNELS_VIEW = "USERS_CHANNELS_VIEW";

    val docRef = db.collection(APP_NAME_COLLECTION).document(DOCUMENT_LOGS);

    fun registerLoggedIn(){
        mAuth.uid?.let {
            docRef.collection(USERS_LOGGED_IN)
                .document(it).set(UserLoggedInModel(it, Timestamp.now()))
        }
    }

    fun unregisterLoggedIn(){
        mAuth.uid?.let {
            docRef.collection(USERS_LOGGED_IN)
                .document(it).delete()
        }
    }

    fun increaseChannelMin(channelId : String, channelName : String){
        try{
            val channelRef =  docRef.collection(CHANNELS_VIEW).document(channelId)
            docRef.collection(CHANNELS_VIEW).whereEqualTo("channelId", channelId)
                .get()
                .addOnSuccessListener {
                    if(it.documents.isNotEmpty()) {
                        channelRef.update("minutes", FieldValue.increment(1))
                    }else{
                        channelRef.set(ChannelTimeModel(channelId, channelName,1));
                    }
                }.addOnFailureListener {
                    channelRef.set(ChannelTimeModel(channelId, channelName,1));
                }
        }catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun increaseChannelMinByUser(context : Context, channelId : String, channelName : String){
//        val userName = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).getString(KEY_USERNAME, "null");
//        userName?.let {
//            try{
//                if(userName == "null") return;
//                val channelRef =  docRef.collection(USERS_CHANNELS_VIEW).document(userName)
//                        .collection(CHANNELS_VIEW)
//
//                channelRef.whereEqualTo("channelId", channelId)
//                        .get()
//                        .addOnSuccessListener {
//                            if(it.documents.isNotEmpty()) {
//                                channelRef.document(channelId).update("minutes", FieldValue.increment(1))
//                            }else{
//                                channelRef.document(channelId).set(ChannelTimeModel(channelId, channelName, 1));
//                            }
//                        }.addOnFailureListener {
//                            channelRef.document(channelId).set(ChannelTimeModel(channelId, channelName, 1));
//                        }
//            }catch (e : Exception){
//                e.printStackTrace()
//            }
//
//        }


    }

    suspend fun startTimerIncreaseMinsChannels(channelId : String,channelName:  String , context : Context, currentTimeOnClickPlay : Long) =
        withContext(Dispatchers.IO){
        val handler = Handler(Looper.getMainLooper())
        val delay = 60000L // 60 segundos en milisegundos


        val runnable = object : Runnable {
            override fun run() {
//                if(PreviewScreen.selectedChannelID == channelId && currentTimeOnClickPlay == PlayerScreen.currentTimeOnClickPlay){
                    try{
                        handler.postDelayed(this, delay)
                        increaseChannelMin(channelId, channelName)
                        increaseChannelMinByUser(context, channelId, channelName)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
//                }


            }
        }

        handler.postDelayed(runnable, delay)
    }

    fun getChannelsViewed(context : Context, onChannelsGet : (List<ChannelTimeModel>) -> Unit){
        val userName = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).getString(KEY_USERNAME, "null");
        userName?.let {
            if(userName == "null") return;
            val channelRef =  docRef.collection(USERS_CHANNELS_VIEW).document(userName)
                .collection(CHANNELS_VIEW)

            channelRef
                .get()
                .addOnSuccessListener { it ->
                    if(it.documents.isNotEmpty()) {
                        val channelsViewed : MutableList<ChannelTimeModel>  = mutableListOf()
                        it.documents.forEach { document ->
                            document.toObject(ChannelTimeModel::class.java)?.let {channel ->
                                channelsViewed.add(channel)
                            }

                        }
                        onChannelsGet(channelsViewed.toList())
                    }else{
                        onChannelsGet(emptyList())
                    }
                }.addOnFailureListener {
                    onChannelsGet(emptyList())
                }
        }
    }


    fun log(typeObject : TypeObjectLog, field : String,  message : String)
    {
        val hashmap = HashMap<String,String>()
        hashmap[field] = message
        db.collection("AJKTV").document(DOCUMENT_LOGS).collection(typeObject.toString()).document(System.currentTimeMillis().toString()).set(hashmap)
    }
}

enum class TypeObjectLog{
    Fingerprint
}
