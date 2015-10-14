// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _setdefaultshape
    extends Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TurtlesetType() | Syntax.LinksetType(),
        Syntax.StringType()};
    return Syntax.commandSyntax(right, "O---");
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    org.nlogo.agent.AgentSet breed = argEvalAgentSet(context, 0);
    String shape = argEvalString(context, 1);
    if (breed.type() == org.nlogo.agent.Patch.class) {
      throw new EngineException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._setdefaultshape.cantSetDefaultShapeOfPatch"));
    }
    if (breed.type() == org.nlogo.agent.Observer.class) {
      throw new EngineException(context, this,
          "cannot set the default shape of the observer, because the observer does not have a shape");
    }
    if (breed != world.turtles() && !world.isBreed(breed) &&
        breed != world.links() && !world.isLinkBreed(breed)) {
      throw new EngineException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._setdefaultshape.canOnlySetDefaultShapeOfEntireBreed"));
    }
    if (breed.type() == org.nlogo.agent.Turtle.class) {
      String checkedShape = world.checkTurtleShapeName(shape);
      if (checkedShape == null) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._setDefaultShape.notADefinedTurtleShape", shape));
      }
      world.turtleBreedShapes.setBreedShape(breed, checkedShape);
    } else if (breed.type() == org.nlogo.agent.Link.class) {
      String checkedShape = world.checkLinkShapeName(shape);
      if (checkedShape == null) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._setDefaultShape.notADefinedLinkShape", shape));
      }
      world.linkBreedShapes.setBreedShape(breed, checkedShape);
    }
    context.ip = next;
  }
}
