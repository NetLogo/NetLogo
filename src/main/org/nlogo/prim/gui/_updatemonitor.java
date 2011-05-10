package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _updatemonitor
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_WILDCARD},
            "O---", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    ((org.nlogo.window.MonitorWidget) context.job.owner)
        .value(args[0].report(context));
    context.ip = next;
  }
}
