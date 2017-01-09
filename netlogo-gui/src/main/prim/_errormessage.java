// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
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

  public _errormessage() { }

  public _errormessage(Let let) {
    this.let = let;
  }



  @Override
  public Object report(final Context context) {
    return report_1(context);
  }

  public String report_1(final Context context) {
    return ((LogoException) context.activation.binding.getLet(let)).getMessage();
  }
}
