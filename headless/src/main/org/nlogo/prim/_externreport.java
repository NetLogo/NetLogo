// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.Reporter;

public final strictfp class _externreport
    extends Reporter {
  private final org.nlogo.api.Reporter reporter;

  public _externreport(org.nlogo.api.Reporter reporter) {
    this.reporter = reporter;
  }

  @Override
  public Syntax syntax() {
    org.nlogo.api.Syntax s = reporter.getSyntax();
    String[] acs = reporter.getAgentClassString().split(":");
    if (acs[0].length() < 4) {
      acs[0] = org.nlogo.api.Syntax.convertOldStyleAgentClassString(acs[0]);
    }
    if (acs.length >= 2) {
      if (acs[1].length() < 4) {
        acs[1] = org.nlogo.api.Syntax.convertOldStyleAgentClassString(acs[1]);
      }
      return Syntax.reporterSyntax
          (s.left(), s.right(), s.ret(), s.precedence(), s.dfault(),
              false, acs[0], acs[1]);
    } else {
      return Syntax.reporterSyntax
          (s.left(), s.right(), s.ret(), s.precedence(), s.dfault(),
              false, acs[0], null);
    }
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    org.nlogo.nvm.Argument arguments[] = new org.nlogo.nvm.Argument[args.length];
    for (int i = 0; i < args.length; i++) {
      arguments[i] = new org.nlogo.nvm.Argument(context, args[i]);
    }
    Object result = null;
    try {
      result =
          reporter.report
              (arguments,
                  new ExtensionContext(workspace, context));
    } catch (org.nlogo.api.ExtensionException ex) {
      EngineException ee =
          new EngineException(context, this, "Extension exception: "
              + ex.getMessage());
      // it might be better to use the Java 1.4 setCause() stuff, for
      // the long term... but then i think the handler would have to
      // be changed, too.
      ee.setStackTrace(ex.getStackTrace());
      throw ee;
    }
    return result;
  }
}
