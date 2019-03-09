package com.smartjackwp.junyoung.cacp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.smartjackwp.junyoung.cacp.Interfaces.OnMeasuredSimilarityListener;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PracticeAccentActivity extends AppCompatActivity implements OnMeasuredSimilarityListener {
    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton recordButton;
    ImageButton closeButton;
    ImageButton captureButton;

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
    double lastPausedTimeOffset;

    boolean isPlaying = false;
    boolean isPaused = false;

    ArrayList<Float> playedPitchList;

    private static final String CAPTURE_PATH = "/CACP_CAPTURE";

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
        captureButton = findViewById(R.id.captureButton);

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
                currentTimeOffset = e.getTimeStamp() + lastPausedTimeOffset;
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
                    if(isPlaying)
                        resume();
                    else
                        play();
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

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(capture())
                    Toast.makeText(PracticeAccentActivity.this, "화면이 캡쳐되었습니다.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(PracticeAccentActivity.this, "화면이 캡쳐 실패", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processPitch(float pitchInHz, int timeStamp){
        if(!isPaused)
        {
            if (pitchInHz < 0)
                pitchInHz = 0;

            playedPitchList.add(pitchInHz);

            graphLastXValue += 1d;
            contentsPitchSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), true, 300);

            int progress = (int)this.lastPausedTimeOffset * 1000 + timeStamp;
            if(duration-100 <= progress)
            {
                stop();
                playSeekBar.setProgress(playSeekBar.getMax());
            }
            else
            {
                if(progress <= playSeekBar.getMax())
                    playSeekBar.setProgress(progress);
                else
                    playSeekBar.setProgress(playSeekBar.getMax());

                runningTimeTextView.setText(Tools.getTimeFormat(progress/1000));
            }
        }
    }

    private void play()
    {
        cacp.playContents(contents.getFilePath(), pdHandler);
        playedPitchList = new ArrayList<>();
        isPlaying = true;
        isPaused = false;
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void pause()
    {
        cacp.pauseContents();
        lastPausedTimeOffset = currentTimeOffset;
        isPaused = true;
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
    }

    private void resume()
    {
        cacp.resumeContents(contents.getFilePath(), pdHandler, currentTimeOffset);
        isPaused = false;
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void stop()
    {
        cacp.stopContents();
        isPlaying = false;
        isPaused = false;
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        currentTimeOffset = 0;
        lastPausedTimeOffset = 0;
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

    private boolean capture()
    {
        View root = this.getWindow().getDecorView().getRootView();
        root.setDrawingCacheEnabled(true);
        root.buildDrawingCache();
        Bitmap screenshot = root.getDrawingCache();

        int[] location = new int[2];
        root.getLocationInWindow(location);

        Bitmap bmp = Bitmap.createBitmap(screenshot, location[0], location[1], root.getWidth(), root.getHeight(), null, false);
        String strFolderPath = Environment.getExternalStorageDirectory() + CAPTURE_PATH;
        File folder = new File(strFolderPath);
        if(!folder.exists()) {
            folder.mkdirs();
        }

        String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(fileCacheItem);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
