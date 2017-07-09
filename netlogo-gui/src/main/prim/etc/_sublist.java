// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final strictfp class _sublist
    extends Reporter
    implements org.nlogo.core.Pure {

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    LogoList list = argEvalList(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    int size = list.size();
    if (start < 0) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.startIsLessThanZero", start));
    } else if (stop < start) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsLessThanStart", stop, start));

    } else if (stop > size) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._sublist.endIsGreaterThanListSize", stop, size));
    }
    return list.logoSublist(start, stop);
  }


}
