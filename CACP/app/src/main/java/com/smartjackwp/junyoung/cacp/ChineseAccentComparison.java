package com.smartjackwp.junyoung.cacp;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.smartjackwp.junyoung.cacp.Database.CacpDBManager;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.Utils.GridMatrix;
import com.smartjackwp.junyoung.cacp.Utils.Similarity;
import com.smartjackwp.junyoung.cacp.Utils.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;

import Interfaces.OnMeasuredSimilarityListener;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;

public class ChineseAccentComparison {
    public static ChineseAccentComparison cac;
    CacpDBManager cacpDB;

    ArrayList<AccentContents> contentsList;

    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    Thread audioThread;

    Context mContext;

    String tempFileName = "cacp_temp.wav";

    private final int GRID_M = 5;
    private final int GRID_N = 10;

    OnMeasuredSimilarityListener onMeasuredSimilarityListener;

    Handler handler;

    private ChineseAccentComparison(Context context){
        this.mContext = context;
        init();
    }

    public static ChineseAccentComparison getInstance(Context context)
    {
        if(cac == null)
            cac = new ChineseAccentComparison(context);

        return cac;
    }

    private void init()
    {
        cacpDB = CacpDBManager.getInstance(mContext);
        handler = new Handler();

        //init Audio format
        tarsosDSPAudioFormat=new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));
    }


    //Contents CRUD 관련
    public ArrayList<AccentContents> getContentsList()
    {
        if(contentsList == null)
            contentsList = cacpDB.getDAO().selectContents();

        return this.contentsList;
    }

    public AccentContents findContentsById(int id){
        for(int i=0; i<contentsList.size(); i++)
            if(contentsList.get(i).getId() == id)
                return contentsList.get(i);

        return null;
    }

    public int saveContents(AccentContents accentContents)
    {
        int id = cacpDB.getDAO().insertContents(accentContents);
        accentContents.setId(id);
        contentsList.add(accentContents);
        return id;
    }

    public boolean deleteContents(AccentContents accentContents)
    {
        if(cacpDB.getDAO().deleteContents(accentContents))
        {
            contentsList.remove(accentContents);
            return true;
        }
        else
            return false;
    }

    //Contents 이용관련
    public void playContents(String filePath, PitchDetectionHandler pdHandler)
    {
        initFileDispatcher(filePath, pdHandler);

        if(dispatcher != null)
        {
            audioThread = new Thread(dispatcher, "Contents Play Thread");
            audioThread.start();
        }
    }

    public void pauseContents()
    {
        if(!dispatcher.isStopped())
            dispatcher.stop();
    }

    public void resumeContents()
    {

    }

    public void startRecord(PitchDetectionHandler pdHandler)
    {
        initMicDispatcher(pdHandler);
        audioThread = new Thread(dispatcher, "Record Thread");
        audioThread.start();
    }

    public void stopRecord()
    {
        releaseDispatcher();
    }

    public void measureSimilarity(AccentContents contents)
    {
        final ArrayList<Float> playedPitchList = contents.getPlayedPitchList();
        final ArrayList<Float> recordedPitchList = contents.getRecordedPitchList();
        if(playedPitchList != null && recordedPitchList != null)
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Float> normPlayedPitchList = Tools.featureScaling(playedPitchList);
                    final ArrayList<Float> normRecordedPitchList = Tools.featureScaling(recordedPitchList);

                    GridMatrix playedGM = new GridMatrix(GRID_M, GRID_N, normPlayedPitchList);
                    GridMatrix recordedGM = new GridMatrix(GRID_M, GRID_N, normRecordedPitchList);

                    //double dist1 = Similarity.GMED(playedGM, recordedGM);
                    //double dist2 = Similarity.GMDTW(playedGM, recordedGM);
                    //double dist = (dist1+dist2)/2;
                    //dist = dist/normPlayedPitchList.size();
                    //final double sim = 100/(1+dist);

                    final double sim= 100*Similarity.JACCARD_SIM(playedGM, recordedGM);

                    if(onMeasuredSimilarityListener != null)
                    {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onMeasuredSimilarityListener.onMeasured(sim, normPlayedPitchList, normRecordedPitchList);
                            }
                        });
                    }
                }
            });
            thread.run();
        }
    }

    public void finishContents()
    {
        releaseDispatcher();
    }

    private void initMicDispatcher(PitchDetectionHandler pdHandler)
    {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        try {
            File file = new File(Environment.getExternalStorageDirectory(), tempFileName);

            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            AudioProcessor recordProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFileDispatcher(String filePath, PitchDetectionHandler pdHandler) //Pitcher 초기화
    {
        try{
            releaseDispatcher();

            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

            AudioProcessor playerProcessor = new AndroidAudioPlayer(tarsosDSPAudioFormat, 2048, 0);
            dispatcher.addAudioProcessor(playerProcessor);

            pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

        }catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void releaseDispatcher()
    {
        if(dispatcher != null)
        {
            if(!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    public void setOnMeasuredSimilarityListener(OnMeasuredSimilarityListener listener){
        this.onMeasuredSimilarityListener = listener;
    }

}
