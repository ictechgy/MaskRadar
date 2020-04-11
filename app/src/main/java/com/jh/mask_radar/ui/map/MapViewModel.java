package com.jh.mask_radar.ui.map;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jh.mask_radar.model.Result;
import com.jh.mask_radar.model.Store;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    private static final String MASK_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?";

    private Result result;     //서버에서 받아온 값의 결과를 가지는 변수. 필요한가?
    private MutableLiveData<List<Store>> stores;

    private RequestQueue requestQueue;      //서버 통신용 Volley queue - 싱글톤 클래스로 wrapping 할 필요 있을까.

    public MapViewModel() {
        stores = new MutableLiveData<>();
        stores.setValue(null);  //초기값 null 설정
    }

    public LiveData<List<Store>> getStores(){
        return stores;
    }

    public void setRequestQueue(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
    }

    public void getMaskInfo(final LatLng coord, final double radius){      //마스크 정보 가져오기. 필요한 값: 지도 중심점 좌표와 반경 반지름
        StringBuilder builder = new StringBuilder();
        builder.append(MASK_URL)
                .append("lat=").append(coord.latitude).append("&lng=").append(coord.longitude).append("&m=").append((int) radius);  //반경값은 Int로 넘겨야 함.

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, builder.toString(), null ,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse (JSONObject response) {
                        try{
                            JSONArray jsonArray= response.getJSONArray("stores");
                            ArrayList<Store> list = new ArrayList<>();
                            //Log.d("jsonResponse", response.toString());
                            for(int i=0; i<jsonArray.length(); i++){
                                Store store = parseStoreJson(jsonArray.getJSONObject(i));   //데이터 파싱
                                store.setIndex(i);
                                list.add(store);
                            }
                            stores.setValue(new ArrayList<>(list));
                        }catch (JSONException e){
                            //Store 정보를 파싱하던 중 오류가 발생한 경우에 대한 상세한 처리 필요.
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("volley error", error.getMessage());      //이쪽도 마찬가지로 상세한 처리가 필요하다.
                    }
                });

        requestQueue.add(request);  //본격 통신 시작.
    }

    private Store parseStoreJson (JSONObject jsonObject) throws JSONException{      //데이터 파싱메소드
        Store store = new Store();

        store.setCode(jsonObject.getString("code"));

        String createdAt = jsonObject.optString("created_at", "알 수 없음");
        store.setCreated_at((createdAt == null || createdAt.equals("null")? "알 수 없음" : createdAt));
        //업데이트 시간이나 입고 시간이 알 수 없음이 뜨는 경우가 존재함. -> 해당 키값이 존재하지 않는 경우도 있어서 optString으로 메소드 대체
        String stockAt = jsonObject.optString("stock_at", "알 수 없음");
        store.setStock_at((stockAt == null || stockAt.equals("null"))? "알 수 없음" : stockAt);     //요건 이제 없어도 되지 않을까..?

        store.setRemain_stat(jsonObject.optString("remain_stat", "알 수 없음"));
        store.setAddr(jsonObject.getString("addr"));
        store.setLat((float)jsonObject.getDouble("lat"));
        store.setLng((float)jsonObject.getDouble("lng"));
        store.setName(jsonObject.getString("name"));
        store.setType(jsonObject.getString("type"));

        return store;
    }

}