package com.maha.htmltest.app;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.maha.htmltest.data.AppDatabase;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Room.databaseBuilder(this, AppDatabase.class, "test_db")
                .fallbackToDestructiveMigration()
                .build();
    }
}
