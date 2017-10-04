// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.WrappedExtensionException;

public final strictfp class _externreport
    extends Reporter {
  private final org.nlogo.api.Reporter reporter;

  public _externreport(org.nlogo.api.Reporter reporter) {
    this.reporter = reporter;
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
                  new ExtensionContext(workspace, workspace.modelTracker(), context));
    } catch (org.nlogo.api.ExtensionException ex) {
      throw new WrappedExtensionException(
          context, this, "Extension exception: " + ex.getMessage(), ex);
    }
    return result;
  }
}
