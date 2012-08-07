// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _linkbreedsingular
    extends Reporter {
  private final String breedName;

  public _linkbreedsingular(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Object report(final Context context) throws LogoException {
    AgentSet breed = world.getLinkBreed(breedName);
    Link link = world.getLink(argEvalDouble(context, 0),
        argEvalDouble(context, 1), breed);
    if (link == null) {
      return Nobody$.MODULE$;
    }
    return link;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType(), Syntax.NumberType()},
            Syntax.LinkType() | Syntax.NobodyType());
  }
}
