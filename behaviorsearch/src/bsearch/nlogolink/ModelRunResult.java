package bsearch.nlogolink;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
/**
 * Class for storing numeric results from a model run.  It can store multiple measures.
 */
public class ModelRunResult {
    private LinkedHashMap<String,LinkedList<Double>> measures = new LinkedHashMap<String,LinkedList<Double>>();
    private long randomSeed;

    public ModelRunResult(long randomSeed)
    {
        this.randomSeed = randomSeed;
    }

    public void addResult(String measure, Double val)
    {
        if (!measures.containsKey(measure))
        {
            measures.put(measure, new LinkedList<Double>());
        }
        measures.get(measure).add(val);
    }
    public boolean isEmpty()
    {
        return measures.isEmpty();
    }
    public ArrayList<String> getMeasureNames()
    {
        return new ArrayList<String>(measures.keySet());
    }
    public LinkedList<Double> getTimeSeriesForMeasure(String measure)
    {
        return measures.get(measure);
    }
    public LinkedList<Double> getPrimaryTimeSeries()
    {
        return measures.get(getMeasureNames().get(0));
    }
    public long getRandomSeed()
    {
        return randomSeed;
    }

    /*
    public Object getFinalResult(String measure)
    {
        List<Object> history = measures.get(measure);
        return history.get(history.size() - 1);
    }
    */

}
