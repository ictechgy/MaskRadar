package com.jh.mask_radar.ui.about;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jh.mask_radar.R;

import java.util.ArrayList;
import java.util.Arrays;

public class LicenseFragment extends Fragment {
    RecyclerView recyclerView;

    public LicenseFragment() {
        // Required empty public constructor
    }

    public static LicenseFragment newInstance() {
        return new LicenseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //param settings
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_license, container, false);
        recyclerView = root.findViewById(R.id.license_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<LicenseAdapter.License> list = new ArrayList<>();
        //라이센스 정보 객체 생성 후 list에 add
        //Hard Coded
        list.add(new LicenseAdapter.License(getString(R.string.license_naver_map_title), getString(R.string.license_naver_map_content)));
        list.add(new LicenseAdapter.License(getString(R.string.license_material_title), getString(R.string.license_material_content)));
        list.add(new LicenseAdapter.License(getString(R.string.license_woowa_title), getString(R.string.license_woowa_content)));
        list.add(new LicenseAdapter.License(getString(R.string.license_naver_font_title), getString(R.string.license_naver_font_content)));
        list.add(new LicenseAdapter.License(getString(R.string.license_nh_ci_title), getString(R.string.license_nh_ci_content)));
        list.add(new LicenseAdapter.License(getString(R.string.license_post_ci_title), getString(R.string.license_post_ci_content)));
        //can be changed to ArrayAdapter<>()?

        LicenseAdapter adapter = new LicenseAdapter();
        adapter.licenses = list;
        recyclerView.setAdapter(adapter);

        return root;
    }

    private static class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>{
        static class License {
            String title;
            String content;
            License(String title, String content){ this.title = title; this.content = content; }
        }
        ArrayList<License> licenses;

        static class LicenseViewHolder extends RecyclerView.ViewHolder{
            TextView licenseTitle;
            TextView licenseContent;
            LicenseViewHolder(@NonNull View itemView) {
                super(itemView);
                licenseTitle = itemView.findViewById(R.id.license_title);
                licenseContent = itemView.findViewById(R.id.license_content);
            }
        }

        @NonNull
        @Override
        public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_license, parent, false);
            return new LicenseViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(@NonNull LicenseViewHolder holder, int position) {
            License license = licenses.get(holder.getAdapterPosition());
            holder.licenseTitle.setText(license.title);
            holder.licenseContent.setText(license.content);
        }

        @Override
        public int getItemCount() {
            return licenses.size();
        }
    }


}
