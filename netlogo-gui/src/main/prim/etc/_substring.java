// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final strictfp class _substring
    extends Reporter
    implements org.nlogo.core.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    String string = argEvalString(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    if (start < 0) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.startIsLessThanZero", start));
    } else if (start > stop) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsLessThanStart", stop, start));
    } else if (stop > string.length()) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._substring.endIsGreaterThanListSize",
              stop, Dump.logoObject(string), string.length()));
    }
    return string.substring(start, stop);
  }


}
