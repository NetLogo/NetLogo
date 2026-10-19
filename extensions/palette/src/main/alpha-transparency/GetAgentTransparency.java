import org.nlogo.api.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Reporter;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

public class GetAgentTransparency implements Reporter {
  public Syntax getSyntax() {
    int[] values = {};
    int ret = Syntax.NumberType();
    return SyntaxJ.reporterSyntax(values, ret, "-TPL");
  }

  public Object report(Argument args[], Context context) {
    Agent caller = context.getAgent();
    double alpha = caller.alpha();
    return (1 - alpha/255) * 100;
  }
}
