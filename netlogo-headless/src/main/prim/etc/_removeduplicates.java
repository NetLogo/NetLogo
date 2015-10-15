// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoHashObject;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import java.util.HashSet;

public final strictfp class _removeduplicates
    extends Reporter
    implements Pure {

  @Override
  public Object report(Context context) {
    LogoList list = argEvalList(context, 0);
    LogoListBuilder result = new LogoListBuilder();
    HashSet<Object> seenHash = new HashSet<Object>();
    for (Object srcElt : list.toJava()) {
      LogoHashObject logoElt = new LogoHashObject(srcElt);
      if (!seenHash.contains(logoElt)) {
        seenHash.add(logoElt);
        result.add(srcElt);
      }
    }
    return result.toLogoList();
  }
}
