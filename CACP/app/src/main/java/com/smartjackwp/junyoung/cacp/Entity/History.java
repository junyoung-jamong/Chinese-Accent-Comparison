package com.smartjackwp.junyoung.cacp.Entity;

public class History {
    String imagePath;
    String fileName;
    String contentName;

    public History(String imagePath, String fileName, String contentName){
        this.imagePath = imagePath;
        this.fileName = fileName;
        this.contentName = contentName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }


}
