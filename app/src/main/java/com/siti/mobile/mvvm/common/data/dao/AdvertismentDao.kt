package com.siti.mobile.mvvm.common.data.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siti.mobile.mvvm.common.data.AdvertismentEntity

@Dao
interface AdvertismentDao {
    @Insert
    fun insertAdvertisment(advertisment: AdvertismentEntity)

    @Query("DELETE FROM AdvertismentTable")
    fun deleteAllAdvertisment()

    @Query("SELECT * FROM AdvertismentTable")
    fun getAllAdvertisment() : List<AdvertismentEntity>
}