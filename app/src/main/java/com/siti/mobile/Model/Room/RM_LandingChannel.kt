package com.siti.mobile.Model.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.siti.mobile.Model.RetroFit.LandingChannel

@Entity(tableName = "LandingChannelTable")
data class RM_LandingChannel(
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    val channelId: Long
)

fun RM_LandingChannel.toModel() : LandingChannel{
    return LandingChannel(channelId)
}