package com.siti.mobile.Utils;

import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;
import static com.siti.mobile.mvvm.common.data.models.CatchupChannelKt.convertCatchupChannelDomainToEntity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.room.Room;

import com.siti.mobile.Model.RetroFit.LandingChannel;
import com.siti.mobile.Model.RetroFit.LiveCategory;
import com.siti.mobile.Model.RetroFit.LiveStream;
import com.siti.mobile.Model.Room.RM_LandingChannel;
import com.siti.mobile.Model.Room.RM_LiveStreamCategory;
import com.siti.mobile.Model.Room.RM_LiveStreams;
import com.siti.mobile.Player.PlayerLiveContainer;
import com.siti.mobile.mvvm.common.data.models.CatchupChannel;
import com.google.gson.Gson;

import java.util.List;

public class DBHelper {

    private RootDatabase database;
    private SharedPreferences mPreferences;
    private final Context context;
    private final String TAG = "DBHelper";

    public boolean isDBOpen() {
        return database != null && database.isOpen();
    }

    public void openDatabase(){
        if(!isDBOpen()){
            database = Room.databaseBuilder(context.getApplicationContext(), RootDatabase.class,
                    "sdsiptvdb").addMigrations(RootDatabase.MIGRATION_1_2).allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
    }

    public DBHelper(Context context){
        this.context = context;
        mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        openDatabase();
    }

    public void closeDb(){
        if(database.isOpen()){
            try{
                database.close();
            }catch (Exception e){
                Log.e(TAG, "Exception closing DB: "+ e.getMessage());
            }
        }
    }

    public boolean insertLiveStreamData(List<LiveStream.LiveStreamData> lists) {
//        Toast.makeText(context, "Total channels: " + lists.size(), Toast.LENGTH_SHORT).show();
        List<RM_LiveStreams> liveStreamsOriginal = database.liveChannelDAO().getLiveStreamsOriginal();
        database.liveChannelDAO().deleteLiveChannels();

        try {
            for (int i = 0; i < lists.size(); i++) {
                LiveStream.LiveStreamData liveStream = lists.get(i);
                RM_LiveStreams streams = new RM_LiveStreams();
                streams.setChannel_no(liveStream.getChannelNo());
                streams.setChannel_id(liveStream.getChannelId());
                streams.setChannel_name(liveStream.getChannelName());
                if(liveStreamsOriginal != null && liveStreamsOriginal.size() > 0) {
                    for(RM_LiveStreams streamOriginal : liveStreamsOriginal) {
                        if(streamOriginal.getChannel_id().equals(streams.getChannel_id())){
                            streams.setCount_viewed(streamOriginal.getCount_viewed());
                        }
                    }
                }

                if (liveStream.getLogo() != null) {
                    streams.setLogo(liveStream.getLogo().toString());
                }

//                if (liveStream.getSource() != null) {
//                    streams.setSource(liveStream.getSource().toString());
//                }
                if (liveStream.getDrmSource() != null) {
                    streams.setDrm_source(liveStream.getDrmSource().toString());
                }

                String categoryJson = new Gson().toJson(liveStream.getCategoryId());
                streams.setCategory_id(categoryJson);
//                streams.setCategory_id(liveStream.getCategoryId().toString());
                streams.setDescription(liveStream.getDescription());
                streams.setDrm_enabled(liveStream.getDrmEnabled());
                streams.setCategory_name(liveStream.getCategoryName());
                streams.setPrice(liveStream.getPrice());
                String token = liveStream.getStreamToken();;
                if(token != null){
                    streams.setStreamToken(token);
                }else{
                    streams.setStreamToken("");
                }

                String encryptedSource = liveStream.getEncryptedSource();
                System.out.println("print encryptedSource DBHelper: " + encryptedSource);
                if(encryptedSource != null){
//                    streams.setEncryptedSource(encryptedSource);
//                    streams.setSource(Crypto.decryptCBC(encryptedSource));
                    streams.setSource(encryptedSource);
                }else{
                    streams.setEncryptedSource("");
                }
//                try{
                    streams.setCatch_up(liveStream.getCatch_up());
                    streams.setRecorder(liveStream.getRecorder());
//                }catch (Exception e){
//
//                }


                database.liveChannelDAO().insertLiveStream(streams);
            }
            if(!lists.isEmpty()){
                String lastPlayedUrl = mPreferences.getString("LAST_PLAYED_URL", "");
                if(lastPlayedUrl.isEmpty()){
                    //[XXX]
                 //   mPreferences.edit().putString("LAST_PLAYED_URL", lists.get(0).getSource().toString()).apply();
                    mPreferences.edit().putString("LAST_PLAYED_URL", PlayerLiveContainer.nullUrl).apply();
                }
                int lastPlayedUrlDrm = mPreferences.getInt("LAST_PLAYED_URL_DRM", 100);
                if(lastPlayedUrlDrm == 100){
                    //[XXX]
                //    mPreferences.edit().putInt("LAST_PLAYED_URL_DRM", lists.get(0).getDrmEnabled()).apply();
                    mPreferences.edit().putInt("LAST_PLAYED_URL_DRM", 1).apply();
                }
            }
            return true;
        } catch (SQLiteConstraintException e) {
            Log.i(TAG, "insertLiveStreamData: " + e.getMessage());
            return false;
        }
    }

    public void insertCatchupChannels(List<CatchupChannel> catchupChannels) {
        database.catchupChannelDao().deleteAllCatchupChannels();
        for(CatchupChannel catchupChannel : catchupChannels) {
            database.catchupChannelDao().insertNewCatchupChannel(convertCatchupChannelDomainToEntity(catchupChannel));
        }
    }

    public void insertLandingChannel(LandingChannel landingChannel) {
        database.landingChannelDao().clear();
        database.landingChannelDao().insert(new RM_LandingChannel(landingChannel.getChannelId()));
    }

    public boolean updateLiveStreams(List<RM_LiveStreams> liveStreams) {
        try{
            database.liveChannelDAO().deleteLiveChannels();
            for(RM_LiveStreams stream: liveStreams) {
                database.liveChannelDAO().insertLiveStream(stream);
            }
            return true;
        }catch (Exception e){
            Log.i(TAG, "updateLiveStreams: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLiveStream(RM_LiveStreams liveStream) {
        try{
            database.liveChannelDAO().updateLiveStream(liveStream);
            return true;
        }catch (Exception e){
            Log.i(TAG, "updateLiveStreams: " + e.getMessage());
            return false;
        }
    }


    public boolean insertLiveStreamCategory(List<LiveCategory.Data> lists) {
        database.liveChannelDAO().deleteLiveCategory();
        try {
            for (int i = 0; i < lists.size(); i++) {
                LiveCategory.Data liveStream = lists.get(i);

                RM_LiveStreamCategory category = new RM_LiveStreamCategory();
                category.setCategory_id(Integer.toString(liveStream.getId()));
                category.setCategory_name(liveStream.getName());
                category.setParent_id(0);
                category.setCategory_count(0);
                category.setPosition(liveStream.getPosition());
                database.liveChannelDAO().insertLiveStreamCategory(category);
            }
            return true;
        } catch (SQLiteConstraintException e) {
            Log.i(TAG, "insertLiveStreamData: db error " + e.getMessage());
            return false;
        }
    }


}
