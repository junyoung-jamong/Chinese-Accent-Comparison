package com.smartjackwp.junyoung.cacp.Utils;

public class Similarity {
    public static double JACCARD_SIM(GridMatrix m1, GridMatrix m2)
    {
        double intersection=0;
        double union=0;

        int[][] matrix1 = m1.matrix;
        int[][] matrix2 = m2.matrix;

        for(int i=0; i<matrix1.length; i++)
        {
            for(int j=0; j<matrix1[i].length; j++)
            {
                if((matrix1[i][j] > 0 || matrix2[i][j] > 0) && j != matrix1[i].length-1)
                   union++;
                if(matrix1[i][j] > 0 && matrix2[i][j] > 0 && j != matrix1[i].length-1)
                   intersection++;
            }
        }

        return intersection/union;
    }

    public static double GMED(GridMatrix m1, GridMatrix m2)
    {
        double sum = 0;

        int[][] matrix1 = m1.matrix;
        int[][] matrix2 = m2.matrix;

        for(int i=0; i<matrix1.length; i++)
        {
            for(int j=0; j<matrix1[i].length; j++)
            {
                sum += Math.pow(matrix1[i][j]-matrix2[i][j], 2);
            }
        }

        return Math.sqrt(sum);
    }

    //matrix-based Dynamic Time Warping
    public static double GMDTW(GridMatrix m1, GridMatrix m2)
    {
        int[][] matrix1 = m1.matrix;
        int[][] matrix2 = m2.matrix;

        double[][] distanceMarix = new double[matrix1.length][matrix2.length];

        for(int i=0; i<distanceMarix.length; i++)
        {
            for (int j=0; j<distanceMarix[i].length; j++)
            {
                double d = ED(matrix1[i], matrix2[j]);
                if(i==0 && j==0)
                    distanceMarix[i][j] = d;
                else if(i==0)
                    distanceMarix[i][j] = d + distanceMarix[i][j-1];
                else if(j==0)
                    distanceMarix[i][j] = d + distanceMarix[i-1][j];
                else
                    distanceMarix[i][j] = d + Math.min(Math.min(distanceMarix[i-1][j-1], distanceMarix[i-1][j]), distanceMarix[i][j-1]);
            }
        }

        if(matrix1.length > 0)
            return distanceMarix[matrix1.length-1][matrix2.length-1];
        else
            return Double.MAX_EXPONENT;
    }

    //standard Euclidean Distance
    public static double ED(int[] a, int[] b)
    {
        int sum = 0;
        for(int i=0; i<a.length; i++)
        {
            sum += Math.pow(a[i]-b[i], 2);
        }

        return Math.sqrt(sum);
    }

    //standard Euclidean Distance
    public static double ED(double[] a, double[] b)
    {
        double sum = 0;
        for(int i=0; i<a.length; i++)
        {
            sum += Math.pow(a[i]-b[i], 2);
        }

        return Math.sqrt(sum);
    }
}
