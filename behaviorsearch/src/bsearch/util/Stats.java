package bsearch.util;

import java.util.Collections;
import java.util.List;

public class Stats {

    /** calculates sum of a list of numbers */
    public static double sum( List<Double> dlist )
    {
        double sum = 0;
        for (double d: dlist)
        {
            sum += d;
        }
        return sum ;
    }

    /** calculates arithmetic mean of a list of numbers */
    public static double mean( List<Double> dlist )
    {
        double sum = 0;
        for (double d: dlist)
        {
            sum += d;
        }
        return sum / dlist.size();
    }

    /** calculates arithmetic mean of a list of (not necessarily sorted) numbers*/
    public static double median(List<Double> dlist)
    {
        List<Double> sortedlist = new java.util.ArrayList<Double>(dlist);
        Collections.sort(sortedlist);
        int len = sortedlist.size();
        if (len % 2 == 0)
        {
            return (sortedlist.get(len / 2 - 1) + sortedlist.get(len / 2)) / 2;
        }
        else
        {
            return sortedlist.get(len / 2);
        }
    }

    /** calculates sample variance (as opposed to population variances) */
    public static double variance( List<Double> dlist )
    {
        double n = dlist.size();
        if (n <= 1)
        {
            return 0;
        }
        double mean = mean(dlist);

        double sum = 0;
        double sumsq = 0;
        for (double d: dlist)
        {
            sum += (d - mean);
            sumsq += (d - mean) * (d - mean);
        }
        return (sumsq - sum * sum / n) / (n - 1);
    }

    public static double stdev(List<Double> dlist)
    {
        return StrictMath.sqrt(variance(dlist));
    }
}
