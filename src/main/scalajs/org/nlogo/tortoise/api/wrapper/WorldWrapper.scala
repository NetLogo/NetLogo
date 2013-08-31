package org.nlogo.tortoise.api.wrapper

import
  scala.js.annotation.expose

import
  org.nlogo.tortoise.engine.{ World, XCor, YCor }

class WorldWrapper(val world: World) extends Wrapper {
  @expose def clearall():                       Unit        = world.clearall()
  @expose def createorderedturtles(n: Int):     Unit        = world.createorderedturtles(n)
  @expose def createturtles(n: Int):            Unit        = world.createturtles(n)
  @expose def getPatchAt(x: Double, y: Double): org.nlogo.tortoise.engine.Patch   = world.getPatchAt(XCor(x), YCor(y))
  @expose def patches:                      Seq[org.nlogo.tortoise.engine.Patch]  = world.patches
  @expose def turtles:                      Seq[org.nlogo.tortoise.engine.Turtle] = world.turtles
}
