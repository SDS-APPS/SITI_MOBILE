package com.siti.mobile.mvvm.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ParkingChannelTable")
data class ParkingChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("channel_id")
    val channelId: Long,
    @ColumnInfo("parking_position")
    val parkingPosition: Int,
    val sec: Int
)

fun ParkingChannelEntity.toDomain() = ParkingChannel(channelId = channelId, parkingPosition = parkingPosition, sec = sec)