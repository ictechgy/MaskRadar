package com.jh.mask_radar.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PharmDao {
    @Query("SELECT * FROM PHARM")
    List<Pharm> getAll();

    @Insert
    void insertPharm(Pharm pharm);

    @Delete
    void deletePharm(Pharm pharm);

    //일단 db에는 약국 하나씩 넣고 하나씩 없애고 전부 조회하는 기능만 필요
}
