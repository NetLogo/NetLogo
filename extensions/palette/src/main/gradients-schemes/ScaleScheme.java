import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

// TODO: Check cache handling

public class ScaleScheme implements Reporter {

  public Syntax getSyntax() {
    int[] right = { Syntax.StringType(),
      Syntax.StringType(),
      Syntax.NumberType(),
      Syntax.NumberType(),
      Syntax.NumberType(),
      Syntax.NumberType()};
      int ret = Syntax.ListType();
      return SyntaxJ.reporterSyntax(right, ret);
    }

    public Object report(Argument args[] , Context context)
    throws ExtensionException {
      String schemename;
      String legendname;
      int size;
      double var;
      double min;
      double max;
      try {
        schemename = args[ 0 ].getString();
        legendname = args[ 1 ].getString();
        size = args[ 2 ].getIntValue();
        var  = args[ 3 ].getDoubleValue();
        min  = args[ 4 ].getDoubleValue();
        max  = args[ 5 ].getDoubleValue();
      }
      catch(LogoException e) {
        throw new ExtensionException(e.getMessage());
      }

      int index = 0;
      int [][] legend;

      double perc = 0.0 ;

      // Case value maps to first endpoint
      if ((max >= min) && (var <= min) || (min > max) && (var > min)) {
        index = 0;
      } else if ((min > max) && ( var < max) || (max >= min) && ( var > max)) {
        // Case value maps to last endpoint
        index = size - 1;
      } else {
        // Main case: the value lies between the maximum and minimum
        double tempval = (max > min) ? var - min : min - var;
        double tempmax = Math.abs(max - min);
        // tempmax is how much of the scale should be alotted for each color
        tempmax /= (double) size;
        // perc is the index of the slice
        perc = Math.floor(tempval / tempmax);
        index = Math.min((int) perc, size - 1);
      }

      legend = ColorSchemes.getRGBArray(schemename, legendname, size);

      LogoListBuilder list = new LogoListBuilder();
      try {
        list.add(Double.valueOf(legend[index][0]));
        list.add(Double.valueOf(legend[index][1]));
        list.add(Double.valueOf(legend[index][2]));
      }
      catch (ArrayIndexOutOfBoundsException e) {
        throw new ExtensionException(
        legendname + " has a maximum of " +
        String.valueOf(ColorSchemes.getRGBArray(schemename, legendname).length) +
        " colors, but you have requested " + size);
      }
      catch (NullPointerException e) {
        throw new ExtensionException(
        legendname + " has a maximum of " +
        String.valueOf(ColorSchemes.getRGBArray(schemename, legendname).length) +
        " colors, but you have requested " + size);
      }

      return list.toLogoList();
    }
  }
