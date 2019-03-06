package com.smartjackwp.junyoung.cacp.Entity;

import android.media.MediaMetadataRetriever;

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
    private double duration; //재생시간

    private ArrayList<Float> playedPitchList;
    private ArrayList<Float> recordedPitchList;

    public AccentContents(String filePath, String title, String description)
    {
        this.filePath = filePath;
        this.title = title;
        this.description = description;
        this.duration = getAudioDuration(filePath);
    }

    public AccentContents(int id, String filePath, String title, String description)
    {
        this.id = id;
        this.filePath = filePath;
        this.title = title;
        this.description = description;
        this.duration = getAudioDuration(filePath);
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

    public void setRecordedPitchList(ArrayList<Float> recordedPitchList)
    {
        this.recordedPitchList = recordedPitchList;
    }

    public ArrayList<Float> getRecordedPitchList(){
        return this.recordedPitchList;
    }

    private double getAudioDuration(String filePath)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long lDuration = Long.parseLong(duration);

        return lDuration/1000;
    }

}
