// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final class _settopology
    extends Command {


  @Override
  public void perform(final Context context)
      throws LogoException {
    workspace.changeTopology
        (argEvalBooleanValue(context, 0),
            argEvalBooleanValue(context, 1));
    context.ip = next;
  }
}
