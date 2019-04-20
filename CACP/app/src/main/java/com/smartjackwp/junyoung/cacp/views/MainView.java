package com.smartjackwp.junyoung.cacp.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartjackwp.junyoung.cacp.Activities.AddSoundFileActivity;
import com.smartjackwp.junyoung.cacp.Activities.MainActivity;
import com.smartjackwp.junyoung.cacp.Activities.PracticeAccentActivity;
import com.smartjackwp.junyoung.cacp.Adapters.ContentsAdapter;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

public class MainView extends LinearLayout {
    Activity mContext;

    final int REQUEST_CODE_ADD_FILE = 1001;
    final String TAG = "MainActivity";

    Button addFileButton;
    ListView contentsListView;

    ChineseAccentComparison cacp;

    ArrayList<AccentContents> contentsList;
    ContentsAdapter contentsAdapter;

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
    }

    private void initUI()
    {
        addFileButton = findViewById(R.id.addFileButton);
        contentsListView = findViewById(R.id.contentsListView);

        contentsAdapter = new ContentsAdapter(mContext, contentsList);
        contentsListView.setAdapter(contentsAdapter);

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivityForResult(new Intent(mContext, AddSoundFileActivity.class), REQUEST_CODE_ADD_FILE);
            }
        });

        contentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, PracticeAccentActivity.class);
                AccentContents contents = contentsList.get(position);
                intent.putExtra(AccentContents._ID, contents.getId());
                mContext.startActivity(intent);
            }
        });

        contentsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case DialogInterface.BUTTON_POSITIVE:
                                AccentContents contents = (AccentContents) contentsAdapter.getItem(position);
                                if(cacp.deleteContents(contents))
                                    contentsAdapter.notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", dialogClickListener)
                        .setNegativeButton("취소", dialogClickListener)
                        .show();

                return true;
            }
        });
    }

    public void notifyDatasSetChanged(){
        contentsAdapter.notifyDataSetChanged();
    }
}
