package com.siti.mobile.Utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.siti.mobile.Model.RetroFit.LandingChannelDao;
import com.siti.mobile.Model.Room.RM_LandingChannel;
import com.siti.mobile.mvvm.common.data.AdvertismentEntity;
import com.siti.mobile.mvvm.common.data.ParkingChannelEntity;
import com.siti.mobile.mvvm.common.data.dao.AdvertismentDao;
import com.siti.mobile.mvvm.common.data.dao.CatchupChannelDao;
import com.siti.mobile.mvvm.common.data.epg.EPGDao;
import com.siti.mobile.mvvm.common.data.models.CatchupChannelEntity;
import com.siti.mobile.Interface.DBQueries.ImageDB;
import com.siti.mobile.Interface.DBQueries.LiveChannel;
import com.siti.mobile.Model.Room.RM_ImageDB;
import com.siti.mobile.Model.Room.RM_LiveStreamCategory;
import com.siti.mobile.Model.Room.RM_LiveStreams;
import com.siti.mobile.mvvm.common.data.programs.ProgramEntity;

@Database(entities = {
        RM_LiveStreams.class,
        RM_LiveStreamCategory.class,
        RM_ImageDB.class,
        CatchupChannelEntity.class,
        RM_LandingChannel.class,
        AdvertismentEntity.class,
        ProgramEntity.class,
        ParkingChannelEntity.class
},
        exportSchema = false, version = 29)

public abstract class RootDatabase extends RoomDatabase {
    public abstract LiveChannel liveChannelDAO();
    public abstract ImageDB imageDBDAO();
    public abstract CatchupChannelDao catchupChannelDao();
    public abstract LandingChannelDao landingChannelDao();
    public abstract AdvertismentDao advertismentDao();
    public abstract EPGDao epgDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Aquí debes escribir el código necesario para realizar la migración de la base de datos de la versión 1 a la versión 2.
            // Por ejemplo, puedes usar SQL para agregar la nueva columna a la tabla de la entidad:
            database.execSQL("ALTER TABLE Live_Streams ADD COLUMN count_viewed INTEGER");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Adding OTT Table
         //   database.execSQL("ALTER TABLE Live_Streams ADD COLUMN count_viewed INTEGER");
        }
    };

}
