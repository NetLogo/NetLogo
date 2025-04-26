package org.nlogo.agent

import
  org.nlogo.core.{ AgentKind, Program }

object Realloc {
  def realloc(world: AgentManagement with CompilationManagement, oldProgram: Program, newProgram: Program): Unit = {
    import scala.jdk.CollectionConverters.SetHasAsScala
    // remove agentsets for breeds that no longer exist, if any
    for(name <- world.breeds.keySet.asScala.toList)
      if(!newProgram.breeds.contains(name))
        world.breeds.remove(name)
    for(name <- world.linkBreeds.keySet.asScala.toList)
      if(!newProgram.linkBreeds.contains(name))
        world.linkBreeds.remove(name)
    // make agentsets for new breeds
    for(breedName <- newProgram.breeds.keys)
      world.breeds.put(breedName,
        Option(world.breeds.get(breedName)).getOrElse(
          new TreeAgentSet(AgentKind.Turtle, breedName.toUpperCase)))
    for(breedName <- newProgram.linkBreeds.keys)
      world.linkBreeds.put(breedName,
        Option(world.linkBreeds.get(breedName)).getOrElse(
          new TreeAgentSet(AgentKind.Link, breedName.toUpperCase)))
    // make sure directednesses are up-to-date
    for((name, breed) <- newProgram.linkBreeds)
      world.linkBreeds.get(name).setDirected(breed.isDirected)

    // call Agent.realloc() on all the turtles
    if (world.turtles != null) {
      val doomedAgents = collection.mutable.Buffer[Turtle]()
      val iter = world.turtles.iterator
      while(iter.hasNext) {
        val agt = iter.next().asInstanceOf[Turtle]
        // first check if we recompiled and our breed disappeared!
        val breedGone =
          (agt.getBreed ne world.turtles) && world.getBreed(agt.getBreed.printName) == null
        if (breedGone)
          doomedAgents += agt
        else
          agt.realloc(oldProgram, newProgram)
      }
      doomedAgents.foreach(_.die())
    }
    // call Agent.realloc() on all links
    if (world.links != null) {
      val doomedAgents = collection.mutable.Buffer[Link]()
      val iter = world.links.iterator
      while(iter.hasNext) {
        val agt = iter.next().asInstanceOf[Link]
        // first check if we recompiled and our breed disappeared!
        val breedGone =
          (agt.getBreed ne world.links) && world.getLinkBreed(agt.getBreed.printName) == null
        if (breedGone)
          doomedAgents += agt
        else
          agt.realloc(oldProgram, newProgram)
      }
      doomedAgents.foreach(_.die())
    }
    // call Agent.realloc() on all the patches
    // Note: we only need to realloc() if the patch variables have changed.
    //  ~Forrest ( 5/2/2007)
    if (world.patches != null && (oldProgram == null ||
      newProgram.patchesOwn != oldProgram.patchesOwn)) {
      val iter = world.patches.iterator
      while(iter.hasNext)
        iter.next().realloc(oldProgram, newProgram)
    }
    // call Agent.realloc() on the observer
    world.observer.realloc(oldProgram, newProgram)
  }
}
