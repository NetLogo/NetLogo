// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final strictfp class _last
    extends Reporter
    implements org.nlogo.core.Pure {


  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[0].report(context);
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (list.size() == 0) {
        throw new RuntimePrimitiveException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyList"));
      }
      return list.get(list.size() - 1);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (string.length() == 0) {
        throw new RuntimePrimitiveException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyString"));
      }
      return string.substring(string.length() - 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }
}
