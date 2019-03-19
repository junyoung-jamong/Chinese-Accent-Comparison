package com.smartjackwp.junyoung.cacp.Entity;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

//원어민 억양 음성 파일 컨텐츠
public class AccentContents {
    public static final String _ID = "id";
    public static final String FILE_PATH = "file_path";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    private int id; //음성 파일 고유값
    private String filePath; //음성 파일 경로
    private String title; //파일 제목
    private String description; //파일 설명
    private double duration; //재생시간 second
    private Subtitle subtitle; //자막

    private ArrayList<Float> playedPitchList;
    private ArrayList<Float> recordedPitchList;

    public AccentContents(String filePath, String title, String description)
    {
        this.filePath = filePath;
        this.title = title;
        this.description = description;
        this.duration = getAudioDuration(filePath);
        this.subtitle = getAudioSubtitle(filePath);
    }

    public AccentContents(int id, String filePath, String title, String description)
    {
        this.id = id;
        this.filePath = filePath;
        this.title = title;
        this.description = description;
        this.duration = getAudioDuration(filePath);
        this.subtitle = getAudioSubtitle(filePath);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getFilePath()
    {
        return this.filePath;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public double getDuration(){
        return this.duration;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

    public void setPlayedPitchList(ArrayList<Float> playedPitchList) {
        this.playedPitchList = playedPitchList;
    }

    public ArrayList<Float> getPlayedPitchList(){
        return this.playedPitchList;
    }

    public void setRecordedPitchList(ArrayList<Float> recordedPitchList){
        this.recordedPitchList = recordedPitchList;
    }

    public ArrayList<Float> getRecordedPitchList(){
        return this.recordedPitchList;
    }

    public Subtitle getSubtitle(){
        return this.subtitle;
    }

    private double getAudioDuration(String filePath)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        double duration = Double.parseDouble(durationString) + 1000;
        return duration/1000;
    }

    private Subtitle getAudioSubtitle(String filePath)
    {
        String[] splitPath = filePath.split("\\.");

        if(splitPath.length > 0)
        {
            String extension = splitPath[splitPath.length-1];
            String subtitlePath = filePath.substring(0, filePath.length() - extension.length()) + "srt";
            if(new File(subtitlePath).exists())
                return new Subtitle(subtitlePath);
        }

        return null;
    }

}
