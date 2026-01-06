package com.siti.mobile.mvvm.common.data


import android.content.Context
import android.util.Log
import androidx.room.Room
import com.siti.mobile.Utils.RootDatabase
import com.siti.mobile.mvvm.common.data.programs.Program
import com.siti.mobile.mvvm.common.data.programs.toDomain
import com.siti.mobile.mvvm.common.data.programs.toEntity
import kotlinx.coroutines.runBlocking


class DBEPGHelper(private val context: Context) {

    private var database : RootDatabase = Room.databaseBuilder(
        context.applicationContext, RootDatabase::class.java,
        "sdsiptvdb"
    ).fallbackToDestructiveMigration().addMigrations(RootDatabase.MIGRATION_1_2).allowMainThreadQueries().build()
    var TAG = "DBEPGHelper"

    fun isDBOpen() = database.isOpen

    fun openDatabase() {
        if (!isDBOpen()) {
            database = Room.databaseBuilder(
                context.applicationContext, RootDatabase::class.java,
                "sdsiptvdb"
            ).fallbackToDestructiveMigration().addMigrations(RootDatabase.MIGRATION_1_2).allowMainThreadQueries().build()
        }
    }

    fun closeDb(){
        if(database.isOpen){
            try {
                database.close()
            }catch (e: Exception){
                Log.e(TAG, "Error closing DB: ${e.message}")
            }
        }
    }

    fun deleteAllPrograms() = runBlocking {
        database.epgDao().deleteAllPrograms()
    }

    fun insertPrograms(programs: List<Program?>?) = runBlocking {

        programs?.forEach {
            it?.let { program ->
                database.epgDao().insertProgramListJson(program.toEntity())
            }

        }
    }

    fun getAllPrograms(channelId : String) = runBlocking{
        try {
            val programs = database.epgDao().getAllProgramListJson(channelId)
            val newPrograms = programs.map { it.toDomain() }
            newPrograms
        } catch (e: Exception) {
            Log.i(TAG, "insertLiveStreamData: db error " + e.message)
            null
        }
    }
}