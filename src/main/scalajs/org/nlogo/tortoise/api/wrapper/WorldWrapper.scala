package org.nlogo.tortoise.api
package wrapper

import
  scala.js.{ annotation, Array => ArrayJS },
    annotation.expose

import
  org.nlogo.tortoise.engine.{ World, XCor, YCor }

class WorldWrapper(override val value: World) extends Wrapper {
  override type ValueType = World
  @expose def clearall():                       Unit                   = value.clearall()
  @expose def createorderedturtles(n: Int):     Unit                   = value.createorderedturtles(n)
  @expose def createturtles(n: Int):            Unit                   = value.createturtles(n)
  @expose def getPatchAt(x: Double, y: Double): PatchWrapper           = value.getPatchAt(XCor(x), YCor(y))
  @expose def patches:                          ArrayJS[PatchWrapper]  = value.patches
  @expose def turtles:                          ArrayJS[TurtleWrapper] = value.turtles
}
