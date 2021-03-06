package com.jh.mask_radar.ui.favorite;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.jh.mask_radar.R;
import com.jh.mask_radar.databinding.FavoriteInfoViewBinding;
import com.jh.mask_radar.databinding.FragmentFavoriteBinding;
import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;

import java.util.List;

public class FavoriteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MaterialButton.OnClickListener{
    //일부 기종에서 연속으로 즐겨찾기 추가시 앱 비정상종료 이슈 발생중
    //즐겨찾기 화면에서 햄버거 메뉴바 클릭 시 업데이트 버튼하고 동일하게 동작하는 이슈 발생중 -> 해결
    //즐겨찾기 화면 refresh layout의 민감도가 지나치게 큼   - 해결
    //라이센스 화면의 디자인 변경 필요
    //선택적 접근권한 부분 알림 추가 필요 - Modal Popup?
    //Drawer Nav 중 즐겨찾기 메뉴가 잠깐잠깐 비활성화 될 때가 있음.

    private FragmentFavoriteBinding favoriteBinding;
    private FavoriteViewModel favoriteViewModel;
    private AppDatabase db;
    private RequestQueue requestQueue;
    private FavoriteAdapter adapter;
    private boolean shoudWait;
    private BlockUpdateTimer timer;

    private static final double MAX_SWIPE_DISTANCE_FACTOR = 0.6;    //swipe refresh layout의 기본값
    private static final int REFRESH_TRIGGER_DISTANCE = 150;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(getContext());
        favoriteViewModel =
                new ViewModelProvider(this).get(FavoriteViewModel.class);
        requestQueue = Volley.newRequestQueue(requireContext());
        favoriteViewModel.setQue(requestQueue, getString(R.string.MASK_URL));   //http요청을 위한 Volley Que를 Viewmodel로 넘김
        favoriteViewModel.fetchByRoom(db);                               //이렇게 db 객체를 넘겨줘도 괜찮을까? viewModel에서는 지역변수로만 쓰게 하면 괜찮지 않을까?

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favoriteBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false);
        setHasOptionsMenu(true);       //Refresh Layout과는 별개의 업데이트 버튼

        favoriteViewModel.getPharms().observe(getViewLifecycleOwner(), (pharms -> {
            //store값 변화시 UI 업데이트
            if(pharms == null){      //초기 로딩 시
                favoriteBinding.favoriteProgressBar.setVisibility(View.VISIBLE);
                favoriteBinding.favoriteSwipeRefreshLayout.setEnabled(false);
            }else if(pharms.size() == 0){                   //즐겨찾기 한 약국이 없을 시
                favoriteBinding.textNoFavorite.setText(getString(R.string.favorite_no_stores));
                favoriteBinding.favoriteProgressBar.setVisibility(View.GONE);
                //즐겨찾기를 모두 삭제시킨 경우 이 부분이 나중에 작동될 수도 있다.
                favoriteBinding.favoriteSwipeRefreshLayout.setEnabled(false);
                favoriteBinding.favoriteRecyclerview.setVisibility(View.INVISIBLE);
            }else {                                      //즐겨찾기 한 약국 표시해주기
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                manager.setOrientation(RecyclerView.VERTICAL);
                favoriteBinding.favoriteRecyclerview.setLayoutManager(manager);
                adapter = new FavoriteAdapter();
                adapter.pharms = pharms;
                favoriteBinding.favoriteRecyclerview.setAdapter(adapter);
                favoriteBinding.favoriteProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                favoriteBinding.favoriteSwipeRefreshLayout.setEnabled(true);
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                int mDistanceToTriggerSync = (int) Math.min(
                        ((View) container.getParent()) .getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
                favoriteBinding.favoriteSwipeRefreshLayout.setDistanceToTriggerSync(mDistanceToTriggerSync);    //swipe 민감도 설정
                favoriteBinding.favoriteSwipeRefreshLayout.setRefreshing(false);
                shoudWait = true;
                timer.start();
            }
        }));

        return favoriteBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoriteBinding.favoriteSwipeRefreshLayout.setOnRefreshListener(this);
        favoriteBinding.setLifecycleOwner(this);
        favoriteBinding.setFavoriteViewModel(favoriteViewModel);    //데이터 바인딩용
        timer = new BlockUpdateTimer(30000, 30000); //onTick은 쓰지 않을 것이므로 두번째 인자는 첫번째와 동일하게 줌.
    }

    @Override
    public void onRefresh() {
        favoriteBinding.favoriteSwipeRefreshLayout.setRefreshing(true);
        if(shoudWait){
            Toast.makeText(getContext(), getString(R.string.block_update), Toast.LENGTH_LONG).show();
            favoriteBinding.favoriteSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        favoriteViewModel.refreshList(db);        //do real update in viewModel
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorite_update_menu, menu);
        //Drawable icon = menu.getItem(0).getIcon();
        //icon.setColorFilter(getResources().getColor(R.color.colorNoSale, null), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        /*
        switch (id){
            case R.id.favorite_update_menu_icon:
                if(!refreshLayout.isEnabled()){
                    Toast.makeText(getContext(), getString(R.string.favorite_update_menu_icon_alert), Toast.LENGTH_SHORT).show();
                }else onRefresh();
                return true;
        }
         */
        if(id != R.id.favorite_update_menu_icon) return super.onOptionsItemSelected(item);  //햄버거 버튼이 눌린 경우 기존의 동작 수행
        if(!favoriteBinding.favoriteSwipeRefreshLayout.isEnabled()){   //업데이트 할 데이터가 없는 경우
            Toast.makeText(getContext(), getString(R.string.favorite_update_menu_icon_alert), Toast.LENGTH_SHORT).show();
        }else onRefresh();
        return true;
    }


    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>{
        private List<Pharm> pharms;

        private class FavoriteViewHolder extends RecyclerView.ViewHolder{
            FavoriteInfoViewBinding infoViewBinding;

            FavoriteViewHolder(@NonNull FavoriteInfoViewBinding infoViewBinding) {
                super(infoViewBinding.getRoot());
                this.infoViewBinding = infoViewBinding;
            }
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FavoriteViewHolder(FavoriteInfoViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
            Pharm pharm = pharms.get(holder.getAdapterPosition());
            holder.infoViewBinding.setPharm(pharm);

            String status;
            int color;
            switch (pharm.remainStat){
                case "plenty":
                    color = R.color.colorPlenty;
                    status = "충분(100개 이상)";
                    break;
                case "some":
                    color = R.color.colorSome;
                    status = "어느 정도 있음(30개 이상)";
                    break;
                case "few":
                    color = R.color.colorFew;
                    status = "부족(30개 미만)";
                    break;
                case "empty":
                    color = R.color.colorEmpty;
                    status = "없음(품절)";
                    break;
                default:
                    color = R.color.colorNoSale;
                    status = "판매 중지";
                    break;
            }

            //holder.stockStatus.setText(status);
            holder.infoViewBinding.favoriteStockStatus.setText(status);
            int newColor = requireContext().getResources().getColor(color, null);
            holder.infoViewBinding.favoriteStatusView.setBackgroundColor(newColor);
            holder.infoViewBinding.favoriteStockStatus.setTextColor(newColor);

            holder.infoViewBinding.setColor(newColor);


            //holder.infoViewBinding.favoriteUpdateIcon.getCompoundDrawables()[0].setTint(newColor);     //0 means left compound drawable icon
            //holder.infoViewBinding.favoriteReceiveIcon.getCompoundDrawables()[0].setTint(newColor);
            //현재 모든 즐겨찾기 아이콘 색이 동일함. 수정 필요...갑자기 왜..
            //DrawableLeft -> DrawableStart로 변경 뒤 오류 발생중!!! - 해결
            //holder.infoViewBinding.favoriteUpdateIcon.getCompoundDrawablesRelative()[0].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(requireContext(), color), PorterDuff.Mode.SRC_IN));
            //holder.infoViewBinding.favoriteReceiveIcon.getCompoundDrawablesRelative()[0].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(requireContext(), color), PorterDuff.Mode.SRC_IN));

            //holder.storeName.setText(pharm.name);
            //holder.updateTime.setText(pharm.createdAt);
            //holder.receiveTime.setText(pharm.stockAt);

            String addr = pharm.addr;
            int idx = addr.indexOf("(");
            if(idx != -1) addr = addr.substring(0, idx);    //주소부분에서 괄호 설명부분은 생략
            //holder.address.setText(addr);
            holder.infoViewBinding.favoriteAddress.setText(addr);

            holder.infoViewBinding.favoriteDeleteButton.setIconTintResource(color);
            holder.infoViewBinding.favoriteDeleteButton.setTextColor(newColor);

            holder.infoViewBinding.favoriteDeleteButton.setOnClickListener(FavoriteFragment.this);
        }

        @Override
        public int getItemCount() {
            return pharms.size();
        }
    }

    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder holder = favoriteBinding.favoriteRecyclerview.findContainingViewHolder(v);
        if(holder==null) {
            Toast.makeText(getContext(), "삭제 처리에 실패하였습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        int posistion = holder.getAdapterPosition();
        favoriteViewModel.deletePharm(db, posistion);
    }

    private class BlockUpdateTimer extends CountDownTimer{

        BlockUpdateTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            //nothing to do
        }

        @Override
        public void onFinish() {
            shoudWait = false;
        }
    }

    @BindingAdapter("bind:item")
    public static void bindItem(RecyclerView recyclerView, LiveData<List<Pharm>> pharms){
        FavoriteAdapter adapter = (FavoriteAdapter)recyclerView.getAdapter();
        if(adapter!=null){
            adapter.pharms = pharms.getValue();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppDatabase.destroyInstance();
        requestQueue = null;
        favoriteViewModel.destroyThread();
        timer.cancel();
    }

    @BindingAdapter("app:drawableTint")
    public static void setDrawableTint(MaterialTextView v, int color){
        //v.getCompoundDrawablesRelative()[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
        Log.e("drawable 개수 확인: ", v.getCompoundDrawablesRelative().length + "");    //4개가 나온다. 즐겨찾기 화면에 뜨는 목록의 총 개수가 4개인데...
        //에시당초 1개만 뜰 것으로 예상하고 한건데.. 즉 4개의 목록이 보이면 8개의 Log가 뜨길 바랬다. 하나의 목록당 아이콘은 2개이므로.
        //왜 아이템 각각에 대한 호출이 되지 않는거지??
        //v.setTextColor(color);    - 이거는 각각의 아이템에 대해 잘 설정 되는 것으로 보아.. compoundDrawable을 쓰면 안될 것 같다.
        v.setCompoundDrawableTintList(ColorStateList.valueOf(color));       //해결 완료.
    }
}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
//Fragment들 생성방식 바꿔야 함. getInstance로 만들어지게
