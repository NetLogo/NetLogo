// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _linkvariable
    extends Reporter {
  public int vn = 0;

  public _linkvariable(int vn) {
    this.vn = vn;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.WildcardType() | Syntax.ReferenceType(),
            "---L");
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.linksOwnNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      return context.agent.getLinkVariable(vn);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  public Object report_1(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      return context.agent.getLinkVariable(vn);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
