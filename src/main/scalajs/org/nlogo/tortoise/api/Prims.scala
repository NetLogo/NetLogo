package org.nlogo.tortoise.api

import
  scala.js.{ annotation, Array => ArrayJS },
    annotation.expose

import
  org.nlogo.tortoise.{ api, engine },
    api.wrapper.PatchWrapper,
    engine.{ Prims => EPrims }

object Prims {
  @expose def fd(dist: Double):            Unit                  = EPrims.fd(dist)
  @expose def bk(dist: Double):            Unit                  = EPrims.bk(dist)
  @expose def right(angle: Int):           Unit                  = EPrims.right(angle)
  @expose def left(angle: Int):            Unit                  = EPrims.left(angle)
  @expose def setxy(x: Double, y: Double): Unit                  = EPrims.setxy(x, y)
  @expose def getNeighbors:                ArrayJS[PatchWrapper] = EPrims.getNeighbors
  @expose def sprout(count: Int):          Unit                  = EPrims.sprout(count)
  @expose def patch(x: Double, y: Double): PatchWrapper          = EPrims.patch(x, y)
  @expose def randomxcor():                Double                = EPrims.randomxcor()
  @expose def randomycor():                Double                = EPrims.randomycor()
}
