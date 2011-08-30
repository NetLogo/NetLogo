package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _experimentstepend
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", false);
  }

  @Override
  public void perform(final Context context) {
    if (context.stopping) {
      // Worker will check this flag to see if the
      // run should be stopped - ST 3/8/06
      context.job.stopping = true;
    }
    context.finished = true;
  }
}
