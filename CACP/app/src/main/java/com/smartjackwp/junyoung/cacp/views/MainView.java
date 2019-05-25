package com.smartjackwp.junyoung.cacp.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartjackwp.junyoung.cacp.Activities.AddSoundFileActivity;
import com.smartjackwp.junyoung.cacp.Activities.MainActivity;
import com.smartjackwp.junyoung.cacp.Activities.NavigationActivity;
import com.smartjackwp.junyoung.cacp.Activities.PracticeAccentActivity;
import com.smartjackwp.junyoung.cacp.Adapters.SoundListAdapter;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

public class MainView extends LinearLayout {
    Activity mContext;

    final String TAG = "MainActivity";

    Button addFileButton;
    RecyclerView soundListView;

    ChineseAccentComparison cacp;

    ArrayList<AccentContents> contentsList;
    SoundListAdapter soundListAdapter;

    public MainView(Context context) {
        super(context);
        init(context);
    }

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        mContext = (Activity)context;
        LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_main, this, true);

        cacp = ChineseAccentComparison.getInstance(mContext);
        contentsList = cacp.getContentsList();

        initUI();

        soundListAdapter = new SoundListAdapter(getContext(), contentsList);
        soundListView.setAdapter(soundListAdapter);
    }

    private void initUI()
    {
        addFileButton = findViewById(R.id.addFileButton);

        soundListView = findViewById(R.id.soundListView);
        soundListView.setLayoutManager(new LinearLayoutManager(getContext()));

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivityForResult(new Intent(mContext, AddSoundFileActivity.class), NavigationActivity.REQUEST_CODE_ADD_FILE);
            }
        });
    }

    public void notifyDatasSetChanged(){
        soundListAdapter.notifyDataSetChanged();
    }

}
