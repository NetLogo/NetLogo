package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _end2
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TurtleType(), "---L");
  }

  @Override
  public Object report(final Context context) {
    return report_1(context);
  }

  public Turtle report_1(final Context context) {
    return ((Link) context.agent).end2();
  }
}
