// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _sublist
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    LogoList list = argEvalList(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    int size = list.size();
    if (start < 0) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.startIsLessThanZero", start));
    } else if (stop < start) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsLessThanStart", stop, start));

    } else if (stop > size) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsGreaterThanListSize", stop, size));
    }
    return list.logoSublist(start, stop);
  }

}
