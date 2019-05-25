package com.smartjackwp.junyoung.cacp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.smartjackwp.junyoung.cacp.Adapters.ContentsAdapter;
import com.smartjackwp.junyoung.cacp.Adapters.SoundListAdapter;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    Button addFileButton;
    ListView contentsListView;

    ChineseAccentComparison cacp;

    ArrayList<AccentContents> contentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacp = ChineseAccentComparison.getInstance(this);
        contentsList = cacp.getContentsList();

        initUI();

    }

    private void initUI()
    {
        addFileButton = findViewById(R.id.addFileButton);
        contentsListView = findViewById(R.id.contentsListView);

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AddSoundFileActivity.class), NavigationActivity.REQUEST_CODE_ADD_FILE);
            }
        });
    }
}
