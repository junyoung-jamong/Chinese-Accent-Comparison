package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.icu.text.AlphabeticIndex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PracticeAccentActivity extends AppCompatActivity {
    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton recordButton;
    ImageButton closeButton;

    TextView titleTextView;

    GraphView contentsPitchGraph;
    private LineGraphSeries<DataPoint> contentsPitchSeries;
    private double graphLastXValue = 1d;
    PitchDetectionHandler pdHandler;

    ChineseAccentComparison cacp;

    AccentContents contents;
    int contents_id;

    ArrayList<Float> playedPitchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_accent);

        cacp = ChineseAccentComparison.getInstance(this);
        Intent intent = getIntent();

        contents_id = intent.getIntExtra(AccentContents._ID, -1);
        contents = cacp.findContentsById(contents_id);

        if(contents != null)
            initUI();
    }

    private void initUI()
    {
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        recordButton = findViewById(R.id.recordButton);
        closeButton = findViewById(R.id.closeButton);

        titleTextView = findViewById(R.id.titleTextView);

        contentsPitchGraph = findViewById(R.id.contentsPitchGraph);

        titleTextView.setText(contents.getTitle());

        contentsPitchSeries = new LineGraphSeries<>();
        contentsPitchSeries.setColor(Color.rgb(0xF1,0x70,0x68));
        contentsPitchSeries.setDataPointsRadius(50);
        contentsPitchSeries.setThickness(10);
        contentsPitchGraph.addSeries(contentsPitchSeries);
        contentsPitchGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        contentsPitchGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        contentsPitchGraph.getViewport().setXAxisBoundsManual(true);
        contentsPitchGraph.getViewport().setMinX(0);
        contentsPitchGraph.getViewport().setMaxX(100);
        contentsPitchGraph.getViewport().setMinY(-1);
        contentsPitchGraph.getViewport().setMaxY(300);

        pdHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contents != null)
                {
                    cacp.playContents(contents.getFilePath(), pdHandler);

                    playedPitchList = new ArrayList<>();

                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cacp.pauseContents();
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playedPitchList.size() > 0)
                {
                    contents.setPlayedPitchList(playedPitchList);
                    Intent intent = new Intent(PracticeAccentActivity.this, RecordActivity.class);
                    intent.putExtra(AccentContents._ID, contents.getId());
                    startActivity(intent);
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void processPitch(float pitchInHz){
        if (pitchInHz < 0)
            pitchInHz = 0;

        playedPitchList.add(pitchInHz);

        graphLastXValue += 1d;
        contentsPitchSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), true, 300);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cacp.finishContents();
    }
}
