package com.jh.mask_radar.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PharmDao {
    @Query("SELECT * FROM PHARM")
    LiveData<List<Pharm>> getAll();       //FavoriteFragment에서 필요한 기능

    //지도에서 클릭한 약국이 이미 즐겨찾기에 존재하는 약국인지 아닌지를 알아야 한다. code를 이용해 쿼리하기.
    @Query("SELECT EXISTS (select * from pharm where code = :code) result")
    int isExist(String code);       //존재할 경우 1, 존재하지 않을경우 0 반환

    @Insert
    long insertPharm(Pharm pharm);      //추가 후 rowId값 반환

    @Update
    int updatePharms(Pharm ...pharms);        //즐겨찾기 항목에 대한 새로고침 - 사용자가 새로고침을 누르고 값을 다시 받으면 해당 사항을 db에 업뎃하는 작업

    @Delete
    int deletePharm(Pharm pharm); //특정 즐겨찾기 약국 삭제
    @Query("delete from pharm where code = :code")
    int deleteSpecifiedPharm(String code);        //또는 코드값만을 비교한 삭제

    @Query("delete from pharm")     //즐겨찾기 모두 삭제
    int deletePharmAll();

}
