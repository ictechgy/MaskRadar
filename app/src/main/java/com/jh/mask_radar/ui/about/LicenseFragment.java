package com.jh.mask_radar.ui.about;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jh.mask_radar.R;

public class LicenseFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_license, container, false);
    }
}
