package com.siti.mobile.Utils

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.Room
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.Model.advertisment.toEntity
import com.siti.mobile.mvvm.common.data.ParkingChannel
import com.siti.mobile.mvvm.common.data.toEntity

class DBHelperKt(private val context: Context) {

    val TAG = "DBHelperKT"

    private var database = Room.databaseBuilder(
        context.applicationContext, RootDatabase::class.java,
        "sdsiptvdb"
    ).fallbackToDestructiveMigration().addMigrations(RootDatabase.MIGRATION_1_2).allowMainThreadQueries().build()

    fun isDBOpen() = database.isOpen

    fun openDatabase() {
        if (!isDBOpen()) {
            database = Room.databaseBuilder(
                context.applicationContext, RootDatabase::class.java,
                "sdsiptvdb"
            ).fallbackToDestructiveMigration().addMigrations(RootDatabase.MIGRATION_1_2).allowMainThreadQueries().build()
        }
    }

    fun insertAdvertisment(appTV: List<AdvertismentModel>) {
        database.advertismentDao().deleteAllAdvertisment()
        try {
            for (i in appTV.indices) {
                val currentAdvertisment = appTV[i]
                database.advertismentDao().insertAdvertisment(currentAdvertisment.toEntity())
            }
        } catch (e: SQLiteConstraintException) {
            Log.e(TAG, "insertVodProviders: " + e.message)
        }
    }

    fun insertParkingChannels(channels: List<ParkingChannel>) {
        database.liveChannelDAO().clearParkingChannels();
        channels.forEach {
            database.liveChannelDAO().insertParkingChannel(it.toEntity());
        }
    }

    fun closeDb(){
        if(database.isOpen){
            try {
                database.close()
            }catch (e : Exception){
                Log.e(TAG, "Error closing DB: ${e.message}")
            }
        }
    }

}