// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

object LinkManager {
  // checking of breed directedness. not sure where else to put this
  def mustNotBeDirected(breed: AgentSet): Option[String] =
    if (breed.isDirected)
      Some(breed.printName + " is a directed breed.")
    else
      None
  def mustNotBeUndirected(breed: AgentSet): Option[String] =
    if (breed.isUndirected)
      Some(breed.printName + " is an undirected breed.")
    else
      None
}

// It would be nice to move this to the api package, but it would take a lot of refactoring to make
// all of the argument types and return types be the api types. - ST 4/11/13

trait LinkManager {

  def reset()

  def cleanupTurtle(turtle: Turtle)
  def cleanupLink(link: Link)

  def checkBreededCompatibility(unbreeded: Boolean): Boolean

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link

  def findLink(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkEitherWay(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkFrom(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link

  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): Iterator[Turtle]
  def findLinkedTo(target: Turtle, sourceSet: AgentSet): Iterator[Turtle]
  def findLinkedWith(target: Turtle, sourceSet: AgentSet): Iterator[Turtle]

  def findLinksFrom(src: Turtle, breed: AgentSet): Iterator[Link]
  def findLinksTo(target: Turtle, breed: AgentSet): Iterator[Link]
  def findLinksWith(target: Turtle, breed: AgentSet): Iterator[Link]

}

import collection.mutable.Buffer

class LinkManagerImpl(world: World, linkFactory: LinkFactory) extends LinkManager {

  // LinkedHashMap not HashMap, so results are reproducible
  private val srcMap  = new collection.mutable.LinkedHashMap[Turtle, Buffer[Link]]
  private val destMap = new collection.mutable.LinkedHashMap[Turtle, Buffer[Link]]

  private var unbreededLinkCount = 0

  def reset() {
    srcMap.clear()
    destMap.clear()
    world.tieManager.reset()
    unbreededLinkCount = 0
    world.links.clearDirected()
  }

  def checkBreededCompatibility(unbreeded: Boolean): Boolean = {
    val it = world.links.iterator
    !it.hasNext || unbreeded == (it.next().asInstanceOf[Link].getBreed eq world.links)
  }

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link = {
    val link = linkFactory(world, src, dest, breed)
    bless(link)
    link
  }

  private def bless(link: Link) {
    val end1 = link.end1
    val end2 = link.end2
    // add to source map
    if (srcMap.contains(end1))
      srcMap(end1) += link
    else
      srcMap += end1 -> Buffer(link)
    // add to destination map
    if (destMap.contains(end2))
      destMap(end2) += link
    else
      destMap += end2 -> Buffer(link)
    if (link.getBreed eq world.links)
      unbreededLinkCount += 1
  }

  def cleanupLink(link: Link) {
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
    if (link.getBreed eq world.links) {
      unbreededLinkCount -= 1
      // were we the last link?
      if (unbreededLinkCount == 0)
        world.links.clearDirected()
    }
  }

  def cleanupTurtle(turtle: Turtle) {
    // this part is a bit tricky -- we need to remove the turtle
    // from the src & dest maps first, so we don't end up in an
    // infinite loop where a dying node kills a link which tries
    // to kill the original node.  But we need the map entries
    // in order to find the links.  Hence the exact ordering
    // below. - ST 3/15/06
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

  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): Iterator[Turtle] = {
    val buf = Buffer[Turtle]()
    for (list <- srcMap.get(src))
      addLinkNeighborsFrom(buf, list, sourceSet, true)
    buf.iterator
  }

  def findLinkedTo(target: Turtle, sourceSet: AgentSet): Iterator[Turtle] = {
    val buf = Buffer[Turtle]()
    for (list <- destMap.get(target))
      addLinkNeighborsTo(buf, list, sourceSet, true)
    buf.iterator
  }

  def findLinkedWith(target: Turtle, sourceSet: AgentSet): Iterator[Turtle] = {
    val buf = Buffer[Turtle]()
    for (list <- destMap.get(target))
      addLinkNeighborsTo(buf, list, sourceSet, false)
    for (list <- srcMap.get(target))
      addLinkNeighborsFrom(buf, list, sourceSet, false)
    buf.iterator
  }

  // the next two methods are essentially the same but are separate for
  // performance reasons ev 4/26/07
  // these are used in two cases, either for link-neighbors in which case
  // sourceSet will always be a breed. but layout-radial also uses it
  // and it might be any agentset.  ev 4/6/07
  private def addLinkNeighborsFrom(buf: Buffer[Turtle], links: Iterable[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links)
      if ((!isBreed && sourceSet.contains(link)) ||
          (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !buf.contains(link.end1)))) ||
          (link.getBreed eq sourceSet))
        buf += link.end2
  }

  private def addLinkNeighborsTo(buf: Buffer[Turtle], links: Iterable[Link], sourceSet: AgentSet, directed: Boolean) {
    val isBreed = sourceSet.printName != null
    val isAllLinks = sourceSet eq world.links
    // if we have unbreeded links we know that there is only one possible link
    // between two turtles, thus we don't have to check if the end point is already
    // in the nodeset, which is slow. so only models that use breeds && link-neighbors
    // will take a performance hit ev 6/15/07
    val unbreededLinks = checkBreededCompatibility(true)
    for (link <- links)
      if ((!isBreed && sourceSet.contains(link)) ||
          (isAllLinks && (unbreededLinks || (directed == link.getBreed.isDirected && !buf.contains(link.end1)))) ||
          (link.getBreed eq sourceSet))
        buf += link.end1
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

  def findLinksFrom(src: Turtle, breed: AgentSet): Iterator[Link] = {
    val isAllLinks = breed eq world.links
    srcMap.getOrElse(src, Nil)
      .iterator
      .filter(link => isAllLinks || (link.getBreed eq breed))
  }

  def findLinksTo(target: Turtle, breed: AgentSet): Iterator[Link] = {
    val isAllLinks = breed eq world.links
    destMap.getOrElse(target, Nil)
      .iterator
      .filter(link => isAllLinks || (link.getBreed eq breed))
  }

  def findLinksWith(target: Turtle, breed: AgentSet): Iterator[Link] =
    findLinksTo(target, breed) ++ findLinksFrom(target, breed)

}
