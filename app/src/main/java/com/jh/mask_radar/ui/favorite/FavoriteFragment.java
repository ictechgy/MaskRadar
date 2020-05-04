package com.jh.mask_radar.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.RoomDatabase;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.jh.mask_radar.R;
import com.jh.mask_radar.db.AppDatabase;
import com.jh.mask_radar.db.Pharm;

import java.util.List;

public class FavoriteFragment extends Fragment {

    private FavoriteViewModel favoriteViewModel;
    private ProgressBar progressBar;
    private AppDatabase db;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppDatabase.destroyInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        favoriteViewModel =
                new ViewModelProvider(this).get(FavoriteViewModel.class);
        favoriteViewModel.fetchByRoom(db);                               //이렇게 db 객체를 넘겨줘도 괜찮을까? viewModel에서는 지역변수로만 쓰게 하면 괜찮지 않을까?
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);
        final TextView textView = root.findViewById(R.id.text_no_favorite);
        recyclerView = root.findViewById(R.id.favorite_recyclerview);

        progressBar = root.findViewById(R.id.favorite_progressBar);

        favoriteViewModel.getStores().observe(getViewLifecycleOwner(), (stores -> {
            //store값 변화시 UI 업데이트
            if(stores == null){      //초기 로딩 시
                progressBar.setVisibility(View.VISIBLE);
            }else if(stores.size() == 0){                   //즐겨찾기 한 약국이 없을 시
                textView.setText(getString(R.string.favorite_no_stores));
                progressBar.setVisibility(View.GONE);
            }else {                                      //즐겨찾기 한 약국 표시해주기
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                manager.setOrientation(RecyclerView.VERTICAL);
                recyclerView.setLayoutManager(manager);
                FavoriteAdapter adapter = new FavoriteAdapter();
                adapter.pharms = stores;
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }));

        return root;
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
            private MaterialButton addToFavoriteButton;
            FavoriteViewHolder(@NonNull View itemView) {
                super(itemView);
                stockStatus = itemView.findViewById(R.id.bottom_sheet_stock_status);
                statusView = itemView.findViewById(R.id.bottom_sheet_status_view);
                updateIcon = itemView.findViewById(R.id.bottom_sheet_update_icon);
                receiveIcon = itemView.findViewById(R.id.bottom_sheet_receive_icon);
                storeName = itemView.findViewById(R.id.bottom_sheet_store_name);
                updateTime = itemView.findViewById(R.id.bottom_sheet_update_time);
                receiveTime = itemView.findViewById(R.id.bottom_sheet_receive_time);
                address = itemView.findViewById(R.id.bottom_sheet_address);
                addToFavoriteButton = itemView.findViewById(R.id.bottom_sheet_button_add_favorite);
                //addToFavoriteButton.setOnClickListener(this);
            }
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_info_view, parent, false);
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

            holder.addToFavoriteButton.setIconTintResource(color);
        }

        @Override
        public int getItemCount() {
            return pharms.size();
        }
    }
}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
//Fragment들 생성방식 바꿔야 함. getInstance로 만들어지게
