package com.smartjackwp.junyoung.functionalsampleproject;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchRealTimeGraphActivity extends AppCompatActivity {
    Button button;
    Button stopButton;
    Button playButton;

    MediaPlayer player;
    MediaRecorder recorder;
    String fileName;

    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    Thread audioThread;

    Boolean state = false;

    GraphView graph;

    private LineGraphSeries<DataPoint> mSeries1;
    private double graphLastXValue = 1d;

    int windowSize = 10;
    ArrayList<Float> pitchWindow = new ArrayList<>();
    float pitch_avg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_real_time_graph);

        button = findViewById(R.id.recordButton);
        graph = findViewById(R.id.realTimeGraph);

        mSeries1 = new LineGraphSeries<>();
        mSeries1.setColor(Color.rgb(0xF1,0x70,0x68));
        graph.addSeries(mSeries1);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);
        graph.getViewport().setMinY(-1);
        graph.getViewport().setMaxY(300);

        initPitcher();
    }

    public void initPitcher()
    {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(state)
                        {
                            processPitch(pitchInHz);
                        }
                    }
                });
            }
        };
        pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
        state = true;
    }

    public void processPitch(float pitchInHz){
        if (pitchInHz < 0)
            pitchInHz = 20;

        float pitchSum = pitch_avg*pitchWindow.size();
        pitchSum += pitchInHz;
        pitchWindow.add(pitchInHz);
        if(pitchWindow.size() > windowSize)
        {
            float fistPitch = pitchWindow.remove(0);
            pitchSum -= fistPitch;
        }
        if (pitchWindow.size() > 0)
            pitch_avg = pitchSum/pitchWindow.size();

        graphLastXValue += 1d;
        mSeries1.appendData(new DataPoint(graphLastXValue, pitch_avg), true, 300);
    }
}
