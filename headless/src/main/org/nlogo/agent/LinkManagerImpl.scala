// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api
import collection.JavaConverters._

class LinkManagerImpl(val world: World) extends LinkManager {

  // Use LinkedHashMap not HashMap for these so model results are
  // reproducible. - ST 12/21/05, 3/15/06, 7/20/07
  private val srcMap  = new java.util.LinkedHashMap[Turtle, java.util.List[Link]]
  private val destMap = new java.util.LinkedHashMap[Turtle, java.util.List[Link]]

  ///

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link = {
    val link = newLink(world, src, dest, breed)
    link.colorDoubleUnchecked(Link.DEFAULT_COLOR)
    bless(link)
    link
  }

  // exists as separate method so we can override in LinkManager3D
  protected[agent] def newLink(world: World, src: Turtle, dest: Turtle, breed: AgentSet) =
    new Link(world, src, dest, breed)

  ///

  private var countUnbreededLinks = 0

  def reset() {
    srcMap.clear()
    destMap.clear()
    world.tieManager.reset()
    countUnbreededLinks = 0
    resetLinkDirectedness()
  }

  private def resetLinkDirectedness() {
    if (countUnbreededLinks == 0)
      world.links.clearDirected()
  }

  def checkBreededCompatibility(unbreeded: Boolean): Boolean = {
    val it = world.links.iterator
    !it.hasNext || unbreeded == (it.next().asInstanceOf[Link].getBreed eq world.links)
  }

  ///

  private def bless(link: Link) {
    val end1 = link.end1
    val end2 = link.end2
    // add to source map
    if (srcMap.containsKey(end1))
      srcMap.get(end1).add(link)
    else {
      val recList = new java.util.ArrayList[Link]
      recList.add(link)
      srcMap.put(end1, recList)
    }
    // add to destination map
    if (destMap.containsKey(end2))
      destMap.get(end2).add(link)
    else {
      val recList = new java.util.ArrayList[Link]
      recList.add(link)
      destMap.put(end2, recList)
    }
    if (link.getBreed eq world.links)
      countUnbreededLinks += 1
  }

  private[agent] def cleanup(link: Link) {
    // keep tie bookkeeping up to date
    link.untie()
    // remove from source map
    val end1 = link.end1
    var list = srcMap.get(end1)
    if (list != null) {
      list.remove(link)
      if (list.isEmpty)
        srcMap.remove(end1)
    }
    // remove from dest map
    val end2 = link.end2
    list = destMap.get(end2)
    if (list != null) {
      list.remove(link)
      if (list.isEmpty)
        destMap.remove(end2)
    }
    if (link.getBreed eq world.links)
      countUnbreededLinks -= 1
    // were we the last link?
    resetLinkDirectedness()
  }

  // Turtle.die() calls this - ST 3/15/06, 7/21/07
  private[agent] def cleanup(turtle: Turtle) {
    // this part is a bit tricky -- we need to remove the turtle
    // from the src & dest maps first, so we don't end up in an
    // infinite loop where a dying node kills a link which tries
    // to kill the original node.  But we need the map entries
    // in order to find the links.  Hence the exact ordering
    // inside each if statement below. - ST 3/15/06
    if (srcMap.containsKey(turtle)) {
      val links = srcMap.get(turtle)
      srcMap.remove(turtle)
      for (link <- links.asScala)
        link.die()
    }
    if (destMap.containsKey(turtle)) {
      val links = destMap.get(turtle)
      destMap.remove(turtle)
      for (link <- links.asScala)
        link.die()
    }
  }

