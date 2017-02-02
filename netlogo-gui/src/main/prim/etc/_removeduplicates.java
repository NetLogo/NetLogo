// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.LogoHashObject;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.HashSet;
import java.util.Iterator;

public final strictfp class _removeduplicates
    extends Reporter
    implements org.nlogo.core.Pure {


  @Override
  public Object report(Context context) throws LogoException {
    LogoList list = argEvalList(context, 0);
    LogoListBuilder result = new LogoListBuilder();
    HashSet<Object> seenHash = new HashSet<Object>();
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object srcElt = it.next();
      LogoHashObject logoElt = new LogoHashObject(srcElt);
      if (!seenHash.contains(logoElt)) {
        seenHash.add(logoElt);
        result.add(srcElt);
      }
    }
    return result.toLogoList();
  }
}
