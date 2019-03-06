package com.smartjackwp.junyoung.cacp.Utils;

public class Tools {
    public static String getTimeFormat(long time)
    {
        return convertFormat(time);
    }

    public static String getTimeFormat(double time)
    {
        time = (long)time;
        return convertFormat(time);
    }

    private static String convertFormat(double time)
    {
        String timeFormat = "";

        int minutes = (int)(time/60);
        int seconds = (int)(time%60);

        /*
        if(minutes < 10)
            timeFormat += "0" + minutes + ":";
        else
        */
        timeFormat += minutes + ":";

        if(seconds < 10)
            timeFormat += "0" + seconds;
        else
            timeFormat += seconds;

        return timeFormat;
    }
}
