/**
* Defines common behaviors for the Sequencial, Diverging and Qualitative class. *
* Note that this class is inherited by the Sequential, Divergent or Qualitative classes, and
* makes heavy use of Reflexion to grab the colors from them
*/

import java.awt.Color;
import java.lang.reflect.Field;

import org.nlogo.api.ExtensionException;

public abstract class ColorSchemes {

  //TODO: calculate the maxColorSchem in function of the getMaximumLegendSize
  //      this means that maxColorScheme will be 9, 11 or 12 depending upon what
  //      scheme will be invoqued.
  private static final int maxColorScheme = 12;
  private static final String[] schemeTypes = {"Sequential", "Divergent", "Qualitative"};

  /**
   * This method returns an 3D array contaning all the arrays legends
   * containing array triplets of RGB values in a ColorScheme
   *
   * @param schemeType   Scheme type eg. Sequential, Divergent or Qualitative
   * @param legendName   A legend name eg. Reds, BuGn, for Sequencial or
   *              BrBG for Divergent
   * @return  int[][][]  An array containing arrays with a maximum of
   *              maxColorScheme = 12, with array triplets contaning
   *              the RGB Values.
   *             eg: Sequencial.Reds
   *
   */

  public static int[][][] getRGBArray(String schemeType,
                    String legendName)
  throws ExtensionException {
    int[][][] colorScheme = new int[maxColorScheme][][];
    Class<? extends ColorSchemes> selectedClass = null;

    if (schemeType.equals("org.nlogo.render.Sequential") ||
      schemeType.equals("Sequential")) {
      selectedClass = Sequential.class;
    }
    else if (schemeType.equals("org.nlogo.render.Divergent") ||
      schemeType.equals("Divergent")) {
      selectedClass = Divergent.class;
    }
    else if (schemeType.equals("org.nlogo.render.Qualitative")||
      schemeType.equals("Qualitative")) {
      selectedClass = Qualitative.class;
    }
    else {
    throw new ExtensionException(
          "Scheme Type must be Sequential, Divergent, or Qualitative");
      // Throw execption or handle this in some way
    }

    // Get ColorScheme array with colorSchemeName
    try {
      Field field = selectedClass.getDeclaredField(legendName);
      colorScheme = (int[][][]) field.get(null);
    }
    catch (NoSuchFieldException e) {
      throw new ExtensionException(
          "Scheme Type must be Sequential, Divergent, or Qualitative");
    }
    catch (IllegalArgumentException e) {
      throw new ExtensionException(
          "The scheme " + legendName + " is not a string. ");
    }
    catch (IllegalAccessException e) {
      throw new ExtensionException(
          "the currently executing method does not have access" +
          "to the definition of the specified field");
    }

    return colorScheme;
  }

  /**
   * This method returns an 3D array contaning a legend array
   * containing array triplets of RGB values in a ColorScheme.
   *
   * @param schemeType  Scheme type eg. Sequential, Divergent or Qualitative
   * @param legendName  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @param legendSize  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @return  int[][]   An array with a maximum of 12 arrays
   *             (maxColorScheme = 12), containing array triplets with
   *             RGB Values.
   *            eg.
   *               {  {  224  ,  236  ,  244  }  ,
   *                {  158  ,  188  ,  218  }  ,
   *                {  136  ,  86  ,  167  }  }
   */


  public static int[][] getRGBArray(String schemeName,
                    String legendName,
                    int legendSize)
  throws ExtensionException {
    int[][][] colorScheme = new int[maxColorScheme][][];
    int[][] colorLegend = null;

    if (legendSize < 3) {
      throw new ExtensionException(
          "The minimum size of a color classes is 3" +
          " but your thrid argument is " + legendSize);
    }

    colorScheme = getRGBArray(schemeName, legendName);
    // Get Legend with colorSchemeSize from colorScheme
    int i = 0;
    while (i < ( colorScheme.length) &&
        colorScheme[i].length != legendSize) {
      i++;
    }
    if  (i < ( colorScheme.length)) {
      colorLegend = new int[legendSize][];
      colorLegend = colorScheme[i];
    }
    else {
      colorLegend = null;
    }
    return colorLegend;
  }

