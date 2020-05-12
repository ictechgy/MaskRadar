package com.jh.mask_radar.ui.favorite;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.jh.mask_radar.R;
import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;

import java.util.List;

public class FavoriteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MaterialButton.OnClickListener{

    private FavoriteViewModel favoriteViewModel;
    private ProgressBar progressBar;
    private AppDatabase db;
    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refreshLayout;
    private FavoriteAdapter adapter;
    private boolean shoudWait;
    private BlockUpdateTimer timer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppDatabase.destroyInstance();
        requestQueue = null;
        favoriteViewModel.destroyThread();
        timer.cancel();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        favoriteViewModel =
                new ViewModelProvider(this).get(FavoriteViewModel.class);
        requestQueue = Volley.newRequestQueue(getContext());
        favoriteViewModel.setQue(requestQueue, getString(R.string.MASK_URL));
        favoriteViewModel.fetchByRoom(db);                               //이렇게 db 객체를 넘겨줘도 괜찮을까? viewModel에서는 지역변수로만 쓰게 하면 괜찮지 않을까?

        View root = inflater.inflate(R.layout.fragment_favorite, container, false);
        final TextView textView = root.findViewById(R.id.text_no_favorite);
        recyclerView = root.findViewById(R.id.favorite_recyclerview);

        progressBar = root.findViewById(R.id.favorite_progressBar);

        favoriteViewModel.getPharms().observe(getViewLifecycleOwner(), (pharms -> {
            //store값 변화시 UI 업데이트
            if(pharms == null){      //초기 로딩 시
                progressBar.setVisibility(View.VISIBLE);
                refreshLayout.setEnabled(false);
            }else if(pharms.size() == 0){                   //즐겨찾기 한 약국이 없을 시
                textView.setText(getString(R.string.favorite_no_stores));
                progressBar.setVisibility(View.GONE);
                //즐겨찾기를 모두 삭제시킨 경우 이 부분이 나중에 작동될 수도 있다.
                refreshLayout.setEnabled(false);
                recyclerView.setVisibility(View.INVISIBLE);
            }else {                                      //즐겨찾기 한 약국 표시해주기
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                manager.setOrientation(RecyclerView.VERTICAL);
                recyclerView.setLayoutManager(manager);
                adapter = new FavoriteAdapter();
                adapter.pharms = pharms;
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                refreshLayout.setEnabled(true);
                refreshLayout.setRefreshing(false);
                shoudWait = true;
                timer.start();
            }
        }));

        refreshLayout = root.findViewById(R.id.favorite_swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        timer = new BlockUpdateTimer(30000, 30000); //onTick은 쓰지 않을 것이므로 두번째 인자는 첫번째와 동일하게 줌.

        return root;
    }



    @Override
    public void onRefresh() {
        if(shoudWait){
            Toast.makeText(getContext(), getString(R.string.block_update), Toast.LENGTH_LONG).show();
            refreshLayout.setRefreshing(false);
            return;
        }
        refreshLayout.setRefreshing(true);
        favoriteViewModel.refreshList(db);        //do real update in viewModel
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorite_update_menu, menu);
        //Drawable icon = menu.getItem(0).getIcon();
        //icon.setColorFilter(getResources().getColor(R.color.colorNoSale, null), PorterDuff.Mode.SRC_ATOP);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.favorite_update_menu_icon:
                if(!refreshLayout.isEnabled()){
                    Toast.makeText(getContext(), getString(R.string.favorite_update_menu_icon_alert), Toast.LENGTH_SHORT).show();
                }else onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    이건 여기서 하는게 아닌듯.
     */

    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>{
        private List<Pharm> pharms;

        private class FavoriteViewHolder extends RecyclerView.ViewHolder{
            private MaterialTextView stockStatus;
            private View statusView;
            private MaterialTextView updateIcon;
            private MaterialTextView receiveIcon;
            private MaterialTextView storeName;
            private MaterialTextView updateTime;
            private MaterialTextView receiveTime;
            private MaterialTextView address;
            private MaterialButton deleteButton;
            FavoriteViewHolder(@NonNull View itemView) {
                super(itemView);
                stockStatus = itemView.findViewById(R.id.favorite_stock_status);
                statusView = itemView.findViewById(R.id.favorite_status_view);
                updateIcon = itemView.findViewById(R.id.favorite_update_icon);
                receiveIcon = itemView.findViewById(R.id.favorite_receive_icon);
                storeName = itemView.findViewById(R.id.favorite_store_name);
                updateTime = itemView.findViewById(R.id.favorite_update_time);
                receiveTime = itemView.findViewById(R.id.favorite_receive_time);
                address = itemView.findViewById(R.id.favorite_address);
                deleteButton = itemView.findViewById(R.id.favorite_delete_button);
            }
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_info_view, parent, false);
            return new FavoriteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
            Pharm pharm = pharms.get(holder.getAdapterPosition());

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

            holder.stockStatus.setText(status);
            int newColor = getContext().getResources().getColor(color, null);
            holder.statusView.setBackgroundColor(newColor);
            holder.stockStatus.setTextColor(newColor);

            holder.updateIcon.getCompoundDrawables()[0].setTint(newColor);     //0 means left compound drawable icon
            holder.receiveIcon.getCompoundDrawables()[0].setTint(newColor);
            //현재 모든 즐겨찾기 아이콘 색이 동일함. 수정 필요...갑자기 왜..

            holder.storeName.setText(pharm.name);
            holder.updateTime.setText(pharm.createdAt);
            holder.receiveTime.setText(pharm.stockAt);

            String addr = pharm.addr;
            int idx = addr.indexOf("(");
            if(idx != -1) addr = addr.substring(0, idx);    //주소부분에서 괄호 설명부분은 생략
            holder.address.setText(addr);

            holder.deleteButton.setIconTintResource(color);
            holder.deleteButton.setTextColor(newColor);

            holder.deleteButton.setOnClickListener(FavoriteFragment.this);
        }

        @Override
        public int getItemCount() {
            return pharms.size();
        }
    }

    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder holder = recyclerView.findContainingViewHolder(v);
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

}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
//Fragment들 생성방식 바꿔야 함. getInstance로 만들어지게
