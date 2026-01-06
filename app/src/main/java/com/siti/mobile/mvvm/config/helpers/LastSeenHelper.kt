package com.siti.mobile.mvvm.config.helpers

import android.content.Context
import com.siti.mobile.Utils.sharedPrefFile
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class LastSeenModel(val channelId : String, val channelLogo : String?)

fun LastSeenModel.toJson() : String {
    val gson = Gson()
    return gson.toJson(this)
}

fun String.toLastSeenModel(): LastSeenModel {
    val gson = Gson()
    return gson.fromJson(this, LastSeenModel::class.java)
}

class LastSeenHelper @Inject constructor(@ApplicationContext private val context : Context) {

    val defaultImage = "https://lppm.upnjatim.ac.id/assets/img/nophoto.png"

    val KEY_LAST_SEEN_1 = "lastSeen1"
    val KEY_LAST_SEEN_2 = "lastSeen2"
    val KEY_LAST_SEEN_3 = "lastSeen3"
    val KEY_LAST_SEEN_4 = "lastSeen4"
    val KEY_LAST_SEEN_5 = "lastSeen5"
    val KEY_LAST_SEEN_6 = "lastSeen6"
    val KEY_LAST_SEEN_7 = "lastSeen7"
    val KEY_LAST_SEEN_8 = "lastSeen8"

    fun saveNewLastSeen(lastSeenModel: LastSeenModel){
        val mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = mPreferences.edit()
        val lastSeenChannel1 = mPreferences.getString(KEY_LAST_SEEN_1, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel2 = mPreferences.getString(KEY_LAST_SEEN_2, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel3 = mPreferences.getString(KEY_LAST_SEEN_3, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel4 = mPreferences.getString(KEY_LAST_SEEN_4, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel5 = mPreferences.getString(KEY_LAST_SEEN_5, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel6 = mPreferences.getString(KEY_LAST_SEEN_6, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel7 = mPreferences.getString(KEY_LAST_SEEN_7, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel8 = mPreferences.getString(KEY_LAST_SEEN_8, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)


        if(lastSeenModel.channelId == lastSeenChannel1.channelId){
            return;
        }

        editor.putString(KEY_LAST_SEEN_1, lastSeenModel.toJson())

        if( lastSeenModel.channelId != lastSeenChannel2.channelId &&
            lastSeenModel.channelId != lastSeenChannel3.channelId &&
            lastSeenModel.channelId != lastSeenChannel4.channelId &&
            lastSeenModel.channelId != lastSeenChannel5.channelId &&
            lastSeenModel.channelId != lastSeenChannel6.channelId &&
            lastSeenModel.channelId != lastSeenChannel7.channelId &&
            lastSeenModel.channelId != lastSeenChannel8.channelId)
        {
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
            editor.putString(KEY_LAST_SEEN_5, lastSeenChannel4.toJson())
            editor.putString(KEY_LAST_SEEN_6, lastSeenChannel5.toJson())
            editor.putString(KEY_LAST_SEEN_7, lastSeenChannel6.toJson())
            editor.putString(KEY_LAST_SEEN_8, lastSeenChannel7.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel2.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel3.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel4.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel5.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
            editor.putString(KEY_LAST_SEEN_5, lastSeenChannel4.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel6.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
            editor.putString(KEY_LAST_SEEN_5, lastSeenChannel4.toJson())
            editor.putString(KEY_LAST_SEEN_6, lastSeenChannel5.toJson())
        }else if(lastSeenModel.channelId == lastSeenChannel7.channelId){
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
            editor.putString(KEY_LAST_SEEN_5, lastSeenChannel4.toJson())
            editor.putString(KEY_LAST_SEEN_6, lastSeenChannel5.toJson())
            editor.putString(KEY_LAST_SEEN_7, lastSeenChannel6.toJson())
        }else{
            editor.putString(KEY_LAST_SEEN_2, lastSeenChannel1.toJson())
            editor.putString(KEY_LAST_SEEN_3, lastSeenChannel2.toJson())
            editor.putString(KEY_LAST_SEEN_4, lastSeenChannel3.toJson())
            editor.putString(KEY_LAST_SEEN_5, lastSeenChannel4.toJson())
            editor.putString(KEY_LAST_SEEN_6, lastSeenChannel5.toJson())
            editor.putString(KEY_LAST_SEEN_7, lastSeenChannel6.toJson())
            editor.putString(KEY_LAST_SEEN_8, lastSeenChannel7.toJson())
        }
        editor.apply()
    }

    fun getLastSeen() : List<LastSeenModel>{
        val mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val lastSeenChannel1 = mPreferences.getString(KEY_LAST_SEEN_1, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel2 = mPreferences.getString(KEY_LAST_SEEN_2, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel3 = mPreferences.getString(KEY_LAST_SEEN_3, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel4 = mPreferences.getString(KEY_LAST_SEEN_4, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel5 = mPreferences.getString(KEY_LAST_SEEN_5, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel6 = mPreferences.getString(KEY_LAST_SEEN_6, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel7 = mPreferences.getString(KEY_LAST_SEEN_7, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        val lastSeenChannel8 = mPreferences.getString(KEY_LAST_SEEN_8, null)?.toLastSeenModel() ?: LastSeenModel("-1", defaultImage)
        return listOf(
            lastSeenChannel1,
            lastSeenChannel2,
            lastSeenChannel3,
            lastSeenChannel4,
            lastSeenChannel5,
            lastSeenChannel6,
            lastSeenChannel7,
            lastSeenChannel8
        )
    }
}