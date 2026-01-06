package com.siti.mobile.Model.RetroFit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siti.mobile.Model.Room.RM_LandingChannel

@Dao
interface LandingChannelDao {
    @Insert
    fun insert(landingChannel: RM_LandingChannel)

    @Query("DELETE FROM LandingChannelTable")
    fun clear()

    @Query("SELECT * FROM LandingChannelTable")
    fun getAll() : List<RM_LandingChannel>
}