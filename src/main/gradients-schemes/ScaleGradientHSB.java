import java.util.ArrayList;
import java.util.Iterator;

import org.nlogo.api.Argument;
import org.nlogo.api.Color;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

public class ScaleGradientHSB implements Reporter {

  // Primitive arguments
  private LogoList colorLogoList = null ;
  private double var = 0 ;
  private double min = 0 ;
  private double max = 0 ;

    // Preceptually reasonable gradation resolution of 256 different colors
  // for each pair of colors
  final static int SIZE = 256 ;

  // Static variables for cache handling
  private static LogoList colorLogoListCache = null ;
  private static double[][] gradientArray =  null;

  public Syntax getSyntax() {
    int[] right = {
      Syntax.ListType() ,   // n list with lists of 3 numbers [[h s b] [h s b] ...]
      Syntax.NumberType() , // number with the value to scale
      Syntax.NumberType() , // number with range1
      Syntax.NumberType()   // number with range2
    };
    int ret = Syntax.ListType() ; // list with 3 numbers [h s b]
    return SyntaxJ.reporterSyntax( right , ret ) ;
  }

  public Object report( Argument args[] , Context context )
      throws ExtensionException {
    // Extract arguments
    try {
      colorLogoList = args[ 0 ].getList() ;
      var = args[ 1 ].getDoubleValue() ;
      min = args[ 2 ].getDoubleValue() ;
      max = args[ 3 ].getDoubleValue() ;
    }
    // Hope that they have the correct type
    catch ( LogoException e ) {
      throw new ExtensionException( e.getMessage() ) ;
    }

    // Validate colorList hsb arguments
    for (int i = 0; i < colorLogoList.length(); i++) {
      LogoList HSBList;
      if (colorLogoList.get(i) instanceof Double) {
        throw new ExtensionException("Detected double as input, must input HSB lists for scale-gradient-hsb");
      }
      else {
        HSBList = (LogoList) colorLogoList.get(i);
      }
      validHSBList(HSBList);
    }

    // Normalize var, min, max
    double perc = 0.0 ;
    if ( min > max ) {  // min and max are really reversed
      if( var < max ) {
        perc = 1.0 ;
      }
      else if( var > min ) {
        perc = 0.0 ;
      }
      else {
        double tempval = min - var ;
        double tempmax = min - max ;
        perc = tempval / tempmax ;
      }
    }
    else {
      if( var > max ) {
        perc = 1.0 ;
      }
      else if( var < min ) {
        perc = 0.0 ;
      }
      else {
        double tempval = var - min ;
        double tempmax = max - min ;
        perc = tempval / tempmax ;
      }
    }

    int index ;
    if (colorLogoList.size() < 3 ) {
      index =  (int) Math.round(perc * (SIZE - 1)); // 0 and 255
    }
    else {
      index = (int) Math.round(perc * ( (SIZE - 1) + (SIZE)*(colorLogoList.size() - 2) )) ; // 255 + n * 256
    }

    // The order of the evaluation matters in statement below !
    if (colorLogoListCache == null ||
        !colorLogoListCache.equals( colorLogoList)) {

      // Store current list as cache
      colorLogoListCache = colorLogoList;

      // Create an array containing color instances of the arguments
      Iterator<Object> it = colorLogoList.javaIterator();
      LogoListBuilder colorList = new LogoListBuilder();
      it = colorLogoList.javaIterator ();
      for (int i = 0; i < colorLogoList.length(); i++) {
        colorList.add((LogoList) colorLogoList.get(i));
      }
      LogoList inputHSB = colorList.toLogoList();

      // Create array with resulting gradient color instances
      gradientArray = new double [SIZE * (inputHSB.length() - 1) ] [3];
      for (int i = 0; i < (inputHSB.length() - 1) ; i++) {
        ColorGradientHSB colorGradient = new ColorGradientHSB(); // make the generate HSB gradient here, ignore case 1, add that manually
        LogoList ll1 = (LogoList) inputHSB.get(i); LogoList ll2 = (LogoList) inputHSB.get(i+1);
        double[] color1 = new double[3]; color1[0] = (double) ll1.get(0);
        color1[1] = (double) ll1.get(1); color1[2] = (double) ll1.get(2);
        double[] color2 = new double[3]; color2[0] = (double) ll2.get(0);
        color2[1] = (double) ll2.get(1); color2[2] = (double) ll2.get(2);

        colorGradient.genArray(color1, color2, SIZE);
        for (int j = 0; j < SIZE ; j++) {
          gradientArray[j+ (SIZE * i)] = colorGradient.getHSBArray()[j];
          LogoListBuilder toRGB = new LogoListBuilder();
          for(int k = 0; k < 3; k++) { toRGB.add(gradientArray[j+ (SIZE * i)][k]); }
          LogoList rgb = HSBUpdated.HSBtoRGB(toRGB.toLogoList());
          for(int k = 0; k < 3; k++) { gradientArray[j+ (SIZE * i)][k] = (double) rgb.get(k); }
        }
      }
    }

    // Extract rgb values of resulting gradient color to a LogoList
    LogoListBuilder gradientList = new LogoListBuilder() ;
    try {
      gradientList.add( Double.valueOf( gradientArray[ index ][ 0 ] ) ) ;
      gradientList.add( Double.valueOf( gradientArray[ index ][ 1 ] ) ) ;
      gradientList.add( Double.valueOf( gradientArray[ index ][ 2 ] ) ) ;
    }
    catch ( ArrayIndexOutOfBoundsException e ) {
      throw new ExtensionException(
          "Please e-mail send this error to bugs@ccl.northwestern.edu" +
          e.getMessage() ) ;
    }
    return gradientList.toLogoList() ;
  }

  void validHSBList( LogoList hsb ) throws ExtensionException {
    if(hsb.size() !=  3) {
      throw new ExtensionException("HSB must have three elements");
    }
    else {
      if((double) hsb.get(0) < 0 || (double) hsb.get(0) > 360) {
        throw new ExtensionException("Hue must be in the range from 0 to 360");
      }
      if((double) hsb.get(1) < 0 || (double) hsb.get(1) > 100) {
        throw new ExtensionException("Saturation must be in the range from 0 to 100");
      }
      if((double) hsb.get(2) < 0 || (double) hsb.get(2) > 100) {
        throw new ExtensionException("Brightness must be in the range from 0 to 100");
      }
    }
  }
}
