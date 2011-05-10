package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _linkbreedvariable
    extends Reporter {
  public String name;

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_WILDCARD | Syntax.TYPE_REFERENCE,
            "---L");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + name;
  }

  public _linkbreedvariable(String name) {
    this.name = name;
  }

  @Override
  public Object report(final Context context) throws LogoException {
    try {
      return context.agent.getLinkBreedVariable(name);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  public Object report_1(final Context context) throws LogoException {
    try {
      return context.agent.getLinkBreedVariable(name);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
