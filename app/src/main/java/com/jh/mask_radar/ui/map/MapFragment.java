package com.jh.mask_radar.ui.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.jh.mask_radar.MainActivity;
import com.jh.mask_radar.R;
import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;
import com.jh.mask_radar.model.Store;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Projection;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

public class MapFragment extends Fragment implements OnMapReadyCallback, NaverMap.OnCameraChangeListener, NaverMap.OnCameraIdleListener, Overlay.OnClickListener, NaverMap.OnMapClickListener, Button.OnClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 777;
    private FusedLocationSource locationSource;
    private LatLng oldPosition;

    private MapViewModel mapViewModel;
    private RequestQueue requestQueue;

    private MapView mapView;
    private NaverMap naverMap;
    private Projection projection;
    private int radiusPixel;    //지도화면상의 중심점을 기준으로 한 반지름(반경)의 픽셀길이값 - 기기별 불변값(지도  크기를 조정한다면 뭐 변하겠지만..)

    private ProgressBar progressBar;        //마커 로딩 중 띄우기.

    private Handler handler;        //Stores 정보 fetch 완료시 별도 쓰레드에서 마커 목록을 생성하고 그 쓰레드에서 메인 쓰레드로 해당 데이터들을 넘겨주기 위한 핸들러
    private ArrayList<Marker> markers;
    private InfoWindow infoWindow;
    private CustomInfoViewAdapter adapter;
    private NavViewHeightReturnSender sender;

    private ArrayList<Store> oldStores;

    private SharedPreferences pref;
    private LatLng lastCoord;

    private AppDatabase db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        //화면에 지도가 보이자마자 권한을 요청. 내 위치 버튼을 누른 경우에 인스턴스화 요청해도 될 것으로 보임.
        //현재 AlertDialog가 띄워져서 바로 보이지 않는 것으로 보임.

        //사용자의 마지막 위치 가져오기
        pref = getContext().getSharedPreferences(getString(R.string.preference_map_fragment), Context.MODE_PRIVATE);
        if(pref!=null && pref.contains("latitude") && pref.contains("longitude")){
            lastCoord = new LatLng(pref.getFloat("latitude", 0), pref.getFloat("longitude", 0));
        }
    }
    //현재 내 위치 버튼을 누른 경우 '데이터와 GPS 켜져있고 관련 권한 허용상태'인데도 비정상 종료가 발생하는 경우 있음
    //네트워크 및 GPS ON/OFF, 허용 여부에 따른 LocationTrackingMode 활성화 상태 변경 및 기타 상황에 대한 제어 처리 필요할 것 같음. 네트워크가 꺼져있으면 앱이 시작되지 않게 한다던지 등.
    //즐겨찾기 (+ 추후 알람기능) 기능 작동을 InfoWindow 클릭으로 할지, 다른 방식으로 추가할 수 있게 할지.. (마커 onLongClick은 없음)
    //InfoWindow를 나중에는 다른 알림팝업으로도 구현 가능(그렇게 하면 해당 마커 클릭 시 해당 좌표를 중심점으로 이동시키는 기능을 추가해도 infoWindow가 닫히지 않아 좋음)
    //또 해당 알림팝업에서 즐겨찾기나 알람 추가 버튼을 추가적으로 구현하기 쉬울 것임..(네이버 지도 구현방식처럼)
    // -> BottomSheet가 있으나 이걸 쓸 경우 BottomNavigation과의 위치를 또 고려해야함.. 흠.. 그냥 Dialog처럼 띄워야 하나.
    //지역 검색기능

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);  //true 반환
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    new MaterialAlertDialogBuilder(getContext()).setTitle(R.string.location_denied_title).setMessage(R.string.location_denied_message)
                            .setPositiveButton(R.string.confirm, ((dialog, which) -> {})).setCancelable(true).show();
                }
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);    //권한이 거부됐음에도 트래킹 아이콘이 작동하는 듯한 모습 해결용
                //그래도 버튼 주위로 빙글빙글 돌아가는건 남아있네..
                return;     //위치 권한일 시 return 해줘야 할 것으로 보임.
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        /*
        기존 코드
        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            //내 위치 찾기 권한에 대한 요청코드(777)과 같을 경우 true 반환.
            //즉, 사용자가 내 위치 보기를 원한 경우 반드시 FusedLocationSource의 onRequest~ 메소드로 넘겨줘야 하며 반환 값은 true 일 것이다.
            //이 안에서 권한 획득 여부에 따른 추가 코드를 작성해야 함. 다른 권한 관련일 시 false 반환일 것이며 외부에서 처리.
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         */
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        requestQueue = Volley.newRequestQueue(getContext());
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.setRequestQueue(requestQueue); //viewModel로 requestQueue 전달

        mapViewModel.getStores().observe(getViewLifecycleOwner(), new Observer<List<Store>>() {
            @Override
            public void onChanged(List<Store> stores) {
                reflectChanges(stores);
            }   //지도상의 Store정보 관측
        });

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        progressBar = root.findViewById(R.id.map_progressBar);

        handler = new Handler();

        MaterialTextView targetTextView = root.findViewById(R.id.target_button);    //구매 대상자 알리기
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        //sunday=1, mon=2 .3.4.5.6. sat = 7 -> 보급패턴은 1,6 2,7 3,8 4,9 5,0
        String targetText;
        if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY){
            targetText = " 주중 미 구입자 구매 가능";
        }else{
            targetText = "출생연도 끝자리 "+(dayOfWeek-1)+", "+((dayOfWeek+4)%10);
        }       //이거에다가 뭐 각종 알림도 띄워줘야 하고.. 마커에 대한것도 더 해야하고(기존 마커 그릴지 그대로 둘지, GPS까지.)
        targetTextView.setText(targetText);

        targetTextView.setOnClickListener((v)->{
            Toast.makeText(getContext(), "오늘은"+targetText+"구매가능 날입니다.", Toast.LENGTH_LONG).show();
        });

        /*
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("공지사항").setMessage("1. 성인용 마스크만 볼 수 있으며 베타서비스입니다." +
                "\n2. 실제 판매처와 5~10분의 차이가 있을 수 있습니다." +
                "\n3. 일부 약국의 번호표 배부나 기타 배부방식은 반영하지 못합니다. 이점 유의 부탁드리겠습니다." +
                "\n4. 데이터출처 : 건강보험심사평가원&정보화진흥원" +
                "\n5. 데이터 공개문의는 maskdata@nia.or.kr로 문의해주세요." +
                "\n6. 일선에서 공헌해주시는 약사, 우체국, 하나로마트분들에게 감사의 말을 전해주세요.")
        .setPositiveButton("알겠습니다.", (dialog, which) -> {
            dialog.dismiss();
        }).setIcon(R.drawable.ic_warning_24px).show();

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.main_alert_title)
                .setItems(getResources().getStringArray(R.array.main_alert_messages), (dialog, which)->{})
                .setPositiveButton(R.string.main_alert_button, (dialog, which) -> {})
                .setIcon(R.drawable.ic_warning_24px).show();

         */
        if(pref == null) pref = getContext().getSharedPreferences(getString(R.string.preference_map_fragment), Context.MODE_PRIVATE);
        int storedDayOfMonth = pref.getInt("day_of_week", 0);
        boolean isDayChanged = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != storedDayOfMonth;
        //날짜는 Sunday를 1로 시작하여 Saturday를 7로 끝난다.
        //최초 로딩시에는 defValue인 0을 반환하여 무조건 Alert가 뜬다.

        if(isDayChanged){   //날짜가 바뀐 경우에 Alert 띄우기

            AlertDialog mainAlertDialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.main_alert_title)
                    //보여줄 메시지가 많은 편이 아니므로 ListAdapter를 써보자.
                    .setAdapter(new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_list_item_1,
                            Arrays.asList(getResources().getStringArray(R.array.main_alert_messages))),
                            (dialog, which) -> {})
                    .setPositiveButton(R.string.main_alert_button, (dialog, which) -> {
                        if(storedDayOfMonth != 0) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.remove("day_of_week");
                            editor.apply();
                        }
                    })
                    .setNegativeButton(R.string.main_alert_cancel, ((dialog, which) -> {
                        //하루동안 보지 않기 구현 코드
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("day_of_week", Calendar.getInstance().get(Calendar.DAY_OF_WEEK)); //오늘의 요일을 저장한다.
                        editor.apply();
                    }))
                    .setIcon(R.drawable.ic_warning_24px).create();

            mainAlertDialog.setOnShowListener(dialog -> {
                        mainAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorFew, null));
                        mainAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorSecondary, null));
                    });
            mainAlertDialog.show();
        }
        //하루동안 보지 않기 코드에 대하여.
        //최초 로딩시에는 storedDayOfMonth 값이 0이 뜨므로 무조건 Alert는 띄워질 것이다. 이후 사용자가 오늘 하루 보지않기를 누르면 오늘 날짜를 저장한다.
        //이후에는 날짜가 바뀌었는지 확인하여 Alert를 띄운다.
        //사용자가 오늘 하루 보지않기를 일요일에 했는데 그 이후에 계속 확인만을 누른다면 다시 돌아오는 일요일에 Alert가 안띄워질 것이다.
        //따라서 날짜가 바뀐 경우 확인을 누르면 값을 0으로 되돌려 놓는다. 사용자가 오늘 하루 다시보지 않기를 누르지 않는 이상 기본적으로 다시 언제나 Alert가 띄워지도록.
        //사용자 유형 : 계속 확인만 누르는 사용자, 날짜가 바뀔 때마다 오늘 보지 않기를 누르는 사용자, 오늘 보지 않기를 눌렀다가 확인만 누르는 사용자


                //메시지 디자인 조금 바꿔보자. 그 다음 310번째 줄 내용(마커)
        // + 네트워크 스테이터스 상황 읽어서 네트워크 없을 시 실행 안되게 해야 할듯. 네트워크 안되니 비정상 종료됨
        // + 권한관련 설명내용 필요할 듯. 등.

        return root;
    }

    /*
    추가해야하는 기능
    화면 중심점 찾고 그 중심점의 좌표 기준으로 주변 약국 마스크 재고 보여주기 (화면의 최대 확대/축소에 대한 부분 생각해야함), 몇km 반경 보여줄지 생각
    GPS기능 추가하기(허용권한 알림 필요) 및 현재위치 누르면 현재위치로 이동되면서 주변 약국 찾기
    약국 마커 누르면 해당 약국에 대한 정보들 띄워주기. 띄워주는 방식은 더 생각하자. 약국 마커 위에 간략히 소개하고 한번 더 누르면 자세한 소개 페이지로 이동?
    해당 소개 페이지에는 약국 사진도 보여주고.. (거리뷰 등)

    지도 축소 어느정도 되면 마커 그룹화해서 지역별 마커를 띄우고, 해당 지역의 약국에 대한 재고있는 약국 수, 재고 없는 약국 수 간략하게 옆에 보이기
    또 현재 위치 중심으로 어느 약국이 재고 있고 가장 가까운지 보여주기 (네비게이션도 있으면 좋을 듯.)

    가능하다면 현재위치 기준으로 마스크 구입가능한 약국의 위치 알림?
    약국, 우체국 등 공적판매처 위치정보 추가
    약국 즐겨찾기 기능 - 즐겨찾기 한 해당 약국 재고 올라가면 알림

    여러 사항들 모달창 팝업, 공지사항 탭 등으로 구현. (약사님 배려해줍시다 등)
    궁금할 만한 사항들 식약처 등 링크 구현, 가져와서 구현도 하던지.. 아이들 대리수령이라던지 등등.

    클릭한 마커를 화면 중심점에 두는 기능

    성능 최적화
    판매처 상세 페이지로 들어갔다 나오거나 다른 화면으로 갔다 오는 경우 기존의 지도 위치나 마커리스트 변경유무
    앱을 껐다가 킨 경우 기존 위치를 보일지 유무
    지도를 별로 움직이지 않은 경우 새롭게 보이는 부분에 대해서만 자료를 가져오게 하는 기능 부분.(store 리스트 중복에 대한 제어)
    건강보험 심사평가원 판매에 대한 데이터 새로고침 주기 설정 유무
     */


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof NavViewHeightReturnSender){
            sender = (NavViewHeightReturnSender) context;
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {        //맵로딩 끝난 후
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);

        Location lastLocation = locationSource.getLastLocation();
        if(lastLocation != null){
            LatLng lastCoord = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            if(!naverMap.getCameraPosition().target.equals(lastCoord)){
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(lastCoord).animate(CameraAnimation.None);
                naverMap.moveCamera(cameraUpdate);
            }
        }   //마지막 위치로 지도를 자동 이동시키는 부분이나 현재 작동하지 않음(앱을 껐다 켰다거나 다른 화면으로 갔다가 돌아온다거나)

        //마지막 맵 위치 구현 코드 -> onCreate부로 마지막 좌표값 초기화 코드 이동
        /*
        SharedPreferences pref = getContext().getSharedPreferences(getString(R.string.preference_map_fragment), Context.MODE_PRIVATE);
        if(pref.contains("latitude") && pref.contains("longitude")){
            LatLng lastCoord = new LatLng(pref.getFloat("latitude", 0), pref.getFloat("longitude", 0));
            if(!naverMap.getCameraPosition().target.equals(lastCoord)){
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(lastCoord).animate(CameraAnimation.None);
                naverMap.moveCamera(cameraUpdate);
            }
        }
         */
        //마지막 위치값으로 지도 중심점 이동
        if(lastCoord != null && !naverMap.getCameraPosition().target.equals(lastCoord)){
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(lastCoord).animate(CameraAnimation.None);
            naverMap.moveCamera(cameraUpdate);
        }
        //지도 위치 초기값 설정부분에다가 작성? 해당 부분에 설정해주면 좋을 것 같은데.. 저장된 위치 없으면 기본값 그대로 뜨게 하고. 있으면 바로. CameraUpdate 시킬 필요없이.
        //-> XML로 MapView를 설정했기 때문에 초기값으로 생성할 수는 없을 것 같다. 객체로 생성시에는 생성할 때 설정해줄 수 있어보임.


        /*
        int bottomNavHeight = (sender.getNavViewHeight() == 0)? (int)getResources().getDimension(R.dimen.bottom_navigation_height): sender.getNavViewHeight();
        naverMap.setContentPadding(0, 0, 0, bottomNavHeight);     //일반적으로 BottomNavigationView는 56dp 이다.
        //BottomNav로 인해 지도가 잘리는 현상때문에 padding 설정.
        -> BottomView를 안쓰므로 삭제.
         */
        naverMap.setIndoorEnabled(true);    //실내 지도 지원
        UiSettings settings = naverMap.getUiSettings();
        settings.setCompassEnabled(true);       //나침반
        settings.setZoomControlEnabled(false);  //줌버튼이 시야를 방해하여 제거함.
        settings.setLocationButtonEnabled(true);    //현재위치버튼 넣기
        naverMap.setMinZoom(13);        //축소 레벨 제한. 지나치게 축소하는 경우 마커를 그리는 수가 많아져서 성능저하.

        projection = naverMap.getProjection();
        infoWindow = new InfoWindow();  //마커 정보창용 객체 인스턴스화
        adapter = new CustomInfoViewAdapter(getContext(), mapView); //InfoWindow 에 대해 커스텀 어댑터 생성
        infoWindow.setAdapter(adapter);     //infoWindow.open이 호출 될 때마다 어댑터 내부의 getContentView 호출.

        naverMap.setOnMapClickListener(this);       //맵에서 특정부분 그냥 클릭시 정보창은 닫는다.

        naverMap.addOnCameraChangeListener(this);   //카메라 이동에 대한 리스너 - 일단 작동하는 것은 없음
        naverMap.addOnCameraIdleListener(this);     //카메라 이동 완료 리스너 - 이동 완료시 약국 정보 다시 fetch

        int contentWidthHalf = naverMap.getContentWidth()/2;
        int contentHeightHalf = naverMap.getContentHeight()/2;
        radiusPixel = Math.min(contentWidthHalf, contentHeightHalf);
        //너비와 높이 중 낮은 값을 원의 반지름으로 설정    - 높은 값으로 설정해서 더 넓은 범위를 보여주도록 할 수도 있고, 두 값을 합하여 반으로 나눔으로서 평균 수치로 접근도 가능하다.
        //픽셀단위 지도 반경 얻어오기(화면 기준) - 반지름 픽셀값을 원하므로 2로 나누어서 구함.

        //getMaskInfo();        //맵 로드 초기에도 onCameraIdle()이 최초 한번 작동하여 정보를 가져온다. 따라서 여기서 메소드 작동시킬 필요가 없다.
    }
    //카메라가 움직이는 도중에도 지도 마커 업데이트를 할 것인지, 움직임이 끝나고 사용자가 손을 떼면 업데이트를 할 것인지..
    //움직임이 끝나는 경우에 대한 리스너는 addOnCameraIdleListener 임.

    /**
     * parameter description by Naver Corp.
     * @param i means 'cause of movement'
     * @param b means 'True if the animation effect is applied and moving, false if not.'
     */
    @Override
    public void onCameraChange(int i, boolean b) {
        //do something?
    }

    @Override
    public void onCameraIdle() {        //카메라 움직임이 끝난 경우. - onMapReady 이후에 기본적으로 최초 한번 호출.
        LatLng newPosition = naverMap.getCameraPosition().target;
        if(oldPosition != null && oldPosition.equals(newPosition)){
            return;
        }
        oldPosition = newPosition;
        //현재위치 버튼 눌렀을 시 해당 위치로 이동한 상황일 시 onCameraIdle이 계속 실행된건지 마커 무한으로 그리려 해서 추가한 구문.

        progressBar.setVisibility(View.VISIBLE);

        if(markers != null && !markers.isEmpty()){
            removeMarker();     //기존 마커 제거
        }
        getMaskInfo();

        //지도를 움직인 경우 보여지지 않는 부분에 대한 마커 제거, 최대 확대/축소값 제한(또는 축소 많이 했을 시 마커를 지역별 단일화 하고 해당 지역의 약국 통계 보여주던가)
        //지도를 움직여도 기존 마커가 남아있는 부분에 대해 새 데이터를 받아서 새로 마커를 그릴지, 기존 마커를 그대로 둘지의 여부. - 그러면 비교 알고리즘이 들어갈 것 같은데.
        //기존에 있는 것과 새로 받아온 것을 비교하는 알고리즘. 중복 존재하는 약국 마커는 그대로 두고 비교해서 새로 받아온 리스트에 존재하지 않는 마커는 지운다던가..(지도에서 사라졌다는 뜻이니까)
        //새로 리스트에 들어온것만 그리면 됨..

        //아니면 전부 제거하고 다시 그리는 방식으로 실시간 데이터 받아오는 방식으로 만들 수도 있다. (계속 API로 값 받아오는 방식이 됨)
        //보여지지 않아진 부분만 제거하고 새로 보이는 부분만 만드는 방식으로 하고, 기존에 존재한 마커는 새로 그릴지, 그대로 둘지.
        //아니면 약국 리스트에 대해 구 데이터와 새 데이터 서로를 비교하는 알고리즘을 쓰는게 아니라 좌표지점 영역값(왼쪽 상단값-우측 하단값 등)을 이용해서
        //좌표 내에 존재하는 약국 들에 대해서는 마커를 유지시키는 방법도 존재한다. 해당 좌표를 벗어난 마커는 제거하고.. 해당 좌표내에 새로 들어온 것만 그린다던가. 등등...
    }

    private void removeMarker(){        //새 마커 그리기 전 기존 마커 제거

        /*
        oldStores = new ArrayList<>();
        List<Store> existingStores = mapViewModel.getStores().getValue();
         */
        for(Marker marker : markers){
            /*
            LatLngBounds bounds = naverMap.getContentBounds();
            if(!bounds.contains(marker.getPosition())){  //지도를 이동했지만 마커가 새로 보이는 지도에도 포함되는 마커인경우 -> 마커를 지울 필요가 없다.
                marker.setMap(null);
                continue;
            }
            MarkerInfo markerInfo = (MarkerInfo)marker.getTag();
            markerInfo.setNew(false);   //새 마커가 아니다 설정
            oldStores.add(existingStores.get(markerInfo.getIndex()));
            markerInfo.setIndex(oldStores.size()-1);

             */
            //위의 과정까지 해서 지도에서 제거하지 않아도 되는 마커는 제거 안되도록 하긴 했다. 나중에 infoWindow 열 시 setNew값에 따라 가지고 올 store값만 달리 하면 되는데
            //문제는 마커를 새로 그리는 부분에 대해서 어떻게 위에 중첩해서 그리지 않도록 하냐의 문제이다. 뭐 새로 나타나게 된 지역에 대해서만 bounds를 가져오고, 해당 지역에
            //대한 마커만 그리게 해야하나. 단순 비교 알고리즘은 n^2 복잡도를 가질 것으로 보인다.
            //예를 들어서.. 기존 지도위치와 중첩되는 바운즈(지역 범위)나 old스토어, oldMarkers에 대한 목록을 ViewModel에 set해서 Json파싱 중 새 마커(스토어)만 그리게 새 값만 걸러내려 한다고 하더라도,
            //기존 데이터 n개에 대해서 새 데이터 n개끼리 비교를 해야하는건 마찬가지다. 결국 n^2복잡도를 지닌다. (쉽게 말해 하나의 새 데이터가 기존 지도에 있던 값인지를 n번 비교해야한다는 것)
            //그냥 새 지역 바운드에 대해서만 선택적으로 값을 가져올 수 있도록 할 수 있으면 모를까.. 흠. 아니면 변동된 지역 바운드에 대해서만 마커가 그려지도록
            //새 데이터를 가져 오더라도 해당 데이터 LatLng 좌표 포지션이 중복되지 않는, 새로 보이는 지역좌표값(바운즈)값에 속한 경우에만 새로 그리도록 할 수도 있다.
            //반대로 중첩되는 지역 바운드를 뷰모델에 세팅해두고 json 파싱중에 해당 범위에 해당하면 파싱안하는 방식? -> ok 이걸로 해보자. 바운드의 contains 작동방식이 어떤지는 모르겠는데..(어떻게 비교하는 것인지)
            //stores나 markers 리스트 비교는 하기가 좀 그렇다. 중복되는 지도부분에 해당하는 json값이라면 파싱 안하는 방식으로. 그럼 중복되는 부분 바운드는 어떻게 만드냐

            //위의 alert를 풀스크린 다이얼로그로 하면 좋을 듯(별도 다이얼로그 뷰로). + 마커 재생산방식에 대하여. +앱 아이콘 등. + 초기 앱 로딩화면
            //+ 넣으려고 하거나 수정하려 했던 기능들 등. 여기에 쓴 주석들 + 메모에 쓴 것들 등.
            //naverMap.getContentBounds().intersects();
            //naverMap.getContentBounds().intersection();
            //위의 두 메소드가 존재하네. 이걸로 만들 수 있을 것 같다. 근데 이렇게 하는게 효용성이 클까.
            //기존의 마커를 지울지 안지울지 결정하는 지역 바운드값은 그냥 이동 후의 새 지도 바운드값으로 해도 되고
            //기존 지도와 새 지도와의 교차지점 바운드 값으로 해도 될 듯 하다.
            //만약에 교차바운드값이 없으면 아예 마커를 다 새로 그리고, 교차지점이 있으면 안지워도 되는 마커는 그대로 두는 방식으로.
            //뷰모델에서는 해당 교차지점 바운드 값을 벗어난 stoer값만 파싱하는 방식.

            marker.setMap(null);    //맵이 지정된 마커는 메인쓰레드에서 제거해야함.   - 원래는 조금만 이동해도 모든 마커 그냥 지우고 전부 새로 그리는 방식이었음.
        }
    }

    private void getMaskInfo(){ //맵 최초로딩 or 카메라 이동시 호출되는 메소드로서 공적마스크 데이터를 가져온다.
        //LatLng coord = naverMap.getCameraPosition().target;     //현재 지도의 중심 좌표값 얻기. (유동) -> oldPosition으로 사용 가능.
        double radius = radiusPixel * projection.getMetersPerPixel();   //반지름을 구성하는 픽셀의 개수(기기별 불변) * 반지름의 픽셀당 미터값 (유동)

        //mapViewModel.getMaskInfo(coord, radius); //통신 시작. 넘겨주는 값은 현재 중심점 좌표와 반경m값.
        mapViewModel.getMaskInfo(oldPosition, radius);
    }

    private void reflectChanges(final List<Store> stores){      //observation callback. 데이터 변경 발생시 호출
        if(stores == null){     //초기 화면 로딩 시 정
            // (초기에 맵이 떴을 때 비동기적으로 약국 정보를 받아오는데, 아직 받아오지 못해서 null인 경우 - ViewModel에서도 해당 값은 초기값으로 null로 설정)
            progressBar.setVisibility(View.VISIBLE);
            return;
        }else if(stores.isEmpty()){     //서버에서 되돌려주는 값이 없는 경우 (지도를 많이 확대해서 해당 구역에 약국이 없다거나, 약국이 없는 지역으로 카메라를 이동시킨 경우)
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "현재 위치에는 판매처가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        //정상적으로 자료를 받은 경우.
        ExecutorService executorService = Executors.newSingleThreadExecutor();  //싱글스레드 받아오기
        executorService.execute(() -> {
            markers = new ArrayList<>();        //데이터를 받아올 때마다 새로 선언해주는 것이 나을 거같다. 데이터가 많아졌다가 적어졌을 때 메모리 적게 잡기 위해.
            for(Store store : stores){          //데이터 받아서 각각 파싱한 store객체 하나당 마커 하나를 생성해주는 반복문이다.
                Marker marker = new Marker();
                marker.setPosition(new LatLng(store.getLat(), store.getLng()));     //마커의 포지션 정보 세팅

                String type = store.getType();      //판매처 종류
                String remain = store.getRemain_stat();     //재고량

                //아이콘 세팅
                int resId;
                if(type.equals("01")){   //약국
                    resId = R.drawable.ic_unselected_pharm;
                }else if(type.equals("02")){     //우체국
                    resId = R.drawable.ic_unselected_post_office;
                }else{      //농협
                    resId = R.drawable.ic_unselected_nh;
                }
                marker.setIcon(OverlayImage.fromResource(resId));       //판매처 유형에 따른 아이콘 세팅


                //크기 설정
                marker.setWidth((int)getResources().getDimension(R.dimen.marker_width_unselected));
                marker.setHeight((int)getResources().getDimension(R.dimen.marker_height_unselected));

                //재고량에 따른 색 설정 및 zIndex(보이는 우선순위) 세팅 -> 겹쳐 보이는 경우 재고량이 많은 가게를 맨 앞에 보이도록 함.
                int color;
                int zIndex;
                switch (remain){
                    case "plenty":
                        //color = Color.GREEN;      기본 제공색의 가시성이 떨어져서 변경.
                        color = R.color.colorPlenty;
                        zIndex = 100;
                        break;
                    case "some":
                        //color = Color.YELLOW;
                        color = R.color.colorSome;
                        zIndex = 80;
                        break;
                    case "few":
                        //color = Color.RED;
                        color = R.color.colorFew;
                        zIndex = 60;
                        break;
                    case "empty":
                        //color = Color.GRAY;
                        color = R.color.colorEmpty;
                        zIndex = 40;
                        break;
                    default:        //판매 중지/완료 - break
                        //color = getResources().getColor(R.color.colorNoSale, null);       //판매중지-완료 색은 일단 보라색 계열로 설정 -> 검정
                        color = R.color.colorNoSale;
                        marker.setCaptionText("판매중지/완료");      //캡션 텍스트
                        marker.setCaptionMinZoom(16);       //16줌레벨 이상 확대해야 캡션 보임
                        zIndex = 0;      //지도에서 겹치는 경우 가장 뒤로 숨김
                        break;
                }
                //marker.setIconTintColor(color);
                marker.setIconTintColor(getResources().getColor(color, null)); //아이콘의 색도 재고량에 따른 색으로 설정
                marker.setZIndex(zIndex);

                marker.setHideCollidedSymbols(true);    //마커와 지도상의 심볼 겹칠 시 지도 심볼 숨기기
                marker.setHideCollidedCaptions(true);   //캡션간 겹칠 시 숨기기

                //marker.setTag(store.getIndex());
                marker.setTag(new MarkerInfo(store.getIndex(), type, true));
                //결국 마커별로 별도의 구분 태그오브젝트 만들게 되었다.. ㅠㅠ -> 기존에는 인덱스 값만 가지도록 하였는데, 마커 클릭시 마커 모양을 변경해줘야 했다.
                //-> 해당 경우에 클릭한 마커의 판매처 유형을 알아야 알맞은 변형 마커로 설정해줄 수 있다. 인덱스 값으로 stores나 markers에서 해당 판매처 유형을 다시 get 할 수도 있긴 했으나..
                //데이터 get 편이성을 위해 위와같이 태그 설정화.
                marker.setOnClickListener(MapFragment.this);        //마커 클릭 시 Overlay.OnClickListener 작동.
                markers.add(marker);        //markers 목록에 marker 등록
            }

            //마커 목록 완성시 메인 쓰레드로 해당 내용 전달.
            handler.post(() -> {                //in main UI Thread
                for(Marker marker : markers){
                    marker.setMap(naverMap);        //마커에 맵 값 설정 (맵에 마커 등록)
                }
                progressBar.setVisibility(View.GONE);
            });

        });

    }

    /**
     * 지도상의 마커를 클릭한 경우 작동하는 콜백 함수.
     * @param overlay 해당 마커 Overlay 객체 (Overlay is Marker's Superclass)
     * @return 이벤트를 지도로 전파할 것인지의 여부. false인 경우 네이버 지도로 전파, true면 전파 안하고 이벤트 소비
     */
    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        Marker oldMarker = infoWindow.getMarker();      //새로운 마커를 클릭했을 때 기존에 이미 클릭한 마커가 있다면? 마커가 눌려있다는 것은 인포윈도우가 살아있음을 의미한다.
        if(oldMarker != null){
            MarkerInfo info = (MarkerInfo)oldMarker.getTag();

            int iconId;
            switch (info.getType()){
                case "01":
                    iconId = R.drawable.ic_unselected_pharm;
                    break;
                case "02":
                    iconId = R.drawable.ic_unselected_post_office;
                    break;
                default:
                    iconId = R.drawable.ic_unselected_nh;
                    break;
            }
            oldMarker.setIcon(OverlayImage.fromResource(iconId));   //기존 마커의 아이콘을 원상태로 만든다. 눌려있던 마커를 다시 누른 경우 포함

            oldMarker.setWidth((int)getResources().getDimension(R.dimen.marker_width_unselected));
            oldMarker.setHeight((int)getResources().getDimension(R.dimen.marker_height_unselected));    //처음 아이콘처럼 돌려놓기.

            infoWindow.close();     //기존 마커의 infoWindow는 닫는다.
            if (oldMarker.equals(overlay)) return true; //새로 누른 마커와 기존 인포 띄워져 있던 마커가 같은경우? -> 기존 마커를 누른 경우이므로 인포만 닫고 종료.
        }
        //infoWindow가 없는데 새롭게 마커를 눌렀거나 다른 마커를 누른 경우 아래로 진행.

        //int index = (int)overlay.getTag();              //tag값 == Markers에서 marker의 위치값 == stores에서 store의 위치값.
        //Store store = mapViewModel.getStores().getValue().get(index);   //해당 태그를 이용하여 마커에 대한 viewModel 내 store 값 가져오기.
        //LatLng coord = new LatLng(store.getLat(), store.getLng());
        //CameraUpdate cameraUpdate = CameraUpdate.scrollTo(coord).animate(CameraAnimation.Easing);
        //naverMap.moveCamera(cameraUpdate);      //클릭한 마커로 카메라 중심점 이동
        //해당 지점으로 이동시 infoWindow 종료 문제 발생. 아마 위치 이동시 마커 다시 그리는데, 그러면서 윈도우도 닫혀버리는 것 같다.
        //흠.. 그러면 이동기능은 살리는 대신 기존 마커와 새 마커에 대해 Store 중복시 마커 새로 그리지 않도록 알고리즘같은거 만들어줘야 할거같은데. <- 이게 가장..나아보이는데.
        //좌표값을 이용하여 중복 체크하든.. Stores 를 이용하든(ViewModel List)
        //아니면 InfoWindow가 맵 중앙에 대해서만 띄워지도록 만들어도 되긴 할 듯.(마커가 아닌 좌표값을 기준으로 만들어지도록)
        //그러면 마커가 사라졌다 새로 만들어져도 윈도우가 꺼지지는 않는다. 또는 아예 이 InfoWindow를 쓰지말고 별도의 팝업창과 같은 것을 이용할 수도 있을 듯.(네이버맵처럼)
        //아니면 마커끼리 비교해서 중복마커만 선별해놓을 수 있나..? 중복마커만 살려놓는 방식으로.. 또는 네이버나 카카오처럼 별도의 '이 영역에서 찾기' 할 수도 있음..

        //태그를 재정의 함으로서 다시 작성.
        MarkerInfo info = (MarkerInfo)overlay.getTag(); //새로 누른 마커에 대한 정보 가져오기.
        Store store;
        try {
            store = mapViewModel.getStores().getValue().get(info.getIndex());
        }catch (Exception e){   //간헐적으로 ArrayList에 대한 IndexOutOfBoundsException이 뜨는 것으로 보고되었는데 왜지??
            new MaterialAlertDialogBuilder(getContext()).setTitle(R.string.error_alert_title).setMessage(R.string.error_alert_message)
                    .setPositiveButton(R.string.confirm, ((dialog, which) -> {})).show();   //우선은 임시방편처리를 하였는데, 확인 후 변경이 필요할 듯. + 오류 보고 기능
            return true;    //오류 발생 시 이벤트 처리 중단. 마커 아이콘 변경 및 infoWindow 처리하지 않음.
        }
        LatLng coord = new LatLng(store.getLat(), store.getLng());
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(coord).animate(CameraAnimation.Easing);
        //naverMap.moveCamera(cameraUpdate); //보류.  -> 누른 마커를 지도의 중앙에 띄우려 했는데 CameraIdle 작동으로 인해 지도는 옮겨지는데 맵 마커 재로딩으로 인해
        //infoWindow가 뜨지 않음.

        //클릭한 마커모양 변경 및 정보창 띄우기.
        Marker marker = (Marker)overlay;

        /*
        이렇게 해서 마커 다시 원래대로 되돌릴때도 store얻은 다음에 .getType으로 어떤 마커였는지 다 확인하고 다시 작업하는 그러는 것보다는 Tag에 타입값을 넣어두는 것이 좋을 것 같다.
        int resId;
        switch (store.getType()){
            case "01":
                resId = R.drawable.ic_marker_pharm;
                break;
            case "02":
                resId = R.drawable.ic_marker_post_office;
                break;
            default:
                resId = R.drawable.ic_marker_nh;
                break;
        }
        marker.setIcon(OverlayImage.fromResource(resId));           //다른 마커 클릭하거나 지도 부분 클릭시 원래대로 되돌리는 기능 필요.
        */
        int iconId;
        switch (info.getType()){
            case "01":
                iconId = R.drawable.ic_marker_pharm;
                break;
            case "02":
                iconId = R.drawable.ic_marker_post_office;
                break;
            default:
                iconId = R.drawable.ic_marker_nh;
                break;
        }
        marker.setIcon(OverlayImage.fromResource(iconId));

        marker.setWidth((int)getResources().getDimension(R.dimen.marker_width));
        marker.setHeight((int)getResources().getDimension(R.dimen.marker_height));          //누른 마커는 모양이 다르고 크기는 조금 더 큼.

        adapter.setStore(store);        //infoWindow에 해당 마커의 정보를 띄우기 위해 마커의 인덱스를 가지고 해당 데이터를 찾은 뒤 adapter에 설정해줌.
        if(marker.getInfoWindow() != null){
            infoWindow.close();         //마커에 대해 정보창이 이미 열려있을 때 마커를 한번 더 누르면 정보창 닫기.
            //oldMarker를 이용한 처리로 이 if종속문은 없어도 됨.
        }else{
            infoWindow.open(marker);        //해당 마커에 대해 정보창 띄우기.

            /*
            //infoWindow가 지도 바깥쪽에 생길 때 잘리는 현상 발생. 따라서 infoWindow에 offset을 두어 온전하게 보이게 하려고 함.
            int[] outLocation = new int[2];
            mapView.getLocationOnScreen(outLocation);   //mapView의 화면상 시작점 절대좌표 얻기

            float centerX = (outLocation[0] + mapView.getMeasuredWidth())/2.0f; //중심 x좌표 얻기 - mapView.getMeasuredWidth()는 naverMap.getWidth()로 대체 가능
            float centerY = (outLocation[1] + mapView.getMeasuredHeight())/2.0f;//중심 y좌표 얻기 - mapView.getMeasuredHeight()는 naverMap.getHeight()로 대체 가능

            PointF markerScreenLoc = projection.toScreenLocation(coord);    //마커의 좌표를 스크린 상 좌표로 변환.
            //adapter.getView를 이용해 View객체를 얻을 수도 있지만 굳이 그러지는 않음.
            int infoWidth = adapter.getViewWidth();
            int infoHeight = adapter.getViewHeight();       //infoWindow View의 크기 가져옴.

            PointF defaultAnchorPoint = infoWindow.getAnchor(); //기본 앵커 위치값 얻어오기(가운데 아래)
            if(markerScreenLoc.x < centerX){    //마커가 X좌표 기준 스크린 중심 왼쪽에 있는 경우
                int offsetX =  (int)markerScreenLoc.x - infoWidth/2;    //마커의 x좌표에서 infoWindow의 절반만큼을 빼본다.
                if(offsetX < 0){    //offsetX가 0보다 작다는 것은 화면 왼쪽으로 infoWindow가 넘어간 것.
                    infoWindow.setOffsetX(-offsetX); //오른쪽으로 밀어주어야 하므로 -1을 곱한 값 사용.
                    defaultAnchorPoint.x += offsetX;    //앵커포인트는 그만큼 다시 왼쪽으로 밀어주어야 함.
                }
            }else{  //마커가 x좌표 기준 스크린 중심 오른쪽에 있는 경우.

            }
            infoWindow.setAnchor(defaultAnchorPoint);
            -> infoWindow 위치는 이동이 되긴 하는데 말풍선의 말 꼬리부분이 계속 중앙부분이어서 적합하지 않다. Anchor가 말풍선 꼬리인줄 알았는데 아니었고..
            Anchor나 offset설정이나 비슷한 설정이다.. 따라서 이걸 쓰려면 꼬리부분이 다른 Custom 말풍선을 써야 할 것 같다.
            -> 그냥 이 상태에서 검색 및 즐겨찾기 기능부터 구현하거나 BottomNav를 측면 Navigation으로 바꾸고 지도의 padding을 없앤 뒤 지도를 전면에 다 띄운다.
            이후에 마커 클릭시 뜨는 infoWindow를 BottomSheet와 같은 별도의 dialog형식으로 정의하고..아래부분이 가려질테니 검색 반경을 조금 줄여야 할지도 - 줄이거나 중심점을 살짝 위로 잡거나)
            그 BottomSheet에서 즐겨찾기 기능을 넣는게 좋을 것 같다.. (누른 마커지점으로 지도 카메라 이동은 그 이후에 고려해볼 문제)
            ActionBar는 없애고 햄버거 버튼만 동그라미로 남겨두거나 .. ActionBar를 남기는 경우 오른쪽 위에 검색버튼 두면 될 듯.
            (검색버튼도 동그라미 버튼으로 FloatingActionButton으로 두어도 될 것 같다. 그래서 지도를 전면 다 차게 만들고, 왼쪽 위에 메뉴버튼, 오른쪽 위에 검색버튼만 보이게.)
            구매 대상자 알림은 적절한 곳으로 옮기고(메뉴 버튼 누르면 navView 아래쪽에 보이게 한다던지..) NavigationView의 상단 이미지는 그래픽 이미지를 수정 이용하면 될 것 같다.
            왼쪽 부분에 앱 아이콘 크게 놓고 오른쪽에 마스크 레이더 이름 놓고 아래에 공적마스크 ~~ 내용 작게 넣고.
            그리고 설정 Fragment와 아이콘 및 이름은 숨겨놓고 물음표 기호와 About Fragment로 하는게 나을 것 같다. 설정 할 것이 없으므로. (한글 명으로는 앱에 대하여)
            + 제안하기 또는 문의하기 버튼도.. 아래쪽에?
            만약 설정할 것이 필요하여 설정화면을 만들어야 한다면 Android Studio에서 제공해주는 SettingFragment를 쓰는게 나을 듯
            즐겨찾기 화면의 등록해둔 약국의 재고 확인(또는 알람)의 동작 방식은 그 이후에 생각하자. 공공데이터의 다른 API를 쓰거나 서버를 구축,.?
            똑같은 API를 쓴다면.. 해당 약국 고유번호와 좌표 기억해뒀다가 재쿼리 한 후(반경은 좁게) 그 후에 고유번호 같은 데이터만 걸러서 즐겨찾기 화면에 Text값으로 출력.
            내 주변 약국 보기 목록같은것도 있으면 좋고.... (검색반경 조절 가능하게 한다던가. 내 위치와의 거리도 표시한다던가.)
             */

        }

        //infoWindow.setOnClickListener();      정보창 클릭 리스너.

        //해당 마커 지점 가게의 상세 정보 띄우기. - 단순히 store 정보 뿐만이 아니라 네이버 DB등도 이용 가능하면 좋을텐데.. 더 상세하게 띄우는거 그런 것도 될까.
        //네이버에서 제공 하면 가능 할 듯 싶은데.. 아니면 해당 약국 코드를 가지고 공공 데이터로 한번 더 fetch?
        //1. 네이버 지도 자체에서 클릭하면 뜨는 것 이용. 2. 또는 네이버 지도에서 제공하는 거 다른 거 있으면 이용 3.네이버 검색 DB 이용. 4. 네이버 다른 DB 이용
        //5. 건강보험 심사평가원 자료 이용. 6. 국립중앙병원 DB이용. 등 네이버 & 공공데이터 포털 등
        //네이버에 등록된 지역별 업체 및 상호 조회 검색 api? + 지도에 있는 해당 좌표 거리뷰 반환. -> 거리뷰는 web api 써야 할 듯. 약국 정보는Open API 이용?
        //-> 내가 원하는 방식의 제공 API가 안보인다. .

        //알람 서비스는 사설 서버가 있어야 하나..? + 모달창으로 여러 공지들 필요.  +지도 줌아웃 제한 필요. + 입고, 업데이트 null 존재함.
        //클릭 전후 마커 모양 다른 것도 해줘야 함. + 학교 과제.. ㅠ  + 길찾기 기능? + 현재 위치 기능. 등.


        //마커를 기본 원모양으로 만들고 물방울 모양 둔 다음에 기본값은 원모양으로, 마커 하나 누르면 물방울 모양으로 바뀌게 하고, 다른 마커나 지도 다른 부분 누르면
        //원래의 원모양으로 바뀌게 한다던가.. 다른 마커 누르면 원래 원모양이 되게 나을 듯.

        //그리고 클릭한 마커 지점에 대해서만 캡션으로 지점 이름, 서브캡션으로 재고량 표시되도록 하자. 모두 다 표시하는 것보다는 그게 깔끔할 듯.

        return true;    //이벤트 소비(지도로 전파 x) -> 지도로 전파시 온전히 작동하지 않는것으로 보임.
    }

    @Override
    public void onClick(View v) {   //InfoView 즐겨찾기 버튼 클릭 콜백 리스너
        //Room 사용 예정.
        //추가한 약국에 대한 정보를 DB에 저장하고.. 흠. Favorite Fragment에서 해당 약국에 대한 정보는 어떻게 가져올까.
        //약국 고유 코드번호를 통해 get하는 API가 없기때문에 해당 좌표를 저장해놓고, 해당 좌표 기준으로 (반경은 0m?) 다시 가져온 뒤 데이터 검증(코드 일치여부 검사)
        //후에 재고량 등 표시하는 방식...이 일단 최선일 것 같다.
        //해당 약국 클릭 시 지도에서 해당 위치로 이동하는 기능까지 추가.
        db = AppDatabase.getInstance(getContext());
        MarkerInfo info = (MarkerInfo) infoWindow.getMarker().getTag();     //현재 열려있는 인포윈도우를 통해 마커의 정보를 가져옴.
        Store store = mapViewModel.getStores().getValue().get(info.getIndex()); //마커의 Tag에는 약국의 인덱스 값과 타입값만 있기 때문에 별도로 get


        String code = store.getCode();
        int isExist = db.pharmDao().isExist(code);
        String result;
        if(isExist == 1){       //만약에 즐겨찾기에 이미 있고 그것을 삭제하는 것이라면
            db.pharmDao().deleteSpecifiedPharm(code);       //code를 이용해 특정 튜플을 삭제하는 것으로 충분하다.
            result = "즐겨찾기에서 삭제 완료";
            //이후 아이콘 변경 필요 -> LiveData?
        }else{      //만약에 새로 즐겨찾기에 추가하고자 한다면
            Pharm pharm = new Pharm();

            pharm.code = store.getCode();
            pharm.addr = store.getAddr();
            pharm.createdAt = store.getCreated_at();
            pharm.remainStat = store.getRemain_stat();
            pharm.lat = store.getLat();
            pharm.lng = store.getLng();
            pharm.name = store.getName();
            pharm.stockAt = store.getStock_at();

            db.pharmDao().insertPharm(pharm);
            result = "즐겨찾기에 추가 완료";
            //이후 아이콘 변경 필요. 그리고 잘 됐는지 그 결과값을 이용하여 처리해주는 것도 있으면 더 좋을 듯.
        }
        Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
        infoWindow.invalidate();    //이걸로 아이콘 변경이 될까?


        db.close();
        //테스팅 필요. + FavoriteFragment에서 불러와주는 코드 추가 필요. 

        //Pharm DTO와 Store객체를 통합하면 어떨까.
    }

    private class CustomInfoViewAdapter extends InfoWindow.DefaultViewAdapter{       //마커를 눌렀을 시 나오는 InfoWindow에 대한 CustomAdapter
        private Store store;

        private MaterialTextView statusView;    //재고량 상태에 따라 변하는 색상패드
        private MaterialTextView storeName;     //가게 이름
        private MaterialTextView stockStatus;   //재고량 상태 글자
        private MaterialTextView updateTime;    //업데이트 시간
        private MaterialTextView receiveTime;   //물품 받은 시각
        private MaterialTextView address;       //주소정보

        private MaterialTextView updateIcon;    //업데이트 시간 옆 아이콘
        private MaterialTextView receiveIcon;   //물품 받은 시간 옆 아이콘

        private View infoView;              //Databinding 고려
        MaterialButton buttonAddFavorite;
        CustomInfoViewAdapter(Context context, MapView mapView){
            super(context);
            infoView = LayoutInflater.from(getContext()).inflate(R.layout.info_view_custom, mapView, false);
            //adapter 생성시 view inflate

            statusView = infoView.findViewById(R.id.status_view);
            storeName = infoView.findViewById(R.id.store_name);
            stockStatus = infoView.findViewById(R.id.stock_status);
            updateTime = infoView.findViewById(R.id.update_time);
            receiveTime = infoView.findViewById(R.id.receive_time);
            address = infoView.findViewById(R.id.address);

            updateIcon = infoView.findViewById(R.id.update_icon);
            receiveIcon = infoView.findViewById(R.id.receive_icon);
            buttonAddFavorite = infoView.findViewById(R.id.button_add_favorite);
            buttonAddFavorite.setOnClickListener(MapFragment.this);
        }


        void setStore(Store store){
            this.store = store;
        }

        public Store getStore(){
            return store;
        }

        int getViewWidth(){
            return (infoView==null)? 0: infoView.getWidth();
        }

        int getViewHeight(){
            return (infoView==null)? 0 : infoView.getHeight();
        }

        @NonNull
        @Override
        protected View getContentView(@NonNull InfoWindow infoWindow) {     //ViewStub?같은거 쓸 수 있나.
            //LayoutInflater inflater = LayoutInflater.from(getContext());
            //View infoView = inflater.inflate(R.layout.info_view_custom, root, false);
            //누를 때마다 계속 inflate 시키는게 과연 좋을까..? inflate하는 부분을 생성자에다 놓아둘까? 최초 한번만 inflate 시키도록. -> ok 잘 된다.

            /*
            아래 바인딩 구문들도 모두 생성자로 이전.
            statusView = infoView.findViewById(R.id.status_view);
            storeName = infoView.findViewById(R.id.store_name);
            stockStatus = infoView.findViewById(R.id.stock_status);
            updateTime = infoView.findViewById(R.id.update_time);
            receiveTime = infoView.findViewById(R.id.receive_time);
            address = infoView.findViewById(R.id.address);

            updateIcon = infoView.findViewById(R.id.update_icon);
            receiveIcon = infoView.findViewById(R.id.receive_icon);     //아이콘 색도 재고 상태에 따라 변경 필요함.. <- 근데 왜 id 조회가 안되냐.
             */

            int color;
            String status;
            switch (store.getRemain_stat()){
                case "plenty":
                    //color = Color.GREEN;
                    color = R.color.colorPlenty;
                    status = "충분(100개 이상)";
                    break;
                case "some":
                    //color = Color.YELLOW;
                    color = R.color.colorSome;
                    status = "어느 정도 있음(30개 이상)";
                    break;
                case "few":
                    //color = Color.RED;
                    color = R.color.colorFew;
                    status = "부족(30개 미만)";
                    break;
                case "empty":
                    //color = Color.GRAY;
                    color = R.color.colorEmpty;
                    status = "없음(품절)";
                    break;
                default:
                    //color = Color.BLACK;
                    color = R.color.colorNoSale;
                    status = "판매 중지";
                    break;
            }

            //statusView.setBackgroundColor(color);
            stockStatus.setText(status);
            //stockStatus.setTextColor(color);
            int newColor = getContext().getResources().getColor(color, null);
            statusView.setBackgroundColor(newColor);
            stockStatus.setTextColor(newColor);

            updateIcon.getCompoundDrawables()[0].setTint(newColor);     //0 means left compound drawable icon
            receiveIcon.getCompoundDrawables()[0].setTint(newColor);

            storeName.setText(store.getName());
            updateTime.setText(store.getCreated_at());
            receiveTime.setText(store.getStock_at());

            String addr = store.getAddr();
            int idx = addr.indexOf("(");
            if(idx != -1) addr = addr.substring(0, idx);    //주소부분에서 괄호 설명부분은 생략
            address.setText(addr);


            //누른 약국이 즐겨찾기에 이미 존재하는지 안하는지에 따른 아이콘 색 변경

            //db 접근은 UI와 별개의 쓰레드에서 작업해주어야 한다.
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(()->{
                db = AppDatabase.getInstance(getContext());
                int isExist = db.pharmDao().isExist(store.getCode());
                if(isExist == 1) handler.post(()-> buttonAddFavorite.setIcon(getResources().getDrawable(R.drawable.ic_star_24px, null)));
                db.close();
            });



            return infoView;
            //이 방식은 마커가 클릭될 때마다 미리 setStore로 adapter 내의 변수 값을 바꿔주는 방식을 취했다. 이 방법 말고 getContentView에 들어오는 매개변수인 infoWindow를 이용하여
            //infoWindow.getMarker().getTag()를 이용해 클릭된 마커에 대한 정보를 가져올 수도 있다.
        }
    }

    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {    //맵의 다른 부분을 그냥 클릭한 경우 infowindow 닫고 마커도 원래 모양으로 되돌린다.
        Marker marker = infoWindow.getMarker();
        if(marker != null){
            MarkerInfo info = (MarkerInfo)marker.getTag();

            int iconId;
            switch (info.getType()){
                case "01":
                    iconId = R.drawable.ic_unselected_pharm;
                    break;
                case "02":
                    iconId = R.drawable.ic_unselected_post_office;
                    break;
                default:
                    iconId = R.drawable.ic_unselected_nh;
                    break;
            }
            marker.setIcon(OverlayImage.fromResource(iconId));

            marker.setWidth((int)getResources().getDimension(R.dimen.marker_width_unselected));
            marker.setHeight((int)getResources().getDimension(R.dimen.marker_height_unselected));

            infoWindow.close();
        }
    }

    //나중에는 즐겨찾기 기능, 재고 들어올 시 알람기능 등..

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        //사용자가 이 지도 화면을 떠날 때 위치를 기억해 두었다가 다음에 다시 돌려주기 위한 부분.
        if(pref == null) pref = getContext().getSharedPreferences(getString(R.string.preference_map_fragment), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        LatLng lastPosition = naverMap.getCameraPosition().target;
        editor.putFloat("latitude", (float) lastPosition.latitude);
        editor.putFloat("longitude", (float) lastPosition.longitude);
        editor.apply();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if(requestQueue != null){
            //requestQueue.cancelAll();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}


//일단 필요한 추가작업
//GPS기능, 이전에 종료한 지점에서 다시 지도가 로드되도록 변경. 마커 그림이 클릭시 바뀌도록 변경. 다시 누를 경우 원래대로 돌아가도록 하는 것 또한.
//마커 클릭시 해당 마커가 중심으로 가도록 변경.
//입고시간 및 업데이트 시간에 대한 정규식같은걸 통해서 좀 더 보기 편하게 (날짜를), 업데이트 및 입고시간에 대한 아이콘이 안띄워지는 문제 해결.
//다른 프래그먼트 부분 변경. 등등등.. 라이센스 표기 및 쓰인 폰트 얘기하기 등.