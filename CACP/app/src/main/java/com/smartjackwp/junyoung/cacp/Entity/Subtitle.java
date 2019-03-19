package com.smartjackwp.junyoung.cacp.Entity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Subtitle {
    ArrayList<SubtitleUnit> subtitleUnits;

    public Subtitle(ArrayList<SubtitleUnit> subtitleUnits){
        this.subtitleUnits = subtitleUnits;
    }

    public Subtitle(String filePath)
    {
        if(filePath != null && filePath.length() > 0)
        {
            readSrtFile(filePath);
        }
    }

    public ArrayList<SubtitleUnit> getSubtitleUnits(){
        return this.subtitleUnits;
    }

    private void readSrtFile(String filePath){
        try{
            File file = new File(filePath);
            if(file.exists())
            {
                ArrayList<SubtitleUnit> subtitleUnits = new ArrayList<>();

                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                ArrayList<String> unitList = new ArrayList<>();
                while(true)
                {
                    line=bufferedReader.readLine();

                    if(line == null || line.trim().length() == 0)
                    {
                        if(unitList.size() >= 3)
                        {
                            SubtitleUnit subtitleUnit = parseSrt(unitList);
                            if(subtitleUnit != null)
                                subtitleUnits.add(subtitleUnit);
                        }

                        if(line == null)
                            break;
                        unitList = new ArrayList<>();
                    }
                    else
                    {
                        unitList.add(line);
                    }
                }

                if(subtitleUnits.size() > 0)
                    this.subtitleUnits = subtitleUnits;
                bufferedReader.close();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private SubtitleUnit parseSrt(ArrayList<String> unitList)
    {
        int size = unitList.size();
        if(size >= 3)
        {
            int index = -1;
            String startTime = null;
            String endTime = null;
            String[] contents = new String[size-2];

            for(int i=0; i<size; i++)
            {
                String s = unitList.get(i).trim();
                switch (i){
                    case 0:
                        try{
                            //index = Integer.parseInt(s);
                        }catch(Exception e){
                            e.printStackTrace();
                            //return null;
                        }
                        break;
                    case 1:
                        String[] duration = s.split("-->");
                        if(duration.length >= 2)
                        {
                            startTime = duration[0].trim();
                            endTime = duration[1].trim();
                        }
                        else
                            return null;
                        break;
                    default:
                        contents[i-2] = s;
                }
            }
            return new SubtitleUnit(index, startTime, endTime, contents);
        }
        return null;
    }
}
