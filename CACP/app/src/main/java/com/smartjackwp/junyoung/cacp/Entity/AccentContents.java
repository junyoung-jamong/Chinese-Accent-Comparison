package com.smartjackwp.junyoung.cacp.Entity;

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
    private String playTime; //재생시간

    private ArrayList<Float> playedPitchList;
    private ArrayList<Float> recordedPitchList;

    public AccentContents(String filePath, String title, String description)
    {
        this.filePath = filePath;
        this.title = title;
        this.description = description;
    }

    public AccentContents(int id, String filePath, String title, String description)
    {
        this.id = id;
        this.filePath = filePath;
        this.title = title;
        this.description = description;
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

    public String getPlayTime(){
        return this.playTime;
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

}
