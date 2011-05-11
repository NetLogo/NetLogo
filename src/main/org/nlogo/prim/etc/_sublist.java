package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _sublist
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    LogoList list = argEvalList(context, 0);
    int start = argEvalIntValue(context, 1);
    int stop = argEvalIntValue(context, 2);
    int size = list.size();
    if (start < 0) {
      throw new EngineException
          (context, this, I18N.errors().getNJava("org.nlogo.prim.etc._sublist.startIsLessThanZero",
              new String[]{new Integer(start).toString()}));
    } else if (stop < start) {
      throw new EngineException
          (context, this, I18N.errors().getNJava("org.nlogo.prim.etc._sublist.endIsLessThanStart",
              new String[]{new Integer(stop).toString(), new Integer(start).toString()}));

    } else if (stop > size) {
      throw new EngineException
          (context, this, I18N.errors().getNJava("org.nlogo.prim.etc._sublist.endIsGreaterThanListSize",
              new String[]{new Integer(stop).toString(), new Integer(size).toString()}));
    }
    return list.logoSublist(start, stop);
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_LIST, Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER},
            Syntax.TYPE_LIST);
  }
}
