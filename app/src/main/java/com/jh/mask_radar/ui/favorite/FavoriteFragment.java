package com.jh.mask_radar.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.RoomDatabase;
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
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

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
            }else if(pharms.size() == 0){                   //즐겨찾기 한 약국이 없을 시
                textView.setText(getString(R.string.favorite_no_stores));
                progressBar.setVisibility(View.GONE);
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
            }
        }));

        refreshLayout = root.findViewById(R.id.favorite_swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnabled(false);

        return root;
    }

    @Override
    public void onRefresh() {
        favoriteViewModel.refreshList(db);        //do real update in viewModel
        refreshLayout.setRefreshing(false);
    }

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

}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
//Fragment들 생성방식 바꿔야 함. getInstance로 만들어지게
