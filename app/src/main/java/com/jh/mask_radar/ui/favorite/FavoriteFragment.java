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
import androidx.room.RoomDatabase;

import com.jh.mask_radar.R;
import com.jh.mask_radar.db.AppDatabase;

public class FavoriteFragment extends Fragment {

    private FavoriteViewModel favoriteViewModel;
    private ProgressBar progressBar;
    private RoomDatabase db;

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

        progressBar = root.findViewById(R.id.favorite_progressBar);

        favoriteViewModel.getStores().observe(getViewLifecycleOwner(), (stores -> {
            //store값 변화시 UI 업데이트
            if(stores == null){      //초기 로딩 시
                progressBar.setVisibility(View.VISIBLE);
            }else if(stores.size() == 0){                   //즐겨찾기 한 약국이 없을 시
                textView.setText(getString(R.string.favorite_no_stores));
                progressBar.setVisibility(View.GONE);
            }else {                                      //즐겨찾기 한 약국 표시해주기

            }
        }));

        return root;
    }
}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
//Fragment들 생성방식 바꿔야 함. getInstance로 만들어지게
