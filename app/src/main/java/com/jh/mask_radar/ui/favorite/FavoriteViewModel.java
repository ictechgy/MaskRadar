package com.jh.mask_radar.ui.favorite;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteViewModel extends ViewModel implements Response.ErrorListener {

    private MutableLiveData<List<Pharm>> pharms;
    private Handler handler;
    private RequestQueue requestQueue;
    private String MASK_URL;
    private List<Pharm> fetchedPharms;



    public FavoriteViewModel() {
        handler = new Handler();
        pharms = new MutableLiveData<>();
        pharms.setValue(null);                  //초기 값 null로
    }

    void setQue(RequestQueue requestQueue, String url){
        this.requestQueue = requestQueue;
        MASK_URL = url;
    }

    LiveData<List<Pharm>> getPharms(){ return pharms; }

    void fetchByRoom(AppDatabase db){        //Room을 통해 데이터베이스에서 즐겨찾기 항목 가져오기
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            fetchedPharms = db.pharmDao().getAll();      //값을 가져온 뒤 한번 값을 업데이트 하고 넘겨주자.
            if(fetchedPharms.size() != 0){
                updatePharms();
                //db에 업뎃하는 내용 추가 - code기반으로 필요한 내용만 업데이트
                updatePharmsToDB(db);
                handler.post(()-> pharms.setValue(fetchedPharms));
            }else{
                handler.post(()-> pharms.setValue(new ArrayList<>()));  //size가 0인 리스트로 세팅.
            }
        });
    }

    void updatePharms(){      //마스크 정보 업데이트
        for(Pharm pharm : fetchedPharms){
            final LatLng coord = new LatLng(pharm.lat, pharm.lng);
            final double radius = 0;
            StringBuilder builder = new StringBuilder();
            builder.append(MASK_URL)
                    .append("lat=").append(coord.latitude).append("&lng=").append(coord.longitude).append("&m=").append((int) radius);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, builder.toString(), null,
                    response -> {
                        try{
                            JSONArray jsonArray= response.getJSONArray("stores");
                            parsePharmJson(jsonArray.getJSONObject(0), pharm);

                        }catch (JSONException e){
                            //Store 정보를 파싱하던 중 오류가 발생한 경우에 대한 상세한 처리 필요.
                            e.printStackTrace();
                        }
            }, this);
            requestQueue.add(request);  //통신 시작.
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("volley error", error.getMessage());      //이쪽도 마찬가지로 상세한 처리가 필요하다.
    }

    private void parsePharmJson (JSONObject jsonObject, Pharm pharm){      //데이터 파싱메소드
        //Store store = new Store();
        //store.setCode(jsonObject.getString("code"));

        //String createdAt = jsonObject.optString("created_at", "알 수 없음");
        //store.setCreated_at((createdAt == null || createdAt.equals("null")? "알 수 없음" : createdAt));
        pharm.createdAt = jsonObject.optString("created_at", "알 수 없음");

        //String stockAt = jsonObject.optString("stock_at", "알 수 없음");
        //store.setStock_at((stockAt == null || stockAt.equals("null"))? "알 수 없음" : stockAt);     //요건 이제 없어도 되지 않을까..?
        pharm.stockAt = jsonObject.optString("stock_at", "알 수 없음");

        //store.setRemain_stat(jsonObject.optString("remain_stat", "알 수 없음"));
        pharm.remainStat = jsonObject.optString("remain_stat", "알 수 없음");

        //store.setAddr(jsonObject.getString("addr"));
        //store.setLat((float)jsonObject.getDouble("lat"));
        //store.setLng((float)jsonObject.getDouble("lng"));
        //store.setName(jsonObject.getString("name"));
        //store.setType(jsonObject.getString("type"));

        //필요한 정보만 바꾼다.

    }

    private void updatePharmsToDB(AppDatabase db){
        for(Pharm pharm : fetchedPharms){
            db.pharmDao().updatePharmByCode(pharm.code, pharm.createdAt, pharm.stockAt, pharm.remainStat);
        }
    }
}