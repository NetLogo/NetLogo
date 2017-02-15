// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.LogoHashObject;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.MutableInteger;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;
import java.util.LinkedHashMap;

public final strictfp class _modes
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    LinkedHashMap<LogoHashObject, MutableInteger> counts =
        new LinkedHashMap<LogoHashObject, MutableInteger>();
    LogoList list = argEvalList(context, 0);
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object srcElt = it.next();
      LogoHashObject logoElt = new LogoHashObject(srcElt);
      if (counts.containsKey(logoElt)) {
        MutableInteger i = counts.get(logoElt);
        i.value_$eq(i.value() + 1);
      } else {
        counts.put(logoElt, new MutableInteger(1));
      }
    }

    Iterator<LogoHashObject> keys = counts.keySet().iterator();
    int currMaxCount = 0;
    while (keys.hasNext()) {
      LogoHashObject currKey = keys.next();
      int currVal = counts.get(currKey).value();
      if (currVal > currMaxCount) {
        currMaxCount = currVal;
      }
    }

    keys = counts.keySet().iterator();
    LogoListBuilder modes = new LogoListBuilder();
    while (keys.hasNext()) {
      LogoHashObject currKey = keys.next();
      int currVal = counts.get(currKey).value();
      if (currVal == currMaxCount) {
        modes.add(currKey.getSourceObject());
      }
    }
    return modes.toLogoList();
  }


}
