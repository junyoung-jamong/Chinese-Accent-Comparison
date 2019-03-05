package com.smartjackwp.junyoung.cacp.Utils;

import java.util.ArrayList;

public class Tools {
    public static ArrayList<Float> featureScaling(ArrayList<Float> ts)
    {
        ArrayList<Float> normTS = new ArrayList<>();

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for(int j=0; j<ts.size(); j++)
        {
            float v = ts.get(j);
            if (max < v)
                max = v;
            if (min > v)
                min = v;
        }

        float range = max-min;
        for(int j=0; j<ts.size(); j++)
        {
            float v = ts.get(j);
            //double normalizedX = Math.round(((x-min)/range)*100)/100.0;
            float normalizedV = (v-min)/range;
            normTS.add(normalizedV);
        }

        return normTS;
    }
}
