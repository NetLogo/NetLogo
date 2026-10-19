import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Color;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

public class ExtractHSB implements Reporter {

  private int index;

  public ExtractHSB (int ind){
    index = ind;
  }

  public Syntax getSyntax() {
    int[] values = {
      Syntax.NumberType() | Syntax.ListType()
    };
    int ret = Syntax.NumberType();
    return SyntaxJ.reporterSyntax(values, ret, 1);
  }

  public Object report(Argument args[], Context context) throws ExtensionException {
    LogoList rgb = ColorManager.extractColorFromArg(args[0]);
    return extract(rgb, index);
  }

  public static double extract(LogoList rgb, int ind) {
    if (rgb.length() > 3) {rgb.butLast();}
    return (double) Color.getHSBListByRGBList(rgb).get(ind);
  }
}
