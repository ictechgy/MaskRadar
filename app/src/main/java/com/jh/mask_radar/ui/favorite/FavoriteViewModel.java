package com.jh.mask_radar.ui.favorite;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.RoomDatabase;

import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;
import com.jh.mask_radar.model.Store;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteViewModel extends ViewModel {

    private MutableLiveData<List<Pharm>> stores;
    private Handler handler;


    public FavoriteViewModel() {
        handler = new Handler();
        stores = new MutableLiveData<>();
        stores.setValue(null);                  //초기 값 null로
    }

    LiveData<List<Pharm>> getStores(){ return stores; }

    void fetchByRoom(AppDatabase db){        //Room을 통해 데이터베이스에서 즐겨찾기 항목 가져오기
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            LiveData<List<Pharm>> pharms = db.pharmDao().getAll();      //값을 가져온 뒤 한번 값을 업데이트 하고 넘겨주자.
            //널값이 넘어오고 있다.. 
            handler.post(()-> stores.setValue(pharms.getValue()));
        });
    }


}