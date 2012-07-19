// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.Program
import collection.JavaConverters._

// this exists to support recompiling a model without causing agent state information to be lost.
// it is called after a successful recompilation.

object Realloc {

  def realloc(world: World) {
    import world.program
    // remove agentsets for breeds that no longer exist, if any
    for(name <- world.breedAgents.asScala.keys)
      if(!program.breeds.contains(name))
        world.breedAgents.remove(name)
    for(name <- world.linkBreedAgents.asScala.keys)
      if(!program.linkBreeds.contains(name))
        world.linkBreedAgents.remove(name)
    // make agentsets for new breeds
    for(breedName <- program.breeds.keys)
      world.breedAgents.put(breedName,
        Option(world.breedAgents.get(breedName)).getOrElse(
          new TreeAgentSet(classOf[Turtle], breedName.toUpperCase, world)))
    for(breedName <- program.linkBreeds.keys)
      world.linkBreedAgents.put(breedName,
        Option(world.linkBreedAgents.get(breedName)).getOrElse(
          new TreeAgentSet(classOf[Link], breedName.toUpperCase, world)))
    // make sure directednesses are up-to-date
    for((name, breed) <- program.linkBreeds)
      world.linkBreedAgents.get(name).setDirected(breed.isDirected)
    // call Agent.realloc() on all the turtles
    val doomedAgents = collection.mutable.Buffer[Agent]()
    if (world.turtles != null) {
      val iter = world.turtles.iterator
      while(iter.hasNext) {
        val agt = iter.next().realloc(true)
        if (agt != null)
          doomedAgents += agt
      }
      doomedAgents.foreach(_.asInstanceOf[Turtle].die())
      doomedAgents.clear()
    }
    // call Agent.realloc() on all links
    if (world.links != null) {
      val iter = world.links.iterator
      while(iter.hasNext) {
        val agt = iter.next().realloc(true)
        if (agt != null)
          doomedAgents += agt
      }
      doomedAgents.foreach(_.asInstanceOf[Link].die())
      doomedAgents.clear()
    }
    // call Agent.realloc() on all the patches
    // Note: we only need to realloc() if the patch variables have changed.
    //  ~Forrest ( 5/2/2007)
    if (world.patches != null && program.patchesOwn != world.oldProgram.patchesOwn) {
      val iter = world.patches.iterator
      while(iter.hasNext)
        iter.next().realloc(true)
    }
    // call Agent.realloc() on the observer
    world.observer.realloc(true)
    // and finally...
    world.turtleBreedShapes.setUpBreedShapes(false, program.breeds)
    world.linkBreedShapes.setUpBreedShapes(false, program.linkBreeds)
  }

}
