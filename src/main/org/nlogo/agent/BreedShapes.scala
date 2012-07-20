// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// methods need to be synchronized so since they can run on both the JobManager thread and the event
// thread without corrupting the shapes map.  we are safe from deadlock here since the methods are
// self-contained. we don't want to synchronize on the world since other classes do that and this
// really shouldn't affect their performance.  so we use a lock object.  --mag 10/03/03

import collection.JavaConverters._
import org.nlogo.api.Breed

class BreedShapes(genericBreedName: String) {

  private[this] val lock = new AnyRef
  private val shapes = collection.mutable.Map[String, String]()

  def setUpBreedShapes(clear: Boolean, breeds: collection.immutable.ListMap[String, Breed]) {
    lock.synchronized {
      if (clear)
        shapes.clear()
      shapes.getOrElseUpdate(genericBreedName, "default")
    }
  }

  def removeFromBreedShapes(shapeName: String) {
    lock.synchronized {
      for ((key, _) <- shapes.find(_._2 == shapeName))
        shapes.remove(key)
    }
  }

  def breedShape(breed: AgentSet): String =
    lock.synchronized {
      shapes.get(breed.printName).getOrElse(shapes(genericBreedName))
    }

  def setBreedShape(breed: AgentSet, shapeName: String) {
    lock.synchronized {
      shapes(breed.printName) = shapeName
    }
  }

}
