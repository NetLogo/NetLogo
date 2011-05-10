package org.nlogo.prim;

import org.nlogo.agent.Patch;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reference;
import org.nlogo.nvm.Referenceable;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Context;

public final strictfp class _patchvariable
    extends Reporter
    implements Referenceable {
  public int vn = 0;

  public _patchvariable(int vn) {
    this.vn = vn;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_WILDCARD | Syntax.TYPE_REFERENCE,
            "-TP-");
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  public Reference makeReference() {
    return new Reference(Patch.class, vn, this);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public Object report_1(Context context) throws LogoException {
    try {
      return context.agent.getPatchVariable(vn);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
