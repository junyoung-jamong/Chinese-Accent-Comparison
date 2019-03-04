package com.smartjackwp.junyoung.cacp.Entity;

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

    public AccentContents(String filePath, String title, String description)
    {
        this.filePath = filePath;
        this.title = title;
        this.description = description;
    }

    public String getFilePath()
    {
        return this.filePath;
    }

    public int getID()
    {
        return this.getID();
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

}
