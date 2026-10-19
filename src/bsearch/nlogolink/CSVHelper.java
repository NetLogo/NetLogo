package bsearch.nlogolink;

import java.util.List;

/** Re-use the CSV output code that NetLogo uses */
public class CSVHelper
{
    public static String headerRow(String [] columnNames)
    {
        return org.nlogo.api.Dump.csv().headerRow(columnNames);
    }
    public static String headerRow(List<String> columnNames)
    {
        return org.nlogo.api.Dump.csv().headerRow(columnNames.toArray(new String[0]));
    }
    public static String dataRow(Object[] data)
    {
        return org.nlogo.api.Dump.csv().dataRow(data);
    }
    public static String dataRow(List<Object> data)
    {
        return org.nlogo.api.Dump.csv().dataRow(data.toArray());
    }
}
