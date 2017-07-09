// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.AgentKindJ;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _setdefaultshape
    extends Command {


  @Override
  public void perform(final Context context)
      throws LogoException {
    org.nlogo.agent.AgentSet breed = argEvalAgentSet(context, 0);
    String shape = argEvalString(context, 1);
    if (breed.kind() == AgentKindJ.Patch()) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._setdefaultshape.cantSetDefaultShapeOfPatch"));
    }
    if (breed.kind() == AgentKindJ.Observer() ) {
      throw new RuntimePrimitiveException(context, this,
          "cannot set the default shape of the observer, because the observer does not have a shape");
    }
    if (breed != world.turtles() && !world.isBreed(breed) &&
        breed != world.links() && !world.isLinkBreed(breed)) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._setdefaultshape.canOnlySetDefaultShapeOfEntireBreed"));
    }
    if (breed.kind() == AgentKindJ.Turtle()) {
      String checkedShape = world.checkTurtleShapeName(shape);
      if (checkedShape == null) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._setDefaultShape.notADefinedTurtleShape", shape));
      }
      world.turtleBreedShapes().setBreedShape(breed, checkedShape);
    } else if (breed.kind() == AgentKindJ.Link()) {
      String checkedShape = world.checkLinkShapeName(shape);
      if (checkedShape == null) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._setDefaultShape.notADefinedLinkShape", shape));
      }
      world.linkBreedShapes().setBreedShape(breed, checkedShape);
    }
    context.ip = next;
  }
}
