package com.smartjackwp.junyoung.cacp.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Constants;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.Entity.History;
import com.smartjackwp.junyoung.cacp.Entity.Subtitle;
import com.smartjackwp.junyoung.cacp.Entity.SubtitleUnit;
import com.smartjackwp.junyoung.cacp.R;
import com.smartjackwp.junyoung.cacp.Utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.smartjackwp.junyoung.cacp.Interfaces.OnMeasuredSimilarityListener;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PracticeAccentActivity extends AppCompatActivity implements OnMeasuredSimilarityListener, ActivityCompat.OnRequestPermissionsResultCallback{
    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton recordButton;
    ImageButton closeButton;
    ImageButton captureButton;

    TextView titleTextView;
    TextView simTextView;
    TextView durationTextView;
    TextView runningTimeTextView;
    TextView subtitleTextView;
    TextView pinyinTextView;

    SeekBar playSeekBar;

    GraphView contentsPitchGraph;
    GraphView similarityGraph;
    private LineGraphSeries<DataPoint> contentToneGraphSeries;
    private LineGraphSeries<DataPoint> contentFlowGraphSeries;

    private LineGraphSeries<DataPoint> flowSeries;
    private LineGraphSeries<DataPoint> toneSeries;
    private double graphLastXValue = 1d;
    PitchDetectionHandler pdHandler;

    ChineseAccentComparison cacp;

    AccentContents contents;
    int contents_id;
    int duration;
    double currentTimeOffset;
    double lastPausedTimeOffset;
    Subtitle subtitle;
    int currentSubtitleIndex;

    boolean isPlaying = false;
    boolean isPaused = false;

    ArrayList<Float> playedPitchList = new ArrayList<>();

    final int PLAYED_COLOR = Color.rgb(0x26, 0xc5, 0xcd);
    final int RECORDED_COLOR = Color.rgb(0xF1,0x70,0x68);
    final int THICKNESS = 25;
    final int GRAPH_X_LENGTH = 100;
    final int PRE_SHOW = 30;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.RECORD_AUDIO, // 카메라
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    final int PERMISSIONS_REQUEST_CODE = 2001;

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
            this.subtitle = contents.getSubtitle();
            initUI();
            cacp.setOnMeasuredSimilarityListener(this);

            new ContentPitchLoadingTask().execute();
        }
    }

    @Override
    public void onMeasured(double sim, ArrayList<Float> playedPitch, ArrayList<Float> recordedPitch) {
        int max = Math.max(playedPitch.size(), recordedPitch.size());
        similarityGraph.removeAllSeries();
        similarityGraph.getViewport().setMaxX(max);

        simTextView.setVisibility(View.VISIBLE);
        simTextView.setText("유사도 점수: " + Math.round(sim) + "%");

        LineGraphSeries<DataPoint> flowPitchSeries = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> playedPitchSeries = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> recordedPitchSeries = new LineGraphSeries<>();

        flowPitchSeries.setColor(Color.TRANSPARENT);
        similarityGraph.addSeries(flowPitchSeries);

        for(int i=0; i<max; i++)
            flowPitchSeries.appendData(new DataPoint(i+1, 0), false, playedPitch.size());

        for(int i=0; i<playedPitch.size(); i++)
        {
            Log.e("onMeasured-playedPitch", " " + playedPitch.get(i));
            if(playedPitch.get(i) > 0)
            {
                if(playedPitchSeries == null)
                {
                    playedPitchSeries = new LineGraphSeries<>();
                    playedPitchSeries.setThickness(THICKNESS);
                    similarityGraph.addSeries(playedPitchSeries);
                }
                playedPitchSeries.appendData(new DataPoint(i+1, playedPitch.get(i)), false, max);
            }
            else
            {
                playedPitchSeries = null;
            }
        }

        for(int i=0; i<recordedPitch.size(); i++)
        {
            Log.e("onMeasured-recordPitch", " " + recordedPitch.get(i));
            if(recordedPitch.get(i) > 0)
            {
                if(recordedPitchSeries == null)
                {
                    recordedPitchSeries = new LineGraphSeries<>();
                    recordedPitchSeries.setColor(RECORDED_COLOR);
                    recordedPitchSeries.setThickness(THICKNESS);
                    similarityGraph.addSeries(recordedPitchSeries);
                }
                recordedPitchSeries.appendData(new DataPoint(i+1, recordedPitch.get(i)), false, max);
            }
            else
            {
                recordedPitchSeries = null;
            }
        }
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
        subtitleTextView = findViewById(R.id.subtitleTextView);
        pinyinTextView = findViewById(R.id.pinyinTextView);

        playSeekBar = findViewById(R.id.playSeekBar);
        this.duration = (int)(contents.getDuration()*1000);

        playSeekBar.setMax(Math.max(duration-1000, 1000));

        contentsPitchGraph = findViewById(R.id.contentsPitchGraph);
        similarityGraph = findViewById(R.id.similarityGraph);

        File file = new File(contents.getFilePath());
        if(file.exists())
            titleTextView.setText(file.getName() + " - " + contents.getTitle());
        durationTextView.setText(Tools.getTimeFormat(Math.ceil(contents.getDuration())));

        initSubtitle();

        similarityGraph.getViewport().setXAxisBoundsManual(true);
        similarityGraph.getViewport().setMinX(0);
        similarityGraph.getViewport().setMinY(0);
        similarityGraph.getViewport().setMaxY(1);
        similarityGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        similarityGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        contentFlowGraphSeries = new LineGraphSeries<>();
        contentFlowGraphSeries.setColor(Color.TRANSPARENT);

        flowSeries = new LineGraphSeries<>();
        flowSeries.setColor(Color.TRANSPARENT);

        contentsPitchGraph.addSeries(contentFlowGraphSeries);
        contentsPitchGraph.addSeries(flowSeries);

        contentsPitchGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        contentsPitchGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        contentsPitchGraph.getViewport().setXAxisBoundsManual(true);

        pdHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, final AudioEvent e){
                currentTimeOffset = e.getTimeStamp() + lastPausedTimeOffset;
                final long timeStamp = Math.round(currentTimeOffset * 1000);
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

                if(!checkMicPermission())
                    return;

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

    private void processPitch(float pitchInHz, long timeStamp){
        if(!isPaused)
        {
            if (pitchInHz < Constants.THRESHOLD_PITCH_MINIMUM || pitchInHz > Constants.THRESHOLD_PITCH_MAXIMUM)
                pitchInHz = 0;

            playedPitchList.add(pitchInHz);
            drawPitchGraph(pitchInHz);

            long progress = timeStamp;
            setProgressBar(progress);
            setSubtitle(progress);

            if(timeStamp >= this.duration-1000)
                initPlaying();
        }
    }

    private void initPitchGraph(ArrayList<Float> pitchList){
        for(int i=0; i<pitchList.size(); i++)
        {
            float pitchInHz = pitchList.get(i);

            if (pitchInHz < Constants.THRESHOLD_PITCH_MINIMUM || pitchInHz > Constants.THRESHOLD_PITCH_MAXIMUM)
                pitchInHz = 0;

            if(pitchInHz > 0)
            {
                if(contentToneGraphSeries == null)
                {
                    contentToneGraphSeries = new LineGraphSeries<>();
                    contentToneGraphSeries.setThickness(THICKNESS);
                    contentToneGraphSeries.setColor(PLAYED_COLOR);
                    contentsPitchGraph.addSeries(contentToneGraphSeries);
                }
                contentToneGraphSeries.appendData(new DataPoint(i+1, pitchInHz), false, 300);
            }
            else
            {
                contentToneGraphSeries = null;
                contentFlowGraphSeries.appendData(new DataPoint(i+1, pitchInHz), false, 300);
            }
        }

        contentsPitchGraph.getViewport().setMinX(0);
        contentsPitchGraph.getViewport().setMaxX(100);
        contentsPitchGraph.getViewport().setMinY(0);
        contentsPitchGraph.getViewport().setMaxY(300);
    }

    private void drawPitchGraph(float pitchInHz)
    {
        graphLastXValue += 1d;

        if (pitchInHz < Constants.THRESHOLD_PITCH_MINIMUM || pitchInHz > Constants.THRESHOLD_PITCH_MAXIMUM)
            pitchInHz = 0;

        if(pitchInHz > 0)
        {
            if(toneSeries == null)
            {
                toneSeries = new LineGraphSeries<>();
                toneSeries.setThickness(THICKNESS);
                contentsPitchGraph.addSeries(toneSeries);
            }
            toneSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), false, 300);
        }
        else
        {
            toneSeries = null;
            flowSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), false, 300);
        }

        if(graphLastXValue > GRAPH_X_LENGTH-PRE_SHOW)
        {
            contentsPitchGraph.getViewport().setMinX(graphLastXValue-GRAPH_X_LENGTH+PRE_SHOW);
            contentsPitchGraph.getViewport().setMaxX(graphLastXValue+PRE_SHOW);
        }
    }

    private void setProgressBar(long progress)
    {
        if(duration <= progress)
        {
            playSeekBar.setProgress(playSeekBar.getMax());
        }
        else
        {
            if(progress <= playSeekBar.getMax())
                playSeekBar.setProgress((int)progress);
            else
                playSeekBar.setProgress(playSeekBar.getMax());

            runningTimeTextView.setText(Tools.getTimeFormat(progress/1000));
        }
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

    private void play()
    {
        cacp.playContents(contents.getFilePath(), pdHandler);
        playedPitchList = new ArrayList<>();
        isPlaying = true;
        isPaused = false;
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        graphLastXValue = 0d;
        flowSeries.resetData(new DataPoint[]{});
        initSubtitle();
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
        initPlaying();
    }

    private void initPlaying()
    {
        isPlaying = false;
        isPaused = false;
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        currentTimeOffset = 0;
        lastPausedTimeOffset = 0;
    }

    private void initSubtitle()
    {
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

    private boolean checkMicPermission(){
        int recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(recordPermission == PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            return false;
        }
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
        String strFolderPath = Environment.getExternalStorageDirectory() + ChineseAccentComparison.CAPTURE_PATH;
        File folder = new File(strFolderPath);
        if(!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = contents.getTitle() + "_" + System.currentTimeMillis() + ".png";
        String strFilePath = strFolderPath + "/" + fileName;
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

            cacp.addHistory(new History(strFilePath, fileName, strFilePath));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean check_result = true;
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if(check_result) ;
        }
    }

    class ContentPitchLoadingTask extends AsyncTask<Void, Void, ArrayList<Float>>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(PracticeAccentActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("데이터 로딩중...");
            dialog.show();
        }

        @Override
        protected ArrayList<Float> doInBackground(Void... voids){
            ArrayList<Float> pitchList = cacp.getContentPitchList(contents.getFilePath());
            return pitchList;
        }

        @Override
        protected void onPostExecute(ArrayList<Float> pitchList) {
            super.onPostExecute(pitchList);

            initPitchGraph(pitchList);

            if(dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
    }
}
