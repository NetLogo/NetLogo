import java.awt.Color;
import java.lang.reflect.Field;

import org.nlogo.api.ExtensionException;

abstract class Scheme
{
    public static int[][] getRGBArray(String schemeTypeName,
                                      String colorSchemeName,
                                      int colorSchemeSize)
    throws ExtensionException
    {
        final int maxColorScheme = 12;
        int[][][] colorScheme = new int[maxColorScheme][][];
        int[][] colorLegend = null;
        Class<? extends ColorSchemes> selectedClass = null;

        //  Select the Scheme with schemeTypeName
        if (schemeTypeName == "Sequential")
        {selectedClass = Sequential.class;}
        else if (schemeTypeName == "Divergent")
        {selectedClass = Divergent.class;}
        else if (schemeTypeName == "Qualitative")
        {selectedClass = Qualitative.class;}
        else
        {
            throw new ExtensionException(
                "Your Scheme Type name was " + schemeTypeName +
                "your argument can only be : " +
                "Sequential, Divergent or Qualitative" );
        }

        // Get ColorScheme array with colorSchemeName
        try
        {
            Field field = selectedClass.getDeclaredField(colorSchemeName);
            colorScheme = (int[][][]) field.get(null);
        }
        catch (NoSuchFieldException e)
        {
            throw new ExtensionException(
                               "The color scheme " + colorSchemeName +
                               " does not exist. " +
                               "Check the spelling of your color Scheme Name");
        }
        catch (IllegalArgumentException e) {
            throw new ExtensionException(
                    "The scheme " + colorSchemeName + " is not a string. ");
        }
        catch (IllegalAccessException e)
        {
            throw new ExtensionException(
                "the currently executing method does not have access to the" +
                " definition of the specified field");
        }

        // Get Legend with colorSchemeSize from colorScheme
        int i = 0;
        while (colorScheme[i].length != colorSchemeSize)
        {
            i++;
            if (colorScheme[i].length == colorSchemeSize)
            {
                colorLegend = new int[colorSchemeSize][];
                colorLegend = colorScheme[i];
            }
        }
        if (colorLegend == null)
        {
            throw new ExtensionException("The Selected Color Scheme" +
                                               colorSchemeName + "does not exist with the given size: " +
                                               colorSchemeSize + "Try a lower class size.");
        }
        return colorLegend;
    }

    public static Color[] getColorArray(String schemeTypeName,
                                 String colorSchemeName,
                                        int colorSchemeSize)
    throws ExtensionException
    {
        Color[] chosenColorLegend = new Color[colorSchemeSize];
        int[][] colorArray = getRGBArray(       schemeTypeName,
                                                colorSchemeName,
                                                colorSchemeSize);

        // Create a Color[] with the rgb values of the int [][]
        for (int i = 0; i < colorArray.length; i++)
        {
            chosenColorLegend[i] = new Color(colorArray [i][0],
                                             colorArray[i][1], colorArray [i][2]);
        }
        return chosenColorLegend;
    }

    public static int[] getIntArray (String schemeTypeName,
                                     String colorSchemeName,
                                     int colorSchemeSize)
    throws ExtensionException
    {
        // note that this isn't doing anything with transparency
        int colorInt[] = new int[colorSchemeSize];
        int[][] colorArray = getRGBArray(   schemeTypeName,
                                            colorSchemeName,
                                            colorSchemeSize);
        for (int i = 0; i < colorArray.length; i++)
        {
             colorInt[i] = (colorArray[i][0] << 16) |
                (colorArray[i][1] << 8 ) |
                colorArray[i][2];
        }
        return colorInt;
    }
}
