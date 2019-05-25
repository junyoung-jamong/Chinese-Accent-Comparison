package com.smartjackwp.junyoung.cacp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.R;
import com.smartjackwp.junyoung.cacp.views.HistoryView;
import com.smartjackwp.junyoung.cacp.views.MainView;
import com.smartjackwp.junyoung.cacp.views.SettingView;

public class NavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int REQUEST_CODE_ADD_FILE = 1001;
    final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_CODE = 1501;

    ChineseAccentComparison cacp;

    FrameLayout contentLayout;
    MainView mainView;
    HistoryView historyView;
    SettingView settingView;

    View currentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        if(!checkMicPermission())
            return;

        init();
    }

    private void init(){
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        cacp = ChineseAccentComparison.getInstance(this);
        cacp.setInitialSoundFile();

        contentLayout = findViewById(R.id.contentLayout);

        mainView = new MainView(this);
        historyView = new HistoryView(this);
        settingView = new SettingView(this);

        setView(mainView);
    }

    private boolean checkMicPermission(){
        int recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if(recordPermission == PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_CODE);
            return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                setView(mainView);
                return true;
            case R.id.navigation_dashboard:
                setView(historyView);
                return true;
                /*
            case R.id.navigation_notifications:
                setView(settingView);
                return true;
                */
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_FILE)
        {
            if(resultCode == RESULT_OK)
                mainView.notifyDatasSetChanged();
        }
    }

    private void setView(View v){
        if(currentView != null)
            contentLayout.removeView(currentView);

        currentView = v;
        contentLayout.addView(v);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean check_result = true;
        if(requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_CODE){
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if(check_result)
                init();
            else
                finish();
        }
    }

}
