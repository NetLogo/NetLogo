import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Color;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

public class GetAgentRGB implements Reporter {

  private int index;

  public GetAgentRGB(int ind) {
    index = ind;
  }

  public Syntax getSyntax() {
    int[] values = {};
    int ret = Syntax.NumberType();
    return SyntaxJ.reporterSyntax(values, ret, "-TPL");
  }

  public Object report(Argument args[], Context context) throws ExtensionException {
    LogoList rgb = ColorManager.getAgentColor(context);
    return rgb.get(index);
  }
}
