// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api

class LinkManagerImpl(val world: World, linkFactory: LinkFactory) extends LinkManager {

  import collection.mutable.ArrayBuffer

  // Use LinkedHashMap not HashMap for these so model results are
  // reproducible. - ST 12/21/05, 3/15/06, 7/20/07
  private val srcMap  = new collection.mutable.LinkedHashMap[Turtle, ArrayBuffer[Link]]
  private val destMap = new collection.mutable.LinkedHashMap[Turtle, ArrayBuffer[Link]]

  ///

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link = {
    val link = linkFactory(world, src, dest, breed)
    link.colorDoubleUnchecked(Link.DEFAULT_COLOR)
    bless(link)
    link
  }

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
    if (srcMap.contains(end1))
      srcMap(end1) += link
    else
      srcMap += end1 -> ArrayBuffer(link)
    // add to destination map
    if (destMap.contains(end2))
      destMap(end2) += link
    else
      destMap += end2 -> ArrayBuffer(link)
    if (link.getBreed eq world.links)
      countUnbreededLinks += 1
  }

  private[agent] def cleanup(link: Link) {
    // keep tie bookkeeping up to date
    link.untie()
    // remove from source map
    val end1 = link.end1
    for (list <- srcMap.get(end1)) {
      list -= link
      if (list.isEmpty)
        srcMap -= end1
    }
    // remove from dest map
    val end2 = link.end2
    for (list <- destMap.get(end2)) {
      list -= link
      if (list.isEmpty)
        destMap -= end2
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
    for (links <- srcMap.get(turtle)) {
      srcMap -= turtle
      for (link <- links)
        link.die()
    }
    for (links <- destMap.get(turtle)) {
      destMap -= turtle
      for (link <- links)
        link.die()
    }
  }

  ///

  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): AgentSet =
    srcMap.get(src) match {
      case None =>
        world.noTurtles
      case Some(list) =>
        val builder = new AgentSetBuilder(api.AgentKind.Turtle, list.size)
        addLinkNeighborsFrom(builder, list, sourceSet, true)
        builder.build()
    }

  def findLinkedTo(target: Turtle, sourceSet: AgentSet): AgentSet =
    destMap.get(target) match {
      case None =>
        world.noTurtles
      case Some(list) =>
        val builder = new AgentSetBuilder(api.AgentKind.Turtle, list.size)
        addLinkNeighborsTo(builder, list, sourceSet, true)
        builder.build()
    }

  def findLinkedWith(target: Turtle, sourceSet: AgentSet): AgentSet = {
    val toList = destMap.get(target)
    val fromList = srcMap.get(target)
    val size =
      fromList.map(_.size).getOrElse(0) +
      toList.map(_.size).getOrElse(0)
    if (size == 0)
      world.noTurtles
    else {
      val builder = new AgentSetBuilder(api.AgentKind.Turtle, size)
      for (l <- toList)
        addLinkNeighborsTo(builder, l, sourceSet, false)
      for (l <- fromList)
        addLinkNeighborsFrom(builder, l, sourceSet, false)
      builder.build()
    }
  }

  // the next two methods are essentially the same but are separate for
  // performance reasons ev 4/26/07
  // these are used in two cases, either for link-neighbors in which case
  // sourceSet will always be a breed. but layout-radial also uses it
  // and it might be any agentset.  ev 4/6/07
  private def addLinkNeighborsFrom(builder: AgentSetBuilder, links: Iterable[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links)
      if ((!isBreed && sourceSet.contains(link)) || (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !builder.contains(link.end1)))) || (link.getBreed eq sourceSet))
        builder.add(link.end2)
  }

  private def addLinkNeighborsTo(builder: AgentSetBuilder, links: Iterable[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    // if we have unbreeded links we know that there is only one possible link
    // between two turtles, thus we don't have to check if the end point is already
    // in the nodeset, which is slow. so only models that use breeds && link-neighbors
    // will take a performance hit ev 6/15/07
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links)
      if ((!isBreed && sourceSet.contains(link)) || (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !builder.contains(link.end1)))) || (link.getBreed eq sourceSet))
        builder.add(link.end1)
  }

  /// tie

  def tiedTurtles(root: Turtle): java.util.List[Turtle] = {
    val myTies = new java.util.ArrayList[Turtle]
    for { list <- srcMap.get(root)
          link <- list }
      if (link.isTied)
        myTies.add(link.end2)
    for { list <- destMap.get(root)
          link <- list }
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
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    val isAllLinks = breed eq world.links
    for { list <- srcMap.get(src); link <- list }
      if (isAllLinks || (link.getBreed eq breed))
        builder.add(link)
    builder.build()
  }

  def findLinksTo(target: Turtle, breed: AgentSet): AgentSet = {
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    val isAllLinks = breed eq world.links
    for { list <- destMap.get(target); link <- list }
      if (isAllLinks || (link.getBreed eq breed))
        builder.add(link)
    builder.build()
  }

  def findLinksWith(target: Turtle, breed: AgentSet): AgentSet = {
    val builder = new AgentSetBuilder(api.AgentKind.Link)
    val isAllLinks = breed eq world.links
    for { list <- destMap.get(target); link <- list }
      if (isAllLinks || (link.getBreed eq breed))
        builder.add(link)
    for { list <- srcMap.get(target); link <- list }
      if (isAllLinks || (link.getBreed eq breed))
        builder.add(link)
    builder.build()
  }

}
