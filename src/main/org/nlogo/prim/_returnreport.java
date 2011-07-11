package org.nlogo.prim;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Context;

public final strictfp class _returnreport extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public String displayName() {
    return "END"; // for use in error messages
  }

  @Override
  public void perform(Context context) throws LogoException {
    perform_1(context);
  }

  public void perform_1(Context context) throws LogoException {
    throw new EngineException(context, this,
            I18N.errorsJ().get("org.nlogo.prim._returnreport.reportNotCalledInReportProcedure"));
  }
}
