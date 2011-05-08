package org.nlogo.agent

import org.scalatest.FunSuite
import org.nlogo.util.JCL.toScalaIterable
import util.Random
import org.nlogo.util.WorldType

class VertCylinderTests extends FunSuite {

  val random = new Random()

  for(worldType <- WorldType.all; x <- List(0, 1, 2, 3, 10, 100); y <- List(0, 1, 2, 3, 10, 100)){
    test("diffuse, worldType: " + worldType + ", worldSize: " + (x,y)){
      val world = new World
      world.createPatches(-x, x, -y, y)
      world.patches.agents.foreach{ a => a.setVariable(Patch.VAR_PLABEL, random.nextInt(100).toDouble) }
      world.diffuse(0.5, Patch.VAR_PLABEL)
    }
  }

}
