// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, agent, nvm },
  agent.{ Turtle, Link, LinkManager, AgentSet, AgentSetBuilder },
  nvm.{ Command, Context, EngineException }

trait LinkCreationCommand extends Command with nvm.CustomAssembled {
  // abstract
  def breedName: String
  def inputType: Int
  def swapEnds(me: Turtle, other: Turtle): Boolean
  def checkDirectedness(context: Context, breed: AgentSet)
  def linkAlreadyExists(src: Turtle, dest: Turtle, breed: AgentSet): Boolean
  def create(context: Context, breed: AgentSet, me: Turtle): AgentSet
  // overrides
  override def toString =
    super.toString + ":" + breedName + ",+" + offset
  override def syntax =
    api.Syntax.commandSyntax(
      Array(inputType, api.Syntax.CommandBlockType | api.Syntax.OptionalType),
      "-T--", "---L", true)
  override def perform(context: Context) {
    val breed =
      if (breedName.isEmpty)
        world.links
      else
        world.getLinkBreed(breedName)
    checkDirectedness(context, breed)
    val newAgents = create(context, breed, context.agent.asInstanceOf[Turtle])
    if (offset - context.ip > 2 && !newAgents.isEmpty)
      context.runExclusiveJob(newAgents, next)
    context.ip = offset
  }
  override def assemble(a: nvm.AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }
  // helpers
  def checkForBreedCompatibility(context: Context, breed: AgentSet) {
    if (!world.linkManager.checkBreededCompatibility(breed eq world.links))
      throw new EngineException(
        context, this, api.I18N.errors.get(
          "org.nlogo.agent.Link.cantHaveBreededAndUnbreededLinks"))
  }
  def newLink(context: Context, src: Turtle, dest: Turtle, breed: AgentSet): Option[Link] =
    if (src eq dest)
      throw new EngineException(
        context, this, api.I18N.errors.get(
          "org.nlogo.prim.$common.turtleCantLinkToSelf"))
    else if (src.id == -1 || dest.id == -1 || linkAlreadyExists(src, dest, breed))
      None
    else {
      val link = world.linkManager.createLink(src, dest, breed)
      workspace.joinForeverButtons(link)
      Some(link)
    }
  def perhapsLink(context: Context, me: Turtle, other: Turtle, breed: AgentSet) =
    if (swapEnds(me, other))
      newLink(context, other, me, breed)
    else
      newLink(context, me, other, breed)
}

trait Single extends LinkCreationCommand {
  override def inputType = api.Syntax.TurtleType
  override def create(context: Context, breed: AgentSet, me: Turtle) =
    perhapsLink(context, me, argEvalTurtle(context, 0), breed)
      .map(AgentSet.fromAgent)
      .getOrElse(world.noLinks)
}

trait Multiple extends LinkCreationCommand {
  override def inputType = api.Syntax.TurtlesetType
  override def create(context: Context, breed: AgentSet, me: Turtle) = {
    val others = argEvalAgentSet(context, 0)
    val builder = new AgentSetBuilder(api.AgentKind.Link, others.count)
    // we must shuffle so who number assignment is random - ST 3/15/06
    val iter = others.shufflerator(context.job.random)
    while(iter.hasNext) {
      val other = iter.next().asInstanceOf[Turtle]
      for (link <- perhapsLink(context, me, other, breed))
        builder.add(link)
    }
    builder.build()
  }
}

trait Directed extends LinkCreationCommand {
  override def checkDirectedness(context: Context, breed: AgentSet) {
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    checkForBreedCompatibility(context, breed)
    if (breed eq world.links)
      breed.setDirected(true)
  }
  override def linkAlreadyExists(src: Turtle, dest: Turtle, breed: AgentSet): Boolean =
    world.linkManager.findLinkFrom(src, dest, breed, false) != null
}

trait DirectedTo extends Directed {
  override def swapEnds(me: Turtle, other: Turtle) = false
}
trait DirectedFrom extends Directed {
  override def swapEnds(me: Turtle, other: Turtle) = true
}

trait Undirected extends LinkCreationCommand {
  override def checkDirectedness(context: Context, breed: AgentSet) {
    for(err <- LinkManager.mustNotBeDirected(breed))
      throw new EngineException(context, this, err)
    checkForBreedCompatibility(context, breed)
    if (breed eq world.links)
      breed.setDirected(false)
  }
  override def linkAlreadyExists(src: Turtle, dest: Turtle, breed: AgentSet): Boolean =
    world.linkManager.findLinkEitherWay(src, dest, breed, false) != null
  override def swapEnds(me: Turtle, other: Turtle) = me.id > other.id
}

class _createlinkwith (val breedName: String) extends Single   with Undirected   {
  def this() = this("") }
class _createlinkto   (val breedName: String) extends Single   with DirectedTo   {
  def this() = this("") }
class _createlinkfrom (val breedName: String) extends Single   with DirectedFrom {
  def this() = this("") }
class _createlinkswith(val breedName: String) extends Multiple with Undirected   {
  def this() = this("") }
class _createlinksto  (val breedName: String) extends Multiple with DirectedTo   {
  def this() = this("") }
class _createlinksfrom(val breedName: String) extends Multiple with DirectedFrom {
  def this() = this("") }
