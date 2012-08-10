// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _turtleorlinkvariable
    extends Reporter {
  public String varName = "";

  public _turtleorlinkvariable(String varName) {
    this.varName = varName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.WildcardType() | Syntax.ReferenceType(),
            "-T-L");
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + varName;
    } else {
      return super.toString() + ":" + varName;
    }
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public Object report_1(Context context) throws LogoException {
    try {
      return context.agent.getTurtleOrLinkVariable(varName);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
