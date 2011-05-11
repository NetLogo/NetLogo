package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _zoom
    extends Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER};
    return Syntax.commandSyntax(right, "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Observer observer = world.observer();
    double delta = argEvalDoubleValue(context, 0);

    // don't zoom past the point you are looking at.
    // maybe this should be an error?
    if (delta > observer.dist()) {
      delta = observer.dist();
    }

    observer.oxyandzcor(observer.oxcor() + (delta * observer.dx()),
        observer.oycor() + (delta * observer.dy()),
        observer.ozcor() - (delta * observer.dz()));
    context.ip = next;
  }
}
