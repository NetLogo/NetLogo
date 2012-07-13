// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.Program

// this exists to support recompiling a model without causing agent state information to be lost.
// it is called after a successful recompilation.

object Realloc {

  def realloc(world: World) {
    import world.program
    // copy the breed agentsets from the old Program object from the previous compile to the new
    // Program object that was created when we recompiled.  any new breeds that were created, we
    // create new agentsets for.  (if this is a first compile, all the breeds will be created.)  any
    // breeds that no longer exist are dropped.
    for(breedName <- program.breeds.keys) {
      val breed = world.oldBreeds.get(breedName).asInstanceOf[AgentSet]
      val newBreed =
        if (breed == null)
          new TreeAgentSet(classOf[Turtle], breedName.toUpperCase, world)
        else
          breed
      program._breeds(breedName).agents = newBreed
    }
    for(breedName <- program.linkBreeds.keys) {
      val directed = program._linkBreeds(breedName).isDirected
      var breed = Option(world.oldLinkBreeds.get(breedName)).map(_.asInstanceOf[AgentSet]).orNull
      if (breed == null)
        breed = new TreeAgentSet(classOf[Link], breedName.toUpperCase, world)
      else // clear the lists first
        breed.clearDirected()
      program._linkBreeds(breedName).agents = breed
      breed.setDirected(directed)
    }
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
    if (world.patches != null && program.patchesOwn != world.oldPatchesOwn) {
      val iter = world.patches.iterator
      while(iter.hasNext)
        iter.next().realloc(true)
    }
    // call Agent.realloc() on the observer
    world.observer.realloc(true)
    // and finally...
    world.turtleBreedShapes.setUpBreedShapes(false, program.breedsJ)
    world.linkBreedShapes.setUpBreedShapes(false, program.linkBreedsJ)
  }

}
