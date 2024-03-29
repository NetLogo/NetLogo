// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _otherend
    extends Reporter {

  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    Link link;
    Turtle node;
    if (context.agent instanceof Link) {
      link = (Link) context.agent;
      if (!(context.myself() instanceof Turtle)) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().get("org.nlogo.prim.etc._otherend.onlyTurtleCanGetLinkEnd"));
      }
      node = (Turtle) context.myself();
    } else {
      node = (Turtle) context.agent;
      if (!(context.myself() instanceof Link)) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().get("org.nlogo.prim.etc._otherend.onlyLinkCanGetTurtleEnd"));
      }
      link = (Link) context.myself();
    }

    Turtle dest = link.end2();
    Turtle src = link.end1();
    if (dest == node) {
      return src;
    }
    if (src == node) {
      return dest;
    }

    throw new RuntimePrimitiveException(context, this,
        I18N.errorsJ().getN("org.nlogo.prim.etc._otherend.incorrectLink", node.toString(), link.toString()));
  }
}
