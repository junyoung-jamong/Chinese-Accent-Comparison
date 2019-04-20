package com.smartjackwp.junyoung.cacp;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.smartjackwp.junyoung.cacp.Database.CacpDBManager;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.Entity.History;
import com.smartjackwp.junyoung.cacp.Interfaces.OnCapturedHistoryListener;
import com.smartjackwp.junyoung.cacp.Utils.GridMatrix;
import com.smartjackwp.junyoung.cacp.Utils.Similarity;
import com.smartjackwp.junyoung.cacp.Utils.Normalization;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.smartjackwp.junyoung.cacp.Interfaces.OnMeasuredSimilarityListener;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;

public class ChineseAccentComparison {
    public enum PLAYER_STATE {NONE, PLAY, PAUSE, RESUME, STOP}
    public static ChineseAccentComparison cac;

    Context mContext;
    CacpDBManager cacpDB;

    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;
    AudioProcessor playerProcessor;
    AudioProcessor recordProcessor;
    AudioProcessor pitchProcessor;
    Thread audioThread;
    Handler handler;

    private final int GRID_M = 8;
    private final int GRID_N = 8;
    private static final String tempFileName = "cacp_temp.wav";
    public static final String CAPTURE_PATH = "/[ToneViewer]";

    private ArrayList<AccentContents> contentsList;
    private ArrayList<History> historyList;
    private PLAYER_STATE playerState = PLAYER_STATE.NONE;

    OnMeasuredSimilarityListener onMeasuredSimilarityListener;
    OnCapturedHistoryListener onCapturedHistoryListener;

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