  ///

  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): AgentSet = {
    val fromList = srcMap.get(src)
    if (fromList == null)
      world.noTurtles
    else {
      val builder = new AgentSetBuilder(api.AgentKind.Turtle, fromList.size)
      addLinkNeighborsFrom(builder, fromList, sourceSet, true)
      builder.build()
    }
  }

  def findLinkedTo(target: Turtle, sourceSet: AgentSet): AgentSet = {
    val fromList = destMap.get(target)
    if (fromList == null)
      world.noTurtles
    else {
      val builder = new AgentSetBuilder(api.AgentKind.Turtle, fromList.size)
      addLinkNeighborsTo(builder, fromList, sourceSet, true)
      builder.build()
    }
  }

  def findLinkedWith(target: Turtle, sourceSet: AgentSet): AgentSet = {
    val toList = destMap.get(target)
    val fromList = srcMap.get(target)
    val size =
      (if (fromList == null) 0 else fromList.size) +
      (if (toList   == null) 0 else toList  .size)
    if (size == 0)
      world.noTurtles
    else {
      val builder = new AgentSetBuilder(api.AgentKind.Turtle, size)
      if (toList != null)
        addLinkNeighborsTo(builder, toList, sourceSet, false)
      if (fromList != null)
        addLinkNeighborsFrom(builder, fromList, sourceSet, false)
      builder.build()
    }
  }

  // the next two methods are essentially the same but are separate for
  // performance reasons ev 4/26/07
  // these are used in two cases, either for link-neighbors in which case
  // sourceSet will always be a breed. but layout-radial also uses it
  // and it might be any agentset.  ev 4/6/07
  private def addLinkNeighborsFrom(builder: AgentSetBuilder, links: java.util.List[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links.asScala)
      if ((!isBreed && sourceSet.contains(link)) || (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !builder.contains(link.end1)))) || (link.getBreed eq sourceSet))
        builder.add(link.end2)
  }

  private def addLinkNeighborsTo(builder: AgentSetBuilder, links: java.util.List[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    // if we have unbreeded links we know that there is only one possible link
    // between two turtles, thus we don't have to check if the end point is already
    // in the nodeset, which is slow. so only models that use breeds && link-neighbors
    // will take a performance hit ev 6/15/07
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links.asScala)
      if ((!isBreed && sourceSet.contains(link)) || (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !builder.contains(link.end1)))) || (link.getBreed eq sourceSet))
        builder.add(link.end1)
  }

  /// tie

  def tiedTurtles(root: Turtle): java.util.List[Turtle] = {
    val myTies = new java.util.ArrayList[Turtle]
    if (srcMap.containsKey(root))
      for(link <- srcMap.get(root).asScala)
        if (link.isTied)
          myTies.add(link.end2)
    if (destMap.containsKey(root))
      for (link <- destMap.get(root).asScala)
        if (!link.getBreed.isDirected && link.isTied)
          myTies.add(link.end1)
    myTies
  }

  /// single lookups

  def findLink(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link =
    if (breed.isDirected)
      findLinkFrom(src, dest, breed, includeAllLinks)
    else
      findLinkEitherWay(src, dest, breed, includeAllLinks)

  def findLinkFrom(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link = {
    if (src == null || dest == null)
      null
    else {
      var link =
        world.links.getAgent(new DummyLink(world, src, dest, breed))
          .asInstanceOf[Link]
      if (link == null && includeAllLinks && (breed eq world.links))
        for (breedName <- world.program.linkBreeds.keys) {
          val agents = world.linkBreedAgents.get(breedName)
          link = world.links.getAgent(new DummyLink(world, src, dest, agents)).asInstanceOf[Link]
          if (link != null)
            return link
        }
      link
    }
  }

  def findLinkEitherWay(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link = {
    val link = findLinkFrom(src, dest, breed, includeAllLinks)
    if (link != null)
      link
    else
      findLinkFrom(dest, src, breed, includeAllLinks)
  }

  /// plural lookups

  def findLinksFrom(src: Turtle, breed: AgentSet): AgentSet = {
    val fromList = srcMap.get(src)
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    val isAllLinks = breed eq world.links
    if (fromList != null)
      for (link <- fromList.asScala)
        if (isAllLinks || (link.getBreed eq breed))
          builder.add(link)
    builder.build()
  }

  def findLinksTo(target: Turtle, breed: AgentSet): AgentSet = {
    val fromList = destMap.get(target)
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    val isAllLinks = breed eq world.links
    if (fromList != null)
      for (link <- fromList.asScala)
        if (isAllLinks || (link.getBreed eq breed))
          builder.add(link)
    builder.build()
  }

  def findLinksWith(target: Turtle, breed: AgentSet): AgentSet = {
    val fromList = destMap.get(target)
    val toList = srcMap.get(target)
    val totalList = new java.util.ArrayList[Link]
    if (fromList != null)
      totalList.addAll(fromList)
    if (toList != null)
      totalList.addAll(toList)
    val isAllLinks = breed eq world.links
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    for (link <- totalList.asScala)
      if (isAllLinks || (link.getBreed eq breed))
        builder.add(link)
    builder.build()
  }

}
