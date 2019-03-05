package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

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

public class RecordActivity extends AppCompatActivity {

    GraphView recordingGraphView;
    ImageButton startRecordButton;
    ImageButton endRecordButton;

    PitchDetectionHandler pdHandler;

    private LineGraphSeries<DataPoint> playedGraphSeries;
    private LineGraphSeries<DataPoint> recordingGraphSeries;
    private double graphLastXValue = 1d;

    ChineseAccentComparison cacp;

    ArrayList<Float> playedPitchList;
    ArrayList<Float> recordedPitchList;
    int playedPitchListSize;
    int maximumSize;

    int contentId;
    AccentContents contents;

    final int GRAPH_X_LENGTH = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        cacp = ChineseAccentComparison.getInstance(this);

        Intent intent = getIntent();
        contentId = intent.getIntExtra(AccentContents._ID, -1);
        contents = cacp.findContentsById(contentId);

        if(contents != null)
            initUI();
    }

    private void initUI()
    {
        recordingGraphView = findViewById(R.id.recordingGraphView);
        startRecordButton = findViewById(R.id.startRecordButton);
        endRecordButton = findViewById(R.id.endRecordButton);

        playedGraphSeries = new LineGraphSeries<>();
        playedGraphSeries.setColor(Color.BLUE);
        playedGraphSeries.setDataPointsRadius(50);
        playedGraphSeries.setThickness(10);

        playedPitchList = contents.getPlayedPitchList();
        playedPitchListSize = playedPitchList.size();
        maximumSize = playedPitchListSize+10;
        for(int i=0; i<playedPitchListSize; i++)
            playedGraphSeries.appendData(new DataPoint(i+1, playedPitchList.get(i)), false, 300);

        recordingGraphView.addSeries(playedGraphSeries);

        recordingGraphSeries = new LineGraphSeries<>();
        recordingGraphSeries.setColor(Color.rgb(0xF1,0x70,0x68));
        recordingGraphSeries.setDataPointsRadius(50);
        recordingGraphSeries.setThickness(10);
        recordingGraphView.addSeries(recordingGraphSeries);
        recordingGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        recordingGraphView.getGridLabelRenderer().setVerticalLabelsVisible(false);

        recordingGraphView.getViewport().setXAxisBoundsManual(true);
        recordingGraphView.getViewport().setMinX(0);
        recordingGraphView.getViewport().setMaxX(GRAPH_X_LENGTH);
        recordingGraphView.getViewport().setMinY(-1);
        recordingGraphView.getViewport().setMaxY(300);

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

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cacp.startRecord(pdHandler);

                recordedPitchList = new ArrayList<>();

                startRecordButton.setVisibility(View.INVISIBLE);
                endRecordButton.setVisibility(View.VISIBLE);
            }
        });

        endRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();

                startRecordButton.setVisibility(View.VISIBLE);
                endRecordButton.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void processPitch(float pitchInHz){
        if (pitchInHz < 0)
            pitchInHz = 0;

        recordedPitchList.add(pitchInHz);

        graphLastXValue += 1d;
        recordingGraphSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), false, 300);
        if(graphLastXValue > GRAPH_X_LENGTH)
        {
            recordingGraphView.getViewport().setMinX(graphLastXValue-GRAPH_X_LENGTH);
            recordingGraphView.getViewport().setMaxX(graphLastXValue);
        }

        if(graphLastXValue > maximumSize)
            stopRecording();

    }

    @Override
    protected void onStop() {
        super.onStop();
        cacp.stopRecord();
    }

    private void stopRecording()
    {
        cacp.stopRecord();
        contents.setRecordedPitchList(recordedPitchList);
        cacp.measureSimilarity(contents);
        finish();
    }
}
