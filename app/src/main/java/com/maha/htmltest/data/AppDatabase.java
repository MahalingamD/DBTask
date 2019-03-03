package com.maha.htmltest.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.maha.htmltest.data.dao.TestTableDao;
import com.maha.htmltest.data.entity.TestTable;

@Database(entities = { TestTable.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase INSTANCE;

    public abstract TestTableDao testTable();


    public static AppDatabase getDatabase( Context context ) {
        if( INSTANCE == null ) {
            INSTANCE = Room.databaseBuilder( context.getApplicationContext(), AppDatabase.class, "test_db" )
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static AppDatabase getMemoryDatabase( Context context ) {
        if( INSTANCE == null ) {
            INSTANCE = Room.inMemoryDatabaseBuilder( context.getApplicationContext(), AppDatabase.class )
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
