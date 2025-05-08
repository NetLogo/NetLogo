// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.api.{ MersenneTwisterFast, WorldType }

class TopologyDiffusionTests extends AnyFunSuite {

  val random = new MersenneTwisterFast()

  for(worldType <- WorldType.all; x <- List(0, 1, 2, 3, 10, 100); y <- List(0, 1, 2, 3, 10, 100)){
    test("diffuse, worldType: " + worldType + ", worldSize: " + ((x, y))) {
      import scala.jdk.CollectionConverters.IterableHasAsScala

      val world = new World2D
      world.changeTopology(worldType.xWrap, worldType.yWrap)
      world.createPatches(-x, x, -y, y)
      for(a <- world.patches.agents.asScala)
        a.setVariable(Patch.VAR_PLABEL, random.nextInt(100).toDouble: java.lang.Double)
      world.diffuse(0.5, Patch.VAR_PLABEL)
    }
  }

}
