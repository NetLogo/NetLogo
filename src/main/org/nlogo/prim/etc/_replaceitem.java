// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _replaceitem
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    int index = argEvalIntValue(context, 0);
    Object obj = args[1].report(context);
    Object elt = args[2].report(context);
    if (index < 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.negativeIndex", index));
    }
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (index >= list.size()) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.indexExceedsListSize",
                index, Dump.logoObject(list), list.size()));
      }
      return list.replaceItem(index, elt);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (!(elt instanceof String)) {
        throw new ArgumentTypeException
            (context, this, 2, Syntax.StringType(), elt);
      } else if (index >= string.length()) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.indexExceedsStringSize",
                index, Dump.logoObject(string), string.length()));
      }
      return string.substring(0, index) + elt + string.substring(index + 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(),
        Syntax.ListType() | Syntax.StringType(),
        Syntax.WildcardType()};
    int ret = Syntax.ListType() | Syntax.StringType();
    return Syntax.reporterSyntax(right, ret);
  }
}
