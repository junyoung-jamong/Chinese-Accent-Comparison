package com.smartjackwp.junyoung.functionalsampleproject;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    Button recordButton;

    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    Thread audioThread;

    GraphView realTimeGraph;
    GraphView staticGraph;

    Boolean recordState = false;

    private LineGraphSeries<DataPoint> realTimeSeries; //Series for real-time realTimeGraph
    private LineGraphSeries<DataPoint> staticSeries; //Series for static realTimeGraph
    private double graphLastXValue = 1d;
    private double staticGraphLastXValue = 1d;

    ArrayList<Point> recordedPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_real_time_graph);

        realTimeGraph = findViewById(R.id.realTimeGraph);
        staticGraph = findViewById(R.id.staticGraph);
        staticGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        staticGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        realTimeSeries = new LineGraphSeries<>();
        realTimeSeries.setColor(Color.rgb(0xF1,0x70,0x68));
        realTimeSeries.setDataPointsRadius(50);
        realTimeSeries.setThickness(10);
        realTimeGraph.addSeries(realTimeSeries);
        realTimeGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        realTimeGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        realTimeGraph.getViewport().setXAxisBoundsManual(true);
        realTimeGraph.getViewport().setMinX(0);
        realTimeGraph.getViewport().setMaxX(100);
        realTimeGraph.getViewport().setMinY(-1);
        realTimeGraph.getViewport().setMaxY(300);

        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recordState)
                {
                    staticGraph.removeAllSeries();
                    staticSeries = new LineGraphSeries<>();
                    staticSeries.setColor(Color.BLUE);
                    staticSeries.setDataPointsRadius(50);
                    staticSeries.setThickness(10);

                    for(int i=0; i<recordedPoints.size(); i++)
                        staticSeries.appendData(new DataPoint(recordedPoints.get(i).t, recordedPoints.get(i).x), true, 300);

                    staticGraph.addSeries(staticSeries);
                    recordButton.setText("시작");
                    recordState = !recordState;
                }
                else
                {
                    recordedPoints = new ArrayList<>();
                    staticGraphLastXValue = 1d;
                    recordButton.setText("중지");
                    recordState = !recordState;
                }
            }
        });

        initPitcher();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDispatcher();
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
                        processPitch(pitchInHz);
                    }
                });
            }
        };
        pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    public void processPitch(float pitchInHz){
        if (pitchInHz < 0)
            pitchInHz = 20;

        graphLastXValue += 1d;
        realTimeSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), true, 300);

        if(recordState)
        {
            staticGraphLastXValue += 1d;
            recordedPoints.add(new Point(staticGraphLastXValue, pitchInHz));
        }
    }

    public void releaseDispatcher()
    {
        if(dispatcher != null)
        {
            if(!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    class Point{
        public double t;
        public float x;

        public Point(double t, float x)
        {
            this.t = t;
            this.x = x;
        }
    }
}