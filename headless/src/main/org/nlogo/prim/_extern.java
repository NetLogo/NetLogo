// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.ExtensionContext;

public final strictfp class _extern
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final org.nlogo.api.Command command;

  public _extern(org.nlogo.api.Command command) {
    this.command = command;
  }

  @Override
  public Syntax syntax() {
    Syntax s = command.getSyntax();
    String[] acs = command.getAgentClassString().split(":");
    if (acs[0].length() < 4) {
      acs[0] = Syntax.convertOldStyleAgentClassString(acs[0]);
    }
    if (acs.length >= 2) {
      if (acs[1].length() < 4) {
        acs[1] = Syntax.convertOldStyleAgentClassString(acs[1]);
      }
      return Syntax.commandSyntax
          (s.right(), s.dfault(), acs[0], acs[1], command.getSwitchesBoolean());
    } else {
      return Syntax.commandSyntax
          (s.right(), s.dfault(), acs[0], null, command.getSwitchesBoolean());
    }
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
              new ExtensionContext(workspace, context));
    } catch (org.nlogo.api.ExtensionException ex) {
      LogoException le =
          new EngineException
              (context, this, "Extension exception: " + ex.getMessage());
      // it might be better to use the Java 1.4 setCause() stuff, for
      // the long term... but then i think the handler would have to
      // be changed, too.
      le.setStackTrace(ex.getStackTrace());
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
