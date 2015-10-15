// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.Pure;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _substring
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    String string = argEvalString(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    if (start < 0) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.startIsLessThanZero", start));
    } else if (start > stop) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsLessThanStart", stop, start));
    } else if (stop > string.length()) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._substring.endIsGreaterThanListSize",
              stop, Dump.logoObject(string), string.length()));
    }
    return string.substring(start, stop);
  }

}
