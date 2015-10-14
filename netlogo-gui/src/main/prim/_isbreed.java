// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _isbreed
    extends Reporter {
  final String breedName;

  public _isbreed(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Object report(final Context context) throws LogoException {
    Object thing = args[0].report(context);
    if (thing instanceof Turtle) {
      Turtle turtle = (Turtle) thing;
      return (turtle.id != -1 &&
          turtle.getBreed() == world.getBreed(breedName))
          ? Boolean.TRUE
          : Boolean.FALSE;
    } else if (thing instanceof Link) {
      Link link = (Link) thing;
      return (link.id != -1 &&
          link.getBreed() == world.getLinkBreed(breedName))
          ? Boolean.TRUE
          : Boolean.FALSE;
    } else {
      return Boolean.FALSE;
    }
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.WildcardType()},
            Syntax.BooleanType());
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }
}
