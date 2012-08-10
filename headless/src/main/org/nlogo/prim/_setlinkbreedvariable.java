// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _setlinkbreedvariable
    extends Command {
  String name;

  public _setlinkbreedvariable(_linkbreedvariable original) {
    name = original.name;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType()};
    return Syntax.commandSyntax(right, "---L", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + name;
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    Object value = args[0].report(context);
    try {
      context.agent.setLinkBreedVariable(name, value);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
