// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.WrappedExtensionException;

public final strictfp class _extern
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final org.nlogo.api.Command command;

  public _extern(org.nlogo.api.Command command) {
    this.command = command;
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    org.nlogo.nvm.Argument arguments[] = new org.nlogo.nvm.Argument[args.length];
    for (int i = 0; i < args.length; i++) {
      arguments[i] = new org.nlogo.nvm.Argument(context, args[i]);
    }

    try {
      command.perform
          (arguments,
              new ExtensionContext(workspace, workspace.modelTracker(), context));
    } catch (org.nlogo.api.ExtensionException ex) {
      LogoException le =
          new WrappedExtensionException(context, this, "Extension exception: " + ex.getMessage(), ex);
      throw le;
    }
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    if (command instanceof org.nlogo.nvm.CustomAssembled) {
      ((org.nlogo.nvm.CustomAssembled) command)
          .assemble(a);
    }
    a.resume();
  }
}
