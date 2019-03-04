package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.support.annotation.Nullable;
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

        cacp = ChineseAccentComparison.getInstance();
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
                //intent.putExtra(AccentContents._ID, contents.getID());
                intent.putExtra(AccentContents.FILE_PATH, contents.getFilePath());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult 호출 - requestCode: " + requestCode + ", resultCode: " + resultCode);

        String filePath = data.getStringExtra(AccentContents.FILE_PATH);
        String title = data.getStringExtra(AccentContents.TITLE);
        String description = data.getStringExtra(AccentContents.DESCRIPTION);

        contentsList.add(new AccentContents(filePath, title, description));
        contentsAdapter.notifyDataSetChanged();
    }
}
