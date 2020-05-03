package com.jh.mask_radar.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.jh.mask_radar.R;

public class FavoriteFragment extends Fragment {

    private FavoriteViewModel favoriteViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favoriteViewModel =
                ViewModelProviders.of(this).get(FavoriteViewModel.class);
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        favoriteViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}

//추가해야하는 기능. 즐겨찾기 화면 완성 및 업데이트 기능, 알람기능은 서버구축 필요
//더불어서 있으면 좋을만한 기능 - 휴일지킴이 약국 표시 및 약국별 운영여부 표시(운영시간)
