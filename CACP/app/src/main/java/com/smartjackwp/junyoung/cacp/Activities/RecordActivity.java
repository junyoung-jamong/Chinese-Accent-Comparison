package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Constants;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.Entity.Subtitle;
import com.smartjackwp.junyoung.cacp.Entity.SubtitleUnit;
import com.smartjackwp.junyoung.cacp.R;

import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class RecordActivity extends AppCompatActivity {

    GraphView recordingGraphView;
    ImageButton startRecordButton;
    ImageButton endRecordButton;

    TextView subtitleTextView;
    TextView pinyinTextView;

    FrameLayout countLayout;
    TextView countText;

    PitchDetectionHandler pdHandler;

    private LineGraphSeries<DataPoint> playedGraphSeries;
    private LineGraphSeries<DataPoint> playedFlowGraphSeries;
    private LineGraphSeries<DataPoint> recordingGraphSeries;
    private LineGraphSeries<DataPoint> recordingFlowGraphSeries;
    private double graphLastXValue = 1d;

    ChineseAccentComparison cacp;

    ArrayList<Float> playedPitchList;
    ArrayList<Float> recordedPitchList;
    int playedPitchListSize;
    int maximumSize;

    int contentId;
    AccentContents contents;
    Subtitle subtitle;
    int currentSubtitleIndex;

    final int GRAPH_X_LENGTH = 100;
    final int PRE_SHOW = 30;

    final int BACKGROUND_COLOR = Color.rgb(0x3f, 0x53, 0x6e);
    final int PLAYED_COLOR = Color.rgb(0x26, 0xc5, 0xcd);
    final int RECORD_COLOR = Color.rgb(0xfd, 0xcc, 0x00);
    final int THICKNESS = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        cacp = ChineseAccentComparison.getInstance(this);

        Intent intent = getIntent();
        contentId = intent.getIntExtra(AccentContents._ID, -1);
        contents = cacp.findContentsById(contentId);

        if(contents != null)
        {
            this.subtitle = contents.getSubtitle();
            initUI();
        }
    }

    private void initUI()
    {
        recordingGraphView = findViewById(R.id.recordingGraphView);
        startRecordButton = findViewById(R.id.startRecordButton);
        endRecordButton = findViewById(R.id.endRecordButton);

        subtitleTextView = findViewById(R.id.subtitleTextView);
        pinyinTextView = findViewById(R.id.pinyinTextView);

        countLayout = findViewById(R.id.countLayout);
        countText = findViewById(R.id.countText);

        if(this.subtitle != null)
        {
            ArrayList<SubtitleUnit> subtitleUnits = subtitle.getSubtitleUnits();
            if(subtitleUnits != null && subtitleUnits.size() > 0)
            {
                String[] contents = subtitleUnits.get(0).getContents();
                subtitleTextView.setText(contents[0]);
                pinyinTextView.setText(contents[1]);
                currentSubtitleIndex = 0;
            }
        }

        playedPitchList = contents.getPlayedPitchList();
        playedPitchListSize = playedPitchList.size();
        maximumSize = playedPitchListSize;

        playedFlowGraphSeries = new LineGraphSeries<>();
        playedFlowGraphSeries.setColor(Color.TRANSPARENT);
        recordingFlowGraphSeries = new LineGraphSeries<>();
        recordingFlowGraphSeries.setColor(RECORD_COLOR);

        recordingGraphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        recordingGraphView.getGridLabelRenderer().setGridColor(BACKGROUND_COLOR);
        recordingGraphView.getGridLabelRenderer().setHorizontalAxisTitleColor(BACKGROUND_COLOR);
        recordingGraphView.addSeries(playedFlowGraphSeries);
        recordingGraphView.addSeries(recordingFlowGraphSeries);

        for(int i=0; i<playedPitchListSize; i++)
        {
            if(playedPitchList.get(i) > 0)
            {
                if(playedGraphSeries == null)
                {
                    playedGraphSeries = new LineGraphSeries<>();
                    playedGraphSeries.setThickness(THICKNESS);
                    playedGraphSeries.setColor(PLAYED_COLOR);
                    recordingGraphView.addSeries(playedGraphSeries);
                }
                playedGraphSeries.appendData(new DataPoint(i+1, playedPitchList.get(i)), false, 300);
            }
            else
            {
                playedGraphSeries = null;
                playedFlowGraphSeries.appendData(new DataPoint(i+1, playedPitchList.get(i)), false, 300);
            }
        }

        recordingGraphSeries = new LineGraphSeries<>();
        recordingGraphSeries.setColor(Color.rgb(0xF1,0x70,0x68));
        recordingGraphSeries.setThickness(THICKNESS);
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
                final long timeStamp = Math.round( e.getTimeStamp() * 1000);
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz, timeStamp);
                    }
                });
            }
        };

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStartRecording();
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

    private void processPitch(float pitchInHz, long timeStamp){
        if (pitchInHz < Constants.THRESHOLD_PITCH_MINIMUM || pitchInHz > Constants.THRESHOLD_PITCH_MAXIMUM)
            pitchInHz = 0;

        recordedPitchList.add(pitchInHz);

        graphLastXValue += 1d;
        if(pitchInHz > 0)
        {
            if(recordingGraphSeries == null)
            {
                recordingGraphSeries = new LineGraphSeries<>();
                recordingGraphSeries.setColor(RECORD_COLOR);
                recordingGraphSeries.setThickness(THICKNESS);
                recordingGraphView.addSeries(recordingGraphSeries);
            }
            recordingGraphSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), false, 300);
        }
        else
            recordingGraphSeries = null;
        recordingFlowGraphSeries.appendData(new DataPoint(graphLastXValue, 0), false, 300);

        if(graphLastXValue > GRAPH_X_LENGTH-PRE_SHOW)
        {
            recordingGraphView.getViewport().setMinX(graphLastXValue-GRAPH_X_LENGTH+PRE_SHOW);
            recordingGraphView.getViewport().setMaxX(graphLastXValue+PRE_SHOW);
        }

        if(graphLastXValue > maximumSize)
            stopRecording();

        setSubtitle(timeStamp);

    }

    private void setSubtitle(long progress)
    {
        if(subtitle != null)
        {
            List<SubtitleUnit> subtitleUnits = subtitle.getSubtitleUnits();
            if(currentSubtitleIndex < subtitleUnits.size())
            {
                if(subtitleUnits.get(currentSubtitleIndex).getEndTimeMillisecond() <= progress)
                {
                    String[] contents = subtitleUnits.get(currentSubtitleIndex).getContents();
                    if(contents.length >= 2)
                    {
                        subtitleTextView.setText(contents[0]);
                        pinyinTextView.setText(contents[1]);
                    }
                    currentSubtitleIndex++;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cacp.stopRecord();
    }

    private void setStartRecording(){
        new CountDownTask().execute();
    }

    private void stopRecording()
    {
        cacp.stopRecord();
        contents.setRecordedPitchList(recordedPitchList);
        cacp.measureSimilarity(contents);
        finish();
    }

    class CountDownTask extends AsyncTask<Void, Integer, Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            countLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if(values[0] == 0)
                countText.setText("시작");
            else
                countText.setText(values[0]+"");


        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(int i=3; i>-1; i--)
            {
                publishProgress(i);
                try{
                    Thread.sleep(1000);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            countLayout.setVisibility(View.INVISIBLE);

            cacp.startRecord(pdHandler);

            recordedPitchList = new ArrayList<>();

            startRecordButton.setVisibility(View.INVISIBLE);
            endRecordButton.setVisibility(View.VISIBLE);
        }
    }
}
