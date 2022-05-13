// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Observer;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _myself
    extends Reporter {


  @Override
  public Object report(final Context context) throws LogoException {
    Agent myself = context.myself();
    if (myself == null || myself instanceof Observer) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._myself.noAgentMyself"));
    }
    return myself;
  }

  public Agent report_1(Context context) throws LogoException {
    Agent myself = context.myself();
    if (myself == null || myself instanceof Observer) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._myself.noAgentMyself"));
    }
    return myself;
  }
}
