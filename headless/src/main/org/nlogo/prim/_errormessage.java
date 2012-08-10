// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Let;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

/**
 * Gets the error message from the LetMap.
 * Used in conjunction with <code>carefully</code>.
 *
 * @see _carefully
 */
public final strictfp class _errormessage
    extends Reporter {
  public Let let = null;  // compiler will fill this in

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.StringType());
  }

  @Override
  public Object report(final Context context) {
    return report_1(context);
  }

  public String report_1(final Context context) {
    return ((LogoException) context.getLet(let)).getMessage();
  }
}
