package org.nlogo.tortoise.api

import
  scala.js.{ annotation, Any => AnyJS, Array => ArrayJS, Dictionary },
    annotation.expose

import
  org.nlogo.tortoise.{ api, engine },
    api.wrapper.WorldWrapper,
    engine.{ Globals => EGlobals, Overlord => EOverlord, Patch => EPatch, Turtle => ETurtle, World => EWorld }

object Globals {
  @expose def init(count: Int):                     Unit  = EGlobals.init(count)
  @expose def getGlobal(varNum: Int):               AnyJS = EGlobals.getGlobal(varNum)
  @expose def setGlobal(varNum: Int, value: AnyJS): Unit  = EGlobals.setGlobal(varNum, value)
}

object Overlord {
  @expose def collectUpdates(): ArrayJS[Dictionary] = EOverlord.collectUpdates()
}

object Patch {
  @expose def init(count: Int): Unit = EPatch.init(count)
}

object Turtle {
  @expose def init(count: Int): Unit = ETurtle.init(count)
}

object World {
  @expose def generate(minX: Int, maxX: Int, minY: Int, maxY: Int): WorldWrapper = EWorld(minX, maxX, minY, maxY)
}
