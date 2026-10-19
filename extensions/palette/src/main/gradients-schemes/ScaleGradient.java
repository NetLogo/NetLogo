import java.util.ArrayList;
import java.util.Iterator;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.Color;


public class ScaleGradient implements Reporter {

  // Primitive arguments
  private LogoList colorLogoList = null;
  private double var = 0;
  private double min = 0;
  private double max = 0;

  // Preceptually reasonable gradation resolution of 256 different colors
  // for each pair of colors
  final static int SIZE = 256;

  // Static variables for cache handling
  private static LogoList colorLogoListCache = null;
  private static int[][] gradientArray =  null;

  public Syntax getSyntax() {
    int[] right = {
      Syntax.ListType(),   // n list with lists of 3 numbers [[r g b] [r g b] ...]
      Syntax.NumberType(), // number with the value to scale
      Syntax.NumberType(), // number with range1
      Syntax.NumberType()   // number with range2
    };
    int ret = Syntax.ListType(); // list with 3 numbers [r g b]
    return SyntaxJ.reporterSyntax(right , ret);
  }

  public Object report(Argument args[], Context context)
      throws ExtensionException {
    // Extract arguments
    try {
      colorLogoList = args[0].getList();
      var = args[1].getDoubleValue();
      min = args[2].getDoubleValue();
      max = args[3].getDoubleValue();
    }
    // Hope that they have the correct type
    catch (LogoException e) {
      throw new ExtensionException(e.getMessage());
    }

    // Validate colorList rgb arguments
    for (int i = 0; i < colorLogoList.length(); i++) {
      LogoList RGBList;
      if (colorLogoList.get(i) instanceof Double){
        RGBList = Color.getRGBListByARGB(Color.getARGBbyPremodulatedColorNumber(Color.modulateDouble((double) colorLogoList.get(i))));
      }
      else {
        RGBList = (LogoList) colorLogoList.get(i);
      }
      if (RGBList.length() == 4) {
        RGBList = RGBList.butLast();
      }
      validRGBList(RGBList);
    }

    // Normalize var, min, max
    double perc = 0.0;
    if (min > max) { // min and max are really reversed
      if (var < max) {
        perc = 1.0;
      }
      else if (var > min) {
        perc = 0.0;
      }
      else {
        double tempval = min - var;
        double tempmax = min - max;
        perc = tempval / tempmax;
      }
    }
    else {
      if (var > max) {
        perc = 1.0;
      }
      else if (var < min) {
        perc = 0.0;
      }
      else {
        double tempval = var - min;
        double tempmax = max - min;
        perc = tempval / tempmax;
      }
    }

    int index;
    if (colorLogoList.size() < 3 ) {
      index =  (int) Math.round(perc * (SIZE - 1)); // 0 and 255
    }
    else {
      index = (int) Math.round(perc * ( (SIZE - 1) + (SIZE)*(colorLogoList.size() - 2) )); // 255 + n * 256
    }

    // The order of the evaluation matters in statement below !
    if (colorLogoListCache == null ||
        !colorLogoListCache.equals( colorLogoList)) {

      // Store current list as cache
      colorLogoListCache = colorLogoList;

      // Create an array containing color instances of the arguments
      ArrayList<Object> colorList = new ArrayList<Object>();
      for (int i = 0; i < colorLogoList.length(); i++) {
        LogoList RGBList;
        if (colorLogoList.get(i) instanceof Double) {
          RGBList = Color.getRGBListByARGB(Color.getARGBbyPremodulatedColorNumber(Color.modulateDouble((double) colorLogoList.get(i))));
        }
        else {
          RGBList = (LogoList) colorLogoList.get(i);
        }
        if (RGBList.length() == 4) {
          RGBList = RGBList.butLast();
        }
        java.awt.Color color = new java.awt.Color (
            ((Double) RGBList.get(0)).intValue(),
            ((Double) RGBList.get(1)).intValue(),
            ((Double) RGBList.get(2)).intValue()
        );
        colorList.add(color);
      }

      // Create array with resulting gradient color instances


      gradientArray = new int [SIZE * (colorList.size() - 1)][3];
      for (int i = 0; i < (colorList.size() - 1); i++) {
        ColorGradient colorGradient = new ColorGradient(
            (java.awt.Color) colorList.get(i),
            (java.awt.Color) colorList.get(i + 1) ,
            SIZE);
        for (int j = 0; j < SIZE; j++) {
          gradientArray[j+ (SIZE * i)] = colorGradient.getGradientRGBArray()[j];
        }
      }
    }

    // Extract rgb values of resulting gradient color to a LogoList
    LogoListBuilder gradientList = new LogoListBuilder();
    try {
      gradientList.add(Double.valueOf(gradientArray[index][0]));
      gradientList.add(Double.valueOf(gradientArray[index][1]));
      gradientList.add(Double.valueOf(gradientArray[index][2]));
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new ExtensionException(
          "Please e-mail send this error to bugs@ccl.northwestern.edu" +
          e.getMessage());
    }

    return gradientList.toLogoList();
  }

  private void validRGB(int c)
  throws ExtensionException {
    if (c < 0 || c > 255) {
      throw new ExtensionException( "RGB values must be 0-255" );
    }
  }

  void validRGBList(LogoList rgb)
  throws ExtensionException {
    if (rgb.size() == 3) {
      try {
        validRGB(((Double)rgb.get(0)).intValue());
        validRGB(((Double)rgb.get(1)).intValue());
        validRGB(((Double)rgb.get(2)).intValue());
        return;
      }
      catch (ClassCastException e) {
        // just fall through and throw the error below
        org.nlogo.api.Exceptions.ignore(e);
      }
    }
    else {
      throw new ExtensionException( "An rgb list must contain 3 numbers 0-255" +
                      "one of your rgb lists contains " +
                      rgb.size() + "numbers");
    }
  }
}
