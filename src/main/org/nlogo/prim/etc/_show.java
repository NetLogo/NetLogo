package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Workspace;

public final strictfp class _show
    extends Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    workspace.outputObject
        (args[0].report(context),
            context.agent,
            true, true,
            Workspace.OutputDestination.NORMAL);
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType()};
    return Syntax.commandSyntax(right);
  }

  ///////////////////////////////////////////////
  // for use with bytecode generator

  public void perform_1(final org.nlogo.nvm.Context context, Object arg0) throws LogoException {
    workspace.outputObject(arg0, context.agent, true, true,
        Workspace.OutputDestination.NORMAL);
    context.ip = next;
  }

}
