package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _dump
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.StringType(), "O---");
  }

  @Override
  public Object report(final Context context) {
    StringBuilder buf = new StringBuilder();
    buf.append(world.program().dump() + "\n");
    for (Procedure p : workspace.getProcedures().values()) {
      buf.append(p.dump() + "\n");
    }
    return buf.toString();
  }
}
