package com.siti.mobile.Model.advertisment

import com.siti.mobile.mvvm.common.data.AdvertismentEntity


data class AdvertismentModel(
    val duration: Int,
    val imageOrder: Int,
    val position: Int,
    val sno: Int,
    val url: String
)

fun AdvertismentModel.toEntity() : AdvertismentEntity {
    return AdvertismentEntity(
        duration = duration,
        imageOrder =  imageOrder,
        position = position,
        sno = sno,
        url = url
    )
}