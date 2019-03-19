package com.smartjackwp.junyoung.cacp.Entity;

import android.util.Log;

public class SubtitleUnit {
    int index;
    String startTime;
    String endTime;
    String[] contents;

    long startTimeMillisecond;
    long endTimeMillisecond;

    public SubtitleUnit(int index, String startTime, String endTime, String[] contents)
    {
        this.index = index;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;

        this.startTimeMillisecond = timeToMillisecond(startTime);
        this.endTimeMillisecond = timeToMillisecond(endTime);

        Log.e("endTimeMillisecond", ""+endTimeMillisecond);
    }

    public String[] getContents(){
        return this.contents;
    }

    public long getStartTimeMillisecond(){
        return this.startTimeMillisecond;
    }

    public long getEndTimeMillisecond(){
        return this.endTimeMillisecond;
    }

    private long timeToMillisecond(String time)
    {
        time = time.trim();
        String[] times = time.split(":");
        long millisecond = 0;
        if(times.length == 3)
        {
            try{
                millisecond += Long.parseLong(times[0])*3600 * 1000;
                millisecond += Long.parseLong(times[1])*60 * 1000;
                millisecond += Long.parseLong(times[2].replace(",", ""));
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return millisecond;
    }
}
