package com.smartjackwp.junyoung.cacp.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartjackwp.junyoung.cacp.Activities.FullImageActivity;
import com.smartjackwp.junyoung.cacp.Adapters.HistoryAdapter;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.History;
import com.smartjackwp.junyoung.cacp.Interfaces.OnCapturedHistoryListener;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

public class HistoryView extends LinearLayout implements OnCapturedHistoryListener {
    Activity mContext;
    ChineseAccentComparison cacp;

    ListView historyListView;
    ArrayList<History> historyList;
    HistoryAdapter historyAdapter;

    public HistoryView(Context context) {
        super(context);
        init(context);
    }

    public HistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = (Activity)context;
        cacp = ChineseAccentComparison.getInstance(context);
        cacp.setOnCapturedHistoryListener(this);

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_history, this, true);

        historyListView = findViewById(R.id.historyListView);

        historyList = cacp.getHistories();
        historyAdapter = new HistoryAdapter(mContext, historyList);
        historyListView.setAdapter(historyAdapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final History history = (History)historyAdapter.getItem(position);
                mContext.startActivity(new Intent(mContext, FullImageActivity.class)
                        .putExtra(FullImageActivity.INTENT_KEY_IMAGE_PATH, history.getImagePath()));
            }
        });
    }

    @Override
    public void onCaptured(History history) {
        if(historyAdapter != null)
            historyAdapter.notifyDataSetChanged();
    }
}