  /**
   * This method returns an 3D array contaning a legend array
   * containing Color Objects of a ColorScheme.
   *
   * @param schemeType  Scheme type eg. Sequential, Divergent or Qualitative
   * @param legendName  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @param legendSize  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @return  Color[][] An array with a maximum of 12 arrays
   *             (maxColorScheme = 12), containing array triplets with
   *             RGB Values.
   *            eg.
   *               {  new Color(  224  ,  236  ,  244  )  ,
   *                new Color(  158  ,  188  ,  218  )  ,
   *                new Color(  136  ,  86  ,  167  )  }
   */

  public static Color[] getColorArray(String schemeTypeName,
                    String colorSchemeName,
                    int colorSchemeSize)
  throws ExtensionException {
    Color[] chosenColorLegend = new Color[colorSchemeSize];
    int[][] colorArray = getRGBArray(   schemeTypeName,
                      colorSchemeName,
                      colorSchemeSize);

    if (colorSchemeSize < 3) {
      throw new ExtensionException(
          "The minimum size of a color classes is 3" +
          " but your thrid argument is " + colorSchemeSize) ;
    }

    // Create a Color[] with the rgb values of the int [][]
    for (int i = 0; i < colorArray.length; i++) {
      chosenColorLegend[i] = new Color(  colorArray [i][0],
                        colorArray[i][1],
                        colorArray [i][2]);
    }
    return chosenColorLegend;
  }

  /**
   * This method returns an 3D array contaning a legend array
   * containing ints representing ARGB of a ColorScheme.
   *
   * @param schemeType  Scheme type eg. Sequential, Divergent or Qualitative
   * @param legendName  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @param legendSize  A legend name eg. Reds, BuGn, for Sequencial or
   *             BrBG for Divergent
   * @return  int[][]   An array with a maximum of 12 arrays
   *             (maxColorScheme = 12), containing array triplets with
   *             RGB Values.
   *            eg.
   *               {  int,
   *                int,
   *                int    }
   */

  // TODO: Should I handle alpha transparency ?

  public static int[] getIntArray (String schemeTypeName,
                   String colorSchemeName,
                   int colorSchemeSize)
  throws ExtensionException {
    if (colorSchemeSize < 3) {
      throw new ExtensionException(
          "The minimum size of a color classes is 3" +
          " but your thrid argument is " + colorSchemeSize) ;
    }

    int colorInt[] = new int[colorSchemeSize];
    int[][] colorArray = getRGBArray(   schemeTypeName,
                      colorSchemeName,
                      colorSchemeSize);
    for (int i = 0; i < colorArray.length; i++) {
      colorInt[i] =   (colorArray[i][0] << 16) |
              (colorArray[i][1] << 8 ) |
              (colorArray[i][2]      ) ;
    }
    return colorInt;
  }

  //TODO: Test this function, just written but not tested
  public static String [] getschemeTypes() {
    return schemeTypes;
  }

  //TODO: Return an array with all the legend names
  //      for a given scheme type
  //static public String [] getlegendNames( String schemeType)
  //{
  //  return legendNames ;
  //}

  public static int[][][][] getRGBArray(String schemeType)
    throws ExtensionException {
    int[][][][] colorScheme = null;
    Class<? extends ColorSchemes> selectedClass = null;

    //  Select the Scheme with schemeTypeName
    if (schemeType.equals("Sequential")) {
      selectedClass = Sequential.class;
    }
    else if (schemeType.equals("Divergent")) {
      selectedClass = Divergent.class;
    }
    else if (schemeType.equals("Qualitative")) {
      selectedClass = Qualitative.class;
    }
    else {
      throw new ExtensionException(
            "Scheme Type must be Sequential, Divergent, or Qualitative");
        // Throw execption or handle this in some way
    }

    // Get ColorScheme array with colorSchemeName
    try {
      Field fields[] = selectedClass.getDeclaredFields();
      colorScheme = new int[fields.length][maxColorScheme][][];
      for (int i = 0; i < fields.length; i++) {
        colorScheme[i] = (int[][][]) fields[i].get(null);
      }
    }
    catch (IllegalAccessException e) {
      throw new ExtensionException(
          "the currently executing method does not have access" +
          "to the definition of the specified field");
    }
    return colorScheme;
  }

  public static int getMaximumLegendSize(String schemeType) throws ExtensionException {
    int [][][][] colorschemes = getRGBArray(schemeType);
    int max = 0;
    for (int i = 0; i < colorschemes.length; i++) {
      max = Math.max(colorschemes[i].length,
                 max);
    }
    // We add 2 since the color schemes start at 3 colors
    return(max + 2);
  }
}
