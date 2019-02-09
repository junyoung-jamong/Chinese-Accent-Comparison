package com.smartjackwp.junyoung.functionalsampleproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button recordPlayButton;
    Button filePlayButton;
    Button pitchGraphButton;
    Button similarityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordPlayButton = findViewById(R.id.recordplayButton);
        filePlayButton = findViewById(R.id.filePlayButton);
        pitchGraphButton = findViewById(R.id.pitchGraphButton);
        similarityButton = findViewById(R.id.similarityButton);

        MOnCLickListener mOnCLickListener = new MOnCLickListener();
        recordPlayButton.setOnClickListener(mOnCLickListener);
        filePlayButton.setOnClickListener(mOnCLickListener);
        pitchGraphButton.setOnClickListener(mOnCLickListener);
        similarityButton.setOnClickListener(mOnCLickListener);
    }

    class MOnCLickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.recordplayButton:
                    startActivity(new Intent(MainActivity.this, RecordPlayActivity.class));
                    break;
                case R.id.filePlayButton:
                    startActivity(new Intent(MainActivity.this, FilePlayActivity.class));
                    break;
                case R.id.pitchGraphButton:
                    startActivity(new Intent(MainActivity.this, PitchRealTimeGraphActivity.class));
                    break;
                case R.id.similarityButton:
                    startActivity(new Intent(MainActivity.this, SimilarityMeasureActivity.class));
                    break;
            }
        }
    }
}
