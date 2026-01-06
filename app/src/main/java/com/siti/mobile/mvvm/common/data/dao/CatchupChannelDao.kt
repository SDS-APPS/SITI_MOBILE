package com.siti.mobile.mvvm.common.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siti.mobile.mvvm.common.data.models.CatchupChannelEntity

@Dao
interface CatchupChannelDao {

    @Insert
    fun insertNewCatchupChannel(catchupChannel: CatchupChannelEntity)

    @Query("DELETE FROM CatchupChannelsTable")
    fun deleteAllCatchupChannels()

    @Query("SELECT * FROM CatchupChannelsTable")
    fun getAllCatchupChannels() : List<CatchupChannelEntity>

}