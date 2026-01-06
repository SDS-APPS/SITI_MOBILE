package com.siti.mobile.Interface.DBQueries;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.siti.mobile.Model.JoinData.JoinLiveStreams;
import com.siti.mobile.Model.Room.RM_LiveStreamCategory;
import com.siti.mobile.Model.Room.RM_LiveStreams;
import com.siti.mobile.mvvm.common.data.ParkingChannelEntity;

import java.util.List;

@Dao
public interface LiveChannel {
    @Insert
    public void insertLiveStream(RM_LiveStreams RMLiveStreams);

    @Update
    public void updateLiveStream(RM_LiveStreams RMLiveStreams);

    @Query("DELETE from Live_Streams")
    public void deleteLiveChannels();

    @Insert
    public void insertParkingChannel(ParkingChannelEntity parkingChannelEntity);

    @Query("DELETE FROM ParkingChannelTable")
    public void clearParkingChannels();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId  order by Live_Streams.channel_no  ASC")
    public List<JoinLiveStreams> getLiveStreams();

    @Query("select *  from Live_Streams")
    public List<RM_LiveStreams> getLiveStreamsOriginal();

    @Query("select *  from ParkingChannelTable")
    public List<ParkingChannelEntity> getParkingChannels();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId  order by Live_Streams.channel_name  ASC")
    public List<JoinLiveStreams> getLiveStreamsAllSortASC();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId  order by Live_Streams.channel_name  DESC")
    public List<JoinLiveStreams> getLiveStreamsAllSortDesc();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where ImageDB.isFavorite='true' order by Live_Streams.channel_name  ASC")
    public List<JoinLiveStreams> getLiveStreamsFavSortASC();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where ImageDB.isFavorite='true' order by Live_Streams.channel_name  DESC")
    public List<JoinLiveStreams> getLiveStreamsFavSortDesc();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where Live_Streams.channel_name LIKE :name")
    public List<JoinLiveStreams> getLiveStreamsWLName(String name);


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where Live_Streams.category_id = :id")
    public List<JoinLiveStreams> getLiveStreamsByCatId(String id);


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where ImageDB.isFavorite='true'")
    public List<JoinLiveStreams> getLiveStreamsFav();


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where LOWER(category_name)=LOWER(:Category) order by channel_name ASC")
    public List<JoinLiveStreams> getLiveStreamsCategoryAscSort(String Category);


    @Query("select *  from Live_Streams LEFT JOIN ImageDB On channel_id=referenceId   where LOWER(category_name)=LOWER(:Category) order by channel_name DESC")
    public List<JoinLiveStreams> getLiveStreamsCategoryDescSort(String Category);


    @Query("select * from Live_Streams where channel_name=:Name")
    public RM_LiveStreams getLiveStreamsWName(String Name);


//    ----------------------------------live categories-----------------------------------------

    @Insert
    public void insertLiveStreamCategory(RM_LiveStreamCategory liveStreamsCategory);

    //sort live stream category ascending
    @Query("select * from LiveStreamCategory order by position ASC")
    public List<RM_LiveStreamCategory> getLiveStreamsCategory();

    @Query("DELETE from LiveStreamCategory")
    public void deleteLiveCategory();


}
