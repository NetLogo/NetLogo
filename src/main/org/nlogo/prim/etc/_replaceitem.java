package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.I18NJava;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _replaceitem
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    int index = argEvalIntValue(context, 0);
    Object obj = args[1].report(context);
    Object elt = args[2].report(context);
    if (index < 0) {
      throw new EngineException(context, this,
          I18NJava.errors().getN("org.nlogo.prim.etc.$common.negativeIndex", index));
    }
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (index >= list.size()) {
        throw new EngineException(context, this,
            I18NJava.errors().getN("org.nlogo.prim.etc.$common.indexExceedsListSize",
                index, Dump.logoObject(list), list.size()));
      }
      return list.replaceItem(index, elt);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (!(elt instanceof String)) {
        throw new ArgumentTypeException
            (context, this, 2, Syntax.TYPE_STRING, elt);
      } else if (index >= string.length()) {
        throw new EngineException(context, this,
            I18NJava.errors().getN("org.nlogo.prim.etc.$common.indexExceedsStringSize",
                index, Dump.logoObject(string), string.length()));
      }
      return string.substring(0, index) + (String) elt + string.substring(index + 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.TYPE_LIST | Syntax.TYPE_STRING, obj);
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_LIST | Syntax.TYPE_STRING,
        Syntax.TYPE_WILDCARD};
    int ret = Syntax.TYPE_LIST | Syntax.TYPE_STRING;
    return Syntax.reporterSyntax(right, ret);
  }
}
