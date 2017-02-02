// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final strictfp class _item
    extends Reporter
    implements org.nlogo.core.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    int index = argEvalIntValue(context, 0);
    Object obj = args[1].report(context);
    if (index < 0) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.negativeIndex", index));
    }
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (index >= list.size()) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.indexExceedsListSize",
                index, Dump.logoObject(list), list.size()));
      }
      return list.get(index);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (index >= string.length()) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.indexExceedsListSize",
                index, Dump.logoObject(string), string.length()));
      }
      return string.substring(index, index + 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }


}