        initHistories();
    }


    //Contents CRUD 관련
    public ArrayList<AccentContents> getContentsList()
    {
        if(contentsList == null)
            contentsList = cacpDB.getDAO().selectContents();

        return this.contentsList;
    }

    public AccentContents findContentsById(int id){
        if(contentsList != null)
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
    public ArrayList<Float> getContentPitchList(String filePath){
        final ArrayList<Float> pitchList = new ArrayList<>();

        initFilePitchDispatcher(filePath, new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent audioEvent) {
                float pitchInHz = res.getPitch();
                if (pitchInHz < Constants.THRESHOLD_PITCH_MINIMUM || pitchInHz > Constants.THRESHOLD_PITCH_MAXIMUM)
                    pitchInHz = 0;

                pitchList.add(pitchInHz);
            }
        });

        if(dispatcher != null){
            audioThread = new Thread(dispatcher, "Content_Pitch_Thread");
            audioThread.start();
            try{
                audioThread.join();
                return pitchList;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            return null;
        }
    }

    public void playContents(String filePath, PitchDetectionHandler pdHandler)
    {
        playerState = PLAYER_STATE.PLAY;

        initFileDispatcher(filePath, pdHandler);

        if(dispatcher != null)
        {
            audioThread = new Thread(dispatcher, "Contents Play Thread");
            audioThread.start();
        }
    }

    public void pauseContents()
    {
        playerState = PLAYER_STATE.PAUSE;

        if(dispatcher != null && !dispatcher.isStopped())
            dispatcher.stop();
    }

    public void stopContents()
    {
        playerState = PLAYER_STATE.STOP;

        if(dispatcher != null && !dispatcher.isStopped())
            dispatcher.stop();
    }

    public void resumeContents(String filePath, PitchDetectionHandler pdHandler, double startTimeOffset)
    {
        playerState = PLAYER_STATE.RESUME;

        initFileDispatcher(filePath, pdHandler, startTimeOffset);

        if(dispatcher != null)
        {
            audioThread = new Thread(dispatcher, "Contents Play Thread");
            audioThread.start();
        }
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
        final ArrayList<Float> playedPitchList =(ArrayList<Float>)contents.getPlayedPitchList().clone();
        final ArrayList<Float> recordedPitchList = (ArrayList<Float>)contents.getRecordedPitchList().clone();
        if(playedPitchList != null && recordedPitchList != null)
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final ArrayList<Float> normPlayedPitchList = Normalization.featureScaling(playedPitchList);
                    final ArrayList<Float> normRecordedPitchList = Normalization.featureScaling(recordedPitchList);

                    if(!hasPitch(recordedPitchList))
                    {
                        onMeasuredSimilarityListener.onMeasured(0, normPlayedPitchList, normRecordedPitchList);
                        return;
                    }

                    //GridMatrix playedGM = new GridMatrix(GRID_M, GRID_N, trimTimeSeries((ArrayList<Float>)normPlayedPitchList.clone()));
                    //GridMatrix recordedGM = new GridMatrix(GRID_M, GRID_N, trimTimeSeries((ArrayList<Float>)normRecordedPitchList.clone()));

                    GridMatrix playedGM = new GridMatrix(GRID_M, GRID_N, normPlayedPitchList);
                    GridMatrix recordedGM = new GridMatrix(GRID_M, GRID_N, normRecordedPitchList);

                    //double dist1 = Similarity.GMED(playedGM, recordedGM);
                    //double dist = Similarity.GMDTW(playedGM, recordedGM);
                    //double dist = (dist1+dist2)/2;
                    //dist = dist/((normPlayedPitchList.size()+normRecordedPitchList.size())/2);
                    //double sim = 100/(1+dist);

                    double sim= 100*Similarity.JACCARD_SIM(playedGM, recordedGM);

                    if(sim > 40)
                        sim += 20*(sim/100);
                    sim = Math.min(sim, 100);

                    final double score = sim;

                    if(onMeasuredSimilarityListener != null)
                    {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onMeasuredSimilarityListener.onMeasured(score, normPlayedPitchList, normRecordedPitchList);
                            }
                        });
                    }
                }
            });
            thread.run();
        }
    }

    private boolean hasPitch(ArrayList<Float> ts){
        boolean hasPitch = false;

        for(Float v: ts)
            if(v > 0)
                return true;

        return hasPitch;
    }

    public void finishContents()
    {
        releaseDispatcher();
    }

    public void setOnMeasuredSimilarityListener(OnMeasuredSimilarityListener listener){
        this.onMeasuredSimilarityListener = listener;
    }

    public void setOnCapturedHistoryListener(OnCapturedHistoryListener listener){
        this.onCapturedHistoryListener = listener;
    }

    public ArrayList<History> getHistories(){
       if(historyList == null)
           initHistories();

        return historyList;
    }

    public void addHistory(History history){
        if(historyList != null){
            historyList.add(0, history);
            if(onCapturedHistoryListener != null){
                onCapturedHistoryListener.onCaptured(history);
            }
        }
    }

    private void initHistories(){
        String path = Environment.getExternalStorageDirectory() + CAPTURE_PATH;
        File directory = new File(path);
        historyList = new ArrayList<>();

        if(directory.exists())
        {
            File[] files = directory.listFiles();

            for(File f: files){
                historyList.add(new History(f.getPath(), f.getName(), f.getAbsolutePath()));
            }
        }
    }

    private void initMicDispatcher(PitchDetectionHandler pdHandler)
    {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        try {
            File file = new File(Environment.getExternalStorageDirectory(), tempFileName);

            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            recordProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFilePitchDispatcher(String filePath, PitchDetectionHandler pdHandler)
    {
        try{
            releaseDispatcher();
            new AndroidFFMPEGLocator(mContext);
            dispatcher = AudioDispatcherFactory.fromPipe(filePath, 22050, 1024, 0);

            pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdHandler);
            dispatcher.addAudioProcessor(pitchProcessor);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initFileDispatcher(String filePath, PitchDetectionHandler pdHandler) //Pitcher 초기화
    {
        try{
            releaseDispatcher();

            //File file = new File(filePath);
            //FileInputStream fileInputStream = new FileInputStream(file);
            //dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

            new AndroidFFMPEGLocator(mContext);
            dispatcher = AudioDispatcherFactory.fromPipe(filePath, 22050, 1024, 0);

            playerProcessor = new AndroidAudioPlayer(tarsosDSPAudioFormat, 5000, AudioManager.STREAM_MUSIC);
            dispatcher.addAudioProcessor(playerProcessor);

            pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

        }catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void initFileDispatcher(String filePath, PitchDetectionHandler pdHandler, double startTimeOffset) //Pitcher 초기화
    {
        try{
            releaseDispatcher();

            //File file = new File(filePath);
            //FileInputStream fileInputStream = new FileInputStream(file);
            //dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

            new AndroidFFMPEGLocator(mContext);
            //dispatcher = AudioDispatcherFactory.fromPipe(filePath, 22050, 1024, 0);
            PipedAudioStream f = new PipedAudioStream(filePath);
            TarsosDSPAudioInputStream audioStream = f.getMonoStream(22050, startTimeOffset);
            dispatcher = new AudioDispatcher(audioStream, 1024, 0);
            Log.e("initFileDispatcher()", "startTimeOffset : "+ startTimeOffset);

            playerProcessor = new AndroidAudioPlayer(tarsosDSPAudioFormat, 5000, AudioManager.STREAM_MUSIC);
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

    private ArrayList<Float> trimTimeSeries(ArrayList<Float> ts)
    {
        if(ts != null && ts.size() > 0)
        {
            for(int i=ts.size(); i>0; i--)
            {
                if(ts.get(i-1) > 0)
                    break;
                else
                    ts.remove(i-1);

            }

            while(true && ts.size() > 0)
            {
                if(ts.get(0) > 0)
                    break;
                else
                    ts.remove(0);
            }
            return ts;
        }
        else
            return ts;

    }
}
