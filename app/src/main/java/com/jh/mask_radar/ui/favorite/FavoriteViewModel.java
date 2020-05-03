package com.jh.mask_radar.ui.favorite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.RoomDatabase;

import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.model.Store;

import java.util.List;

public class FavoriteViewModel extends ViewModel {

    private MutableLiveData<List<Store>> stores;


    public FavoriteViewModel() {

        stores = new MutableLiveData<>();
        stores.setValue(null);                  //초기 값 null로
    }

    LiveData<List<Store>> getStores(){ return stores; }

    void fetchByRoom(RoomDatabase db){        //Room을 통해 데이터베이스에서 즐겨찾기 항목 가져오기
        
    }


}