package com.siti.mobile.mvvm.common.data.epg


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siti.mobile.mvvm.common.data.programs.ProgramEntity

@Dao
interface EPGDao {
    @Insert
    suspend fun insertProgramListJson(program: ProgramEntity)

    @Query("select *  from EPG where channel_id=:channelId")
    suspend fun getAllProgramListJson(channelId : String): List<ProgramEntity>

    @Query("delete from EPG")
    suspend fun deleteAllPrograms()
}