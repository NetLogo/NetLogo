package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _item
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    int index = argEvalIntValue(context, 0);
    Object obj = args[1].report(context);
    if (index < 0) {
      throw new EngineException
          (context, this, I18N.errors().getNJava("org.nlogo.prim.etc.$common.negativeIndex", new String[]{new Integer(index).toString()}));
    }
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (index >= list.size()) {
        throw new EngineException
            (context, this, I18N.errors().getNJava("org.nlogo.prim.etc.$common.indexExceedsListSize",
                new String[]{new Integer(index).toString(), Dump.logoObject(list), new Integer(list.size()).toString()}));

      }
      return list.get(index);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (index >= string.length()) {
        throw new EngineException
            (context, this, I18N.errors().getNJava("org.nlogo.prim.etc.$common.indexExceedsListSize",
                new String[]{new Integer(index).toString(), Dump.logoObject(string), new Integer(string.length()).toString()}));
      }
      return string.substring(index, index + 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.TYPE_LIST | Syntax.TYPE_STRING, obj);
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_LIST | Syntax.TYPE_STRING};
    int ret = Syntax.TYPE_WILDCARD;
    return Syntax.reporterSyntax(right, ret);
  }
}
