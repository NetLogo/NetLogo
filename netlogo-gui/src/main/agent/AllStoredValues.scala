// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// Exists so export-world can bring up a warning if someone exports tasks, since they can't be
// re-imported.  This could be in World I suppose, but World is in Java right now and I want to
// write this in Scala. - ST 8/10/11

// Doesn't handle ExtensionObjects, so tasks inside arrays and tables will be missed.
// Letting it slide for now. - ST 8/10/11

import org.nlogo.api.LogoList
import collection.JavaConverters._

object AllStoredValues {
  def apply(world: World): Iterator[AnyRef] = {
    val agents: Iterator[org.nlogo.api.Agent] =
      world.observers.agents.iterator.asScala ++
      world.turtles.agents.iterator.asScala ++
      world.patches.agents.iterator.asScala ++
      world.links.agents.iterator.asScala
    def contents(x: AnyRef): Iterator[AnyRef] =
      Iterator(x) ++ (x match {
        case l: LogoList =>
          l.scalaIterator.flatMap(contents)
        case _ =>
          Iterator.empty
      })
    agents.flatMap{a => Iterator(a) ++ a.variables.iterator.flatMap(contents)}
  }
}
