// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ Observer, Turtle, Patch }
import org.nlogo.api.{ AgentKind, I18N, LogoException, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setdefaultshape extends Command {

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TurtlesetType | Syntax.LinksetType,
                               Syntax.StringType),
                         "O---")

  override def perform(context: Context) {
    val breed = argEvalAgentSet(context, 0)
    val shape = argEvalString(context, 1)

    if (breed.kind == AgentKind.Patch)
      throw new EngineException(context, this, I18N.errors.get(
        "org.nlogo.prim.etc._setdefaultshape.cantSetDefaultShapeOfPatch"))
    if (breed.kind == AgentKind.Observer)
      throw new EngineException(context, this,
        "cannot set the default shape of the observer, because the observer does not have a shape")
    if ((breed ne world.turtles) && !world.isBreed(breed) &&
        (breed ne world.links) && !world.isLinkBreed(breed))
      throw new EngineException(context, this,
          I18N.errors.get("org.nlogo.prim.etc._setdefaultshape.canOnlySetDefaultShapeOfEntireBreed"))

    if (breed.kind == AgentKind.Turtle) {
      val checkedShape = world.checkTurtleShapeName(shape)
      if (checkedShape == null)
        throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.prim.etc._setDefaultShape.notADefinedTurtleShape", shape))
      world.turtleBreedShapes.setBreedShape(breed, checkedShape)
    }
    else { // link breed
      val checkedShape = world.checkLinkShapeName(shape)
      if (checkedShape == null)
        throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.prim.etc._setDefaultShape.notADefinedLinkShape", shape))
      world.linkBreedShapes.setBreedShape(breed, checkedShape)
    }
    context.ip = next
  }
}
