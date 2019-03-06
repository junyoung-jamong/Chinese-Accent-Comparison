package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.icu.text.AlphabeticIndex;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;
import com.smartjackwp.junyoung.cacp.Utils.Tools;

import java.util.ArrayList;

import Interfaces.OnMeasuredSimilarityListener;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PracticeAccentActivity extends AppCompatActivity implements OnMeasuredSimilarityListener {
    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton recordButton;
    ImageButton closeButton;

    TextView titleTextView;
    TextView simTextView;
    TextView durationTextView;
    TextView runningTimeTextView;

    SeekBar playSeekBar;

    GraphView contentsPitchGraph;
    GraphView similarityGraph;
    private LineGraphSeries<DataPoint> contentsPitchSeries;
    private double graphLastXValue = 1d;
    PitchDetectionHandler pdHandler;

    ChineseAccentComparison cacp;

    AccentContents contents;
    int contents_id;
    int duration;
    double currentTimeOffset;

    boolean isPlaying = false;

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
        {
            initUI();
            cacp.setOnMeasuredSimilarityListener(this);
        }
    }

    @Override
    public void onMeasured(double sim, ArrayList<Float> playedPitch, ArrayList<Float> recordedPitch) {
        simTextView.setVisibility(View.VISIBLE);
        simTextView.setText("유사도 점수: " + Math.round(sim) + "%");

        LineGraphSeries<DataPoint> playedPitchSeries = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> recordedPitchSeries = new LineGraphSeries<>();

        for(int i=0; i<playedPitch.size(); i++)
            playedPitchSeries.appendData(new DataPoint(i+1, playedPitch.get(i)), true, 300);

        for(int i=0; i<recordedPitch.size(); i++)
            recordedPitchSeries.appendData(new DataPoint(i+1, recordedPitch.get(i)), true, 300);

        playedPitchSeries.setColor(Color.rgb(0xF1,0x70,0x68));

        similarityGraph.getViewport().setMaxX(Math.max(playedPitch.size(), recordedPitch.size()));
        similarityGraph.removeAllSeries();
        similarityGraph.addSeries(playedPitchSeries);
        similarityGraph.addSeries(recordedPitchSeries);
        similarityGraph.setVisibility(View.VISIBLE);

    }

    private void initUI()
    {
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        recordButton = findViewById(R.id.recordButton);
        closeButton = findViewById(R.id.closeButton);

        titleTextView = findViewById(R.id.titleTextView);
        simTextView = findViewById(R.id.simTextView);
        durationTextView = findViewById(R.id.durationTextView);
        runningTimeTextView = findViewById(R.id.runningTimeTextView);

        playSeekBar = findViewById(R.id.playSeekBar);
        this.duration = (int)(contents.getDuration()*1000);
        playSeekBar.setMax(duration);

        contentsPitchGraph = findViewById(R.id.contentsPitchGraph);
        similarityGraph = findViewById(R.id.similarityGraph);

        titleTextView.setText(contents.getTitle());
        durationTextView.setText(Tools.getTimeFormat((long)contents.getDuration()));

        similarityGraph.getViewport().setXAxisBoundsManual(true);
        similarityGraph.getViewport().setMinX(0);
        similarityGraph.getViewport().setMinY(0);
        similarityGraph.getViewport().setMaxY(1);
        similarityGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        similarityGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

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
            public void handlePitch(PitchDetectionResult res, final AudioEvent e){
                //Log.e("HandlePitch", "EndTimeStamp : " + e.getEndTimeStamp() + " | FrameLength :" + e.getFrameLength()
                //+ " | Progress:" + e.getProgress() + " | TimeStamp: " + e.getTimeStamp());

                final int timeStamp = (int)(e.getTimeStamp() * 1000);
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz, timeStamp);
                    }
                });
            }
        };

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contents != null)
                {
                    play();
                    /*
                    if(isPlaying)
                        resume();
                    else
                        play();
                    */
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playedPitchList.size() > 0)
                {
                    pause();

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

    private void processPitch(float pitchInHz, int timeStamp){
        if (pitchInHz < 0)
            pitchInHz = 0;

        playedPitchList.add(pitchInHz);

        graphLastXValue += 1d;
        contentsPitchSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), true, 300);

        playSeekBar.setProgress(timeStamp);
        runningTimeTextView.setText(Tools.getTimeFormat(timeStamp/1000));

        if(duration-10 <= timeStamp)
            stop();

    }

    private void play()
    {
        cacp.playContents(contents.getFilePath(), pdHandler);
        playedPitchList = new ArrayList<>();
        isPlaying = true;
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void pause()
    {
        cacp.pauseContents();
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
    }

    private void resume()
    {
        cacp.resumeContents(contents.getFilePath(), pdHandler, currentTimeOffset);
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void stop()
    {
        cacp.stopContents();
        isPlaying =false;
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cacp.finishContents();
    }
}
