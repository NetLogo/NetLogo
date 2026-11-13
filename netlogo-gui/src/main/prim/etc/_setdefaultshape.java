// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.AgentKindJ;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setdefaultshape
    extends Command {


  @Override
  public void perform(final Context context)
      throws LogoException {
    org.nlogo.agent.AgentSet breed = argEvalAgentSet(context, 0);
    String shape = argEvalString(context, 1);
    if (breed.kind() == AgentKindJ.Patch()) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Cannot set the default shape of patches, because patches do not have shapes."));
    }
    if (breed.kind() == AgentKindJ.Observer() ) {
      throw new RuntimePrimitiveException(context, this,
          "cannot set the default shape of the observer, because the observer does not have a shape");
    }
    if (breed != world.turtles() && !world.isBreed(breed) &&
        breed != world.links() && !world.isLinkBreed(breed)) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Can only set the default shape of all turtles , all links, or an entire breed."));
    }
    if (breed.kind() == AgentKindJ.Turtle()) {
      String checkedShape = world.checkTurtleShapeName(shape);
      if (checkedShape == null) {
        throw new RuntimePrimitiveException(context, this,
          LocaleContextJ.loc("\"{}\" is not a currently defined turtle shape.", shape));
      }
      world.turtleBreedShapes().setBreedShape(breed, checkedShape);
    } else if (breed.kind() == AgentKindJ.Link()) {
      String checkedShape = world.checkLinkShapeName(shape);
      if (checkedShape == null) {
        throw new RuntimePrimitiveException(context, this,
          LocaleContextJ.loc("\"{}\" is not a currently defined link shape.", shape));
      }
      world.linkBreedShapes().setBreedShape(breed, checkedShape);
    }
    context.ip = next;
  }
}
