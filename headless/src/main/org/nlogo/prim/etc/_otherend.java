// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _otherend
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.AgentType(), "-T-L");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Link link;
    Turtle node;
    if (context.agent instanceof Link) {
      link = (Link) context.agent;
      if (!(context.myself() instanceof Turtle)) {
        throw new EngineException(context, this,
            I18N.errorsJ().get("org.nlogo.prim.etc._otherend.onlyTurtleCanGetLinkEnd"));
      }
      node = (Turtle) context.myself();
    } else {
      node = (Turtle) context.agent;
      if (!(context.myself() instanceof Link)) {
        throw new EngineException(context, this,
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

    throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.prim.etc._otherend.incorrectLink", node.toString(), link.toString()));
  }
}
