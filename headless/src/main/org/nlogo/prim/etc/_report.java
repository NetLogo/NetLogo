// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.NonLocalExit$;
import org.nlogo.nvm.Procedure;

public final strictfp class _report
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()});
  }

  @Override
  public void perform(Context context) {
    perform_1(context, args[0].report(context));
  }

  public void perform_1(Context context, Object arg0) {
    context.job.result = arg0;
    context.stopping = false;
    context.ip = next;
    if (context.activation.procedure().isTask()) {
      throw NonLocalExit$.MODULE$;
    } else if (!context.activation.procedure().isReporter()) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim._report.canOnlyUseInToReport", displayName()));
    } else if (!context.atTopActivation()) {
      // you can't report from inside an ask.  you can't write code like
      //   to-report foo ask turtle 0 [ report 5 ] end
      // maybe you should be able to, but at least for now, you can't
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim._report.mustImmediatelyBeUsedInToReport", displayName()));
    }
  }
}
