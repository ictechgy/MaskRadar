package com.jh.mask_radar;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jh.mask_radar.ui.map.NavViewHeightReturnSender;
import com.naver.maps.map.NaverMapSdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavViewHeightReturnSender {

    private int navViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_favorite, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        NaverMapSdk.getInstance(this).setClient(new NaverMapSdk.NaverCloudPlatformClient(getString(R.string.API_KEY)));
        //navViewHeight = navView.getHeight();  이 방식으로 측정시 0값 측정됨.
        navView.post(()-> navViewHeight = navView.getMeasuredHeight());
    }

    @Override
    public int getNavViewHeight(){
        return navViewHeight;
    }

}
