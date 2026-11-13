// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.Pure;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _substring
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    String string = argEvalString(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    if (start < 0) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is less than zero.", Integer.toString(start)));
    } else if (start > stop) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is less than {}.", Integer.toString(stop), Integer.toString(start)));
    } else if (stop > string.length()) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is too big for {}, which is only of length {}.", Integer.toString(stop),
                           Dump.logoObject(string), Integer.toString(string.length())));
    }
    return string.substring(start, stop);
  }

}
