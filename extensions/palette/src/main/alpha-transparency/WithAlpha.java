import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

// [color] palette:with-component [number]
// changes the component of the color to number while leaving the other components in the same colorspace unchanged
public class WithAlpha implements Reporter {
  public Syntax getSyntax() {
    int left = Syntax.NumberType() | Syntax.ListType();
    int values[] = {Syntax.NumberType()};
    int ret = Syntax.ListType();
    return SyntaxJ.reporterSyntax(left, values, ret, Syntax.NormalPrecedence());
  }
  public Object report(Argument args[], Context context) throws ExtensionException {
    LogoList rgbcolor = ColorManager.extractColorFromArg(args[0]);
    double alpha = args[1].getDoubleValue();
    if (alpha < 0 || alpha > 255) {
      throw new ExtensionException("Alpha must be in the range from 0 to 255.");
    }
    LogoListBuilder ans = new LogoListBuilder();
    for (int i = 0; i < 3; i++) {
      ans.add(rgbcolor.get(i));
    }
    if (alpha != 255) {
      ans.add(alpha);
    }
    return ans.toLogoList();
  }
}
