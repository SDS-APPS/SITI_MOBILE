package com.siti.mobilesds.Interface.DBQueries;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.siti.mobilesds.Model.Room.RM_ImageDB;

@Dao
public interface ImageDB {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertChannelImage(RM_ImageDB rm_imageDB);

}
