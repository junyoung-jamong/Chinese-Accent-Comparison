package com.smartjackwp.junyoung.cacp.Utils;

import java.util.ArrayList;

public class GridMatrix {
    public int[][] matrix;
    int m;
    int n;

    public GridMatrix(int m, int n)
    {
        this.m = m;
        this.n = n;
        this.matrix =new int[m][n];
    }

    public GridMatrix(int m, int n, ArrayList<Float> ts)
    {
        this.m = m;
        this.n = n;
        this.matrix =new int[m][n];
        setTimeSeries(ts);
    }

    public void setTimeSeries(ArrayList<Float> ts)
    {
        int T = ts.size();

        double height = 1.0/m;
        double width = (double)T/n;
        //double height = Math.round((1.0/m)*100)/100.0;
        //double width =  Math.round(((double)T/n)*100)/100.0;

        for(int idx=0; idx<T; idx++)
        {
            double x = ts.get(idx);
            double t = idx+1;

            int i = (int)((1-x)/height);
            if (i == m)
                i = m-1;

            int j;
            if((int)(t/width) == Math.round((t/width)*1000000)/1000000.0)
                j = (int)(t/width)-1;
            else
                j = (int)(t/width);

            matrix[i][j]++;
        }
    }

    public void print()
    {
        for (int i=0; i<matrix.length; i++)
        {
            for(int j=0; j<matrix[i].length; j++)
                System.out.print(matrix[i][j]+ " ");
            System.out.println();
        }
    }
}
