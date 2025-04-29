// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _setdefaultshape extends Command {

  override def perform(context: Context): Unit = {
    val breed = argEvalAgentSet(context, 0)
    val shape = argEvalString(context, 1)

    if (breed.kind == AgentKind.Patch)
      throw new RuntimePrimitiveException(context, this, I18N.errors.get(
        "org.nlogo.prim.etc._setdefaultshape.cantSetDefaultShapeOfPatch"))
    if (breed.kind == AgentKind.Observer)
      throw new RuntimePrimitiveException(context, this,
        "cannot set the default shape of the observer, because the observer does not have a shape")
    if ((breed ne world.turtles) && !world.isBreed(breed) &&
        (breed ne world.links) && !world.isLinkBreed(breed))
      throw new RuntimePrimitiveException(context, this,
          I18N.errors.get("org.nlogo.prim.etc._setdefaultshape.canOnlySetDefaultShapeOfEntireBreed"))

    if (breed.kind == AgentKind.Turtle) {
      val checkedShape = world.checkTurtleShapeName(shape)
      if (checkedShape == null)
        throw new RuntimePrimitiveException(context, this,
            I18N.errors.getN("org.nlogo.prim.etc._setDefaultShape.notADefinedTurtleShape", shape))
      world.turtleBreedShapes.setBreedShape(breed, checkedShape)
    }
    else { // link breed
      val checkedShape = world.checkLinkShapeName(shape)
      if (checkedShape == null)
        throw new RuntimePrimitiveException(context, this,
            I18N.errors.getN("org.nlogo.prim.etc._setDefaultShape.notADefinedLinkShape", shape))
      world.linkBreedShapes.setBreedShape(breed, checkedShape)
    }
    context.ip = next
  }
}
