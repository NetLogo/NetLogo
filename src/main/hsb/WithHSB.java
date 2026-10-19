import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

// [color] palette:with-component [number]
// changes the component of the color to number while leaving the other components in the same colorspace unchanged
public class WithHSB implements Reporter {

  private int index;

  public WithHSB(int ind) {
    index = ind;
  }

  public Syntax getSyntax() {
    int left = Syntax.NumberType() | Syntax.ListType();
    int values[] = {Syntax.NumberType()};
    int ret = Syntax.ListType();
    return SyntaxJ.reporterSyntax(left, values, ret, Syntax.NormalPrecedence());
  }

  public Object report(Argument args[], Context context) throws ExtensionException {
    LogoList rgb = ColorManager.extractColorFromArg(args[0]);
    double newVal = args[1].getDoubleValue();
    return HSBUpdated.updateHSB(rgb, newVal, index);
  }
}
