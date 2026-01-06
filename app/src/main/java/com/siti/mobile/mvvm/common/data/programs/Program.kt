package com.siti.mobile.mvvm.common.data.programs

import androidx.room.Entity
import androidx.room.PrimaryKey


data class Program(
    var channel_id : Long,
    var title : String,
    var type : String,
    var source : String,
    var startAt : String,
    var endAt : String,
    var startAtXMLTV : String,
    var endAtXMLTV : String,
    var duration: Long
)

fun Program.toEntity() : ProgramEntity {
    return ProgramEntity(
        channel_id = this.channel_id,
        title = this.title,
        type = this.type,
        source = this.source,
        startAt = this.startAt,
        endAt = this.endAt,
        startAtXMLTV = this.startAtXMLTV,
        endAtXMLTV = this.endAtXMLTV,
        duration = this.channel_id,
    )
}

fun ProgramEntity.toDomain() : Program {
    return Program(
        channel_id = this.channel_id ?: 0,
        title = this.title ?: "",
        type = this.type?: "",
        source = this.source?: "",
        startAt = this.startAt?: "",
        endAt = this.endAt?: "",
        startAtXMLTV = this.startAtXMLTV?: "",
        endAtXMLTV = this.endAtXMLTV?: "",
        duration = this.duration ?: 0L,
    )
}

@Entity(tableName = "EPG")
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var channel_id : Long? = null,
    var title : String? = null,
    var type : String? = null,
    var source : String?= null,
    var startAt : String?= null,
    var endAt : String?= null,
    var startAtXMLTV : String?= null,
    var endAtXMLTV : String?= null,
    var duration: Long?= null
)
