package com.siti.mobile.mvvm.common.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CatchupChannel(
    val category_id: Int,
    val category_name: String,
    val channel_id: String,
    val channel_name: String,
    val channel_no: Int,
    val description: String,
    val drm_enabled: Int,
    val drm_source: String,
    val logo: String,
    val source: String,
    val recorder: Int
)

@Entity(tableName = "CatchupChannelsTable")
data class CatchupChannelEntity(
    @PrimaryKey
    val channel_id: String,
    val category_id: Int,
    val category_name: String,
    val channel_name: String,
    val channel_no: Int,
    val description: String,
    val drm_enabled: Int,
    val drm_source: String,
    val logo: String,
    val source: String,
    val recorder : Int,
)

fun CatchupChannel.toEntity(): CatchupChannelEntity {
    return CatchupChannelEntity(
        channel_id = channel_id,
        category_id = category_id,
        category_name = category_name,
        channel_name = channel_name,
        channel_no = channel_no,
        description = description,
        drm_enabled = drm_enabled,
        drm_source = drm_source,
        logo = logo,
        source = source,
        recorder = recorder
    )
}

fun CatchupChannelEntity.toDomain(): CatchupChannel {
    return CatchupChannel(
        channel_id = channel_id,
        category_id = category_id,
        category_name = category_name,
        channel_name = channel_name,
        channel_no = channel_no,
        description = description,
        drm_enabled = drm_enabled,
        drm_source = drm_source,
        logo = logo,
        source = source,
        recorder = recorder
    )
}

fun convertCatchupChannelEntityToDomain(catchupChannelEntity: List<CatchupChannelEntity>) = catchupChannelEntity.map { it.toDomain() }.filter { it.recorder == 1 }
fun convertCatchupChannelDomainToEntity(catchupChannel: CatchupChannel) = catchupChannel.toEntity()