// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _sublist
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    LogoList list = argEvalList(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    int size = list.size();
    if (start < 0) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is less than zero.", Integer.toString(start)));
    } else if (stop < start) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is less than {}.", Integer.toString(stop), Integer.toString(start)));
    } else if (stop > size) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} is greater than the length of the input list ({}).", Integer.toString(stop),
                           Integer.toString(size)));
    }
    return list.logoSublist(start, stop);
  }

}
