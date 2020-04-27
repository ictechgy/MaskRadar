package com.jh.mask_radar.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Pharm.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PharmDao pharmDao();

    private static final Object lockObj = new Object();

    private static AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context){

        synchronized (lockObj){
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "pharm.db").build();
            }
            return INSTANCE;
        }

    }

}
