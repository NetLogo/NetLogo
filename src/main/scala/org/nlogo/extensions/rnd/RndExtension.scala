// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.extensions.rnd

import org.nlogo.api.DefaultClassManager
import org.nlogo.api.PrimitiveManager

class RndExtension extends DefaultClassManager {
  override def load(primManager: PrimitiveManager): Unit = {
    for {
      prim <- Seq(
        WeightedOneOfAgentSet,
        WeightedOneOfList,
        WeightedNOfAgentSetWithRepeats,
        WeightedNOfListWithRepeats,
        WeightedNOfAgentSetWithoutRepeats,
        WeightedNOfListWithoutRepeats)
    } primManager.addPrimitive(prim.name, prim)
  }
}
