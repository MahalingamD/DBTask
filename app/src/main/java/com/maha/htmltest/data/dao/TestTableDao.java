package com.maha.htmltest.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.maha.htmltest.data.entity.TestTable;

import java.util.List;

@Dao
public interface TestTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TestTable content);

    @Update
    void update(TestTable... content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TestTable> order);

    @Query("Delete From testTable")
    void delete();

    @Query("select * from testTable")
   public List<TestTable> aList();

    @Query("select Count(*) from testTable")
    int aRowCount();

}
