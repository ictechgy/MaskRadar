package com.jh.mask_radar.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Pharm.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PharmDao pharmDao();
}
