// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _breedvariable
    extends Reporter {
  public final String name;

  public _breedvariable(String name) {
    this.name = name;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.WildcardType() | Syntax.ReferenceType(),
            "-T--");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + name;
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public Object report_1(Context context) throws LogoException {
    try {
      return context.agent.getBreedVariable(name);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
