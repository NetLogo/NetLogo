// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _linkheading extends Reporter {

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public double report_1(Context context) {
    try {
      Link link = (Link) context.agent;
      return world.protractor().towards(link.end1(), link.end2(), true);
    } catch (org.nlogo.api.AgentException e) {
      throw new org.nlogo.nvm.RuntimePrimitiveException
          (context, this,
              "there is no heading of a link whose endpoints are in the same position");
    }
  }
}
