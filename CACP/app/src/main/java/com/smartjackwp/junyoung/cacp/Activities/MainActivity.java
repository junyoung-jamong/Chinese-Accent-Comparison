package com.smartjackwp.junyoung.cacp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.smartjackwp.junyoung.cacp.Adapters.ContentsAdapter;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final int REQUEST_CODE_ADD_FILE = 1001;
    final String TAG = "MainActivity";

    Button addFileButton;
    ListView contentsListView;

    ChineseAccentComparison cacp;

    ArrayList<AccentContents> contentsList;
    ContentsAdapter contentsAdapter;

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

        contentsAdapter = new ContentsAdapter(this, contentsList);
        contentsListView.setAdapter(contentsAdapter);

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AddSoundFileActivity.class), REQUEST_CODE_ADD_FILE);
            }
        });

        contentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PracticeAccentActivity.class);
                AccentContents contents = (AccentContents)contentsAdapter.getItem(position);
                intent.putExtra(AccentContents._ID, contents.getId());
                startActivity(intent);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", dialogClickListener)
                        .setNegativeButton("취소", dialogClickListener)
                        .show();

                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult 호출 - requestCode: " + requestCode + ", resultCode: " + resultCode);

        contentsAdapter.notifyDataSetChanged();
    }
}
