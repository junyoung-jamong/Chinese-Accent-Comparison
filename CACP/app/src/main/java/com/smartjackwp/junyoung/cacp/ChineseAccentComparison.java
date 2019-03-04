package com.smartjackwp.junyoung.cacp;

import com.smartjackwp.junyoung.cacp.Entity.AccentContents;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

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

public class ChineseAccentComparison {
    public static ChineseAccentComparison cac;

    ArrayList<AccentContents> contentsList;

    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    Thread audioThread;

    private ChineseAccentComparison(){
        init();
    }

    public static ChineseAccentComparison getInstance()
    {
        if(cac == null)
            cac = new ChineseAccentComparison();

        return cac;
    }

    private void init()
    {
        contentsList = new ArrayList<>();

        //init Audio format
        tarsosDSPAudioFormat=new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));
    }


    public ArrayList<AccentContents> getContentsList()
    {
        return this.contentsList;
    }

    public void saveSoundFile(String filePath, String title, String description)
    {

    }

    public void playContents(String filePath, PitchDetectionHandler pdHandler)
    {
        initDispatcher(filePath, pdHandler);

        audioThread = new Thread(dispatcher, "Contents Play Thread");
        audioThread.start();
    }

    public void pauseContents()
    {

    }

    public void finishContents()
    {

    }

    private void initDispatcher(String filePath, PitchDetectionHandler pdHandler) //Pitcher 초기화
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

}
