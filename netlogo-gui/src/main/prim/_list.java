// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _list
    extends Reporter
    implements org.nlogo.core.Pure, org.nlogo.nvm.CustomGenerated {

  @Override
  public int returnType() {
    return Syntax.ListType();
  }



  @Override
  public Object report(final Context context) throws LogoException {
    LogoListBuilder list = new LogoListBuilder();
    for (int i = 0; i < args.length; i++) {
      list.add(args[i].report(context));
    }
    return list.toLogoList();
  }
}
