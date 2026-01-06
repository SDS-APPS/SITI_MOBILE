package com.siti.mobile.mvvm.common.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.siti.mobile.Model.advertisment.AdvertismentModel

@Entity(tableName = "AdvertismentTable")
data class AdvertismentEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val duration: Int,
    val imageOrder: Int,
    val position: Int,
    val sno: Int,
    val url: String
)

fun AdvertismentEntity.toDomain() : AdvertismentModel {
    return AdvertismentModel(
        duration = duration,
        imageOrder = imageOrder,
        position = position,
        sno = sno,
        url = url
    )
}