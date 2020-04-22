package com.jh.mask_radar.ui.about;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jh.mask_radar.R;

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

        return root;
    }

    static class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.LicenseHolder>{

        static class LicenseHolder extends RecyclerView.ViewHolder{

            public LicenseHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public LicenseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull LicenseHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }


}
