// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import scala.collection.JavaConverters._
import scala.collection.mutable
import java.util.IdentityHashMap

import scala.collection.mutable.ArrayBuffer

// It would be nice to move this to the api package, but it would take a lot of refactoring to make
// all of the argument types and return types be the api types. - ST 4/11/13

trait LinkManager {

  def reset()

  def cleanupTurtle(turtle: Turtle)
  def cleanupLink(link: Link)

  def checkBreededCompatibility(unbreeded: Boolean): Boolean

  def createLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Link

  /**
    * Gets a specific link for the given turtles and link breed.
    * Note that unlike the other methods in this class, if `world.links` is
    * given as the breed, *only* an unbreeded link can be found. Thus, there
    * can only be a single matching link. If the breed is undirected, `src`
    * and `dest` are interchangeable.
    */
  def getLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Option[Link]

  /**
    * Gets all links that connect the two turtles with the given link breed.
    * If `world.links` is given as the breed, *all* links connecting the two
    * turtles will be returned, regardless of breed. This method ignores
    * the directedness and direction of links. Because link direction is
    * ignored, `src` and `dest` are interchangeable.
    */
  def linksWith(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link]

  /**
    * Gets all links going from `src` to `dest` of the given breed.
    * Thus, directed, outgoing links from `src` to `dest` as well as
    * undirected links connecting them will be included. If `world.links` is
    * given as the breed, *all* such links will be returned, regardless of
    * breed. Note that no `linksFrom` is provided as you can simply reverse
    * the arguments to this method.
    */
  def linksTo(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link]

  def outNeighbors(src: Turtle, linkBreed: AgentSet): Array[Turtle]
  def inNeighbors(target: Turtle, linkBreed: AgentSet): Array[Turtle]
  def neighbors(target: Turtle, linkBreed: AgentSet): Array[Turtle]

  def outLinks(src: Turtle, linkBreed: AgentSet): Array[Link]
  def inLinks(target: Turtle, linkBreed: AgentSet): Array[Link]
  def links(target: Turtle, linkBreed: AgentSet): Array[Link]

}

//
// About the data structures used here:
//
// Note first that LinkManager isn't actually the main place links are stored.  That would be
// World.links, which is a TreeAgentSet, which uses a TreeMap to support looking up a link
// efficiently (in O(log n) time) if both of the endpoints are known.  See the calls to
// `world.links.getAgent` below; those calls involve passing a DummyLink which we have filled
// in with a description of the actual link we're trying to find.  (This works because
// DummyLink extends Link, and Link has an appropriate implementation of compareTo().)
//
// To support efficient lookups when only one endpoint is known, `LinkMap`s are used (one for
// each breed, and one that includes ALL links). LinkMap contains a number of HashMaps that
// allow for fast lookup of links connected to turtles. These maps contain no additional
// information beyond what's in world.links, they just provide a faster means of looking up
// that information.
//
// LinkManager is responsible for keeping its LinkMaps up to date with world.links.
//


class LinkMap {

  // These should NEVER be iterated over if the order of iteration is
  // important. It will result in non-determinism in simulations.
  // -- BCH 10/22/2016
  private val undirected = mutable.Map[Turtle, ArrayBuffer[Link]]()
  private val outgoing = mutable.Map[Turtle, ArrayBuffer[Link]]()
  private val incoming = mutable.Map[Turtle, ArrayBuffer[Link]]()

  def addLink(link: Link) = {
    if (link.isDirectedLink) {
      outgoing.getOrElseUpdate(link.end1, ArrayBuffer.empty[Link]) += link
      incoming.getOrElseUpdate(link.end2, ArrayBuffer.empty[Link]) += link
    } else {
      undirected.getOrElseUpdate(link.end1, ArrayBuffer.empty[Link]) += link
      undirected.getOrElseUpdate(link.end2, ArrayBuffer.empty[Link]) += link
    }
  }

  def removeLink(link: Link) = {
    if (link.isDirectedLink) {
      outgoing(link.end1) -= link
      incoming(link.end2) -= link
    } else {
      undirected(link.end1) -= link
      undirected(link.end2) -= link
    }
  }

  /**
    * Returns all links connected to the given turtle that match one of the
    * given includes as an array.
    * @return The array containing all matching links. This array is a *copy*
    *         and may be safely mutated by the caller. An array is returned
    *         for performance reasons, as well as for convenient consumption
    *         by Java and convenient (and fast) conversion to an AgentSet.
    *
    */
  def links(turtle: Turtle, includeUndirected: Boolean = false,
                            includeOutgoing:   Boolean = false,
                            includeIncoming:   Boolean = false): Array[Link] = {
    var size = 0

    val un = if (includeUndirected && undirected.nonEmpty)
      undirected.get(turtle).map { ls => size += ls.size; ls }
    else None

    val out = if (includeOutgoing && outgoing.nonEmpty)
      outgoing.get(turtle).map { ls => size += ls.size; ls }
    else None

    val in = if (includeIncoming && incoming.nonEmpty)
      incoming.get(turtle).map { ls => size += ls.size; ls }
    else None

    val result = new Array[Link](size)
    var i = 0

    un.foreach { ls => ls.copyToArray(result, i); i += ls.size }
    out.foreach { ls => ls.copyToArray(result, i); i += ls.size }
    in.foreach { ls => ls.copyToArray(result, i); i += ls.size }

    result
  }

  /**
   * Removes the turtle from the maps. Note that this only removes that
   * the given turtle's entries from the maps. It does NOT remove the turtle's
   * links that appear in other turtles' entries.
   */
  def removeTurtle(turtle: Turtle) = {
    undirected -= turtle
    outgoing -= turtle
    incoming -= turtle
  }

  def clear() = {
    undirected.clear
    outgoing.clear
    incoming.clear
  }

}


class LinkManagerImpl(world: World, linkFactory: LinkFactory) extends LinkManager {

  private val allLinks: LinkMap = new LinkMap()
  // We use an IdentityHashMap here as breedsets are ALWAYS canonicalized via
  // the world. To be fair, lots of other code depends on that (anywhere you
  // see an `eq` check on an agentset).
  // Note this should never be iterated over where order matters! The iteration
  // order of the breeds is non-deterministic here and will result in
  // non-reproducible runs.
  // -- BCH 10/18/2016
  private val breededLinks = new IdentityHashMap[AgentSet, LinkMap].asScala

  private var unbreededLinkCount = 0

  def reset() {
    allLinks.clear()
    breededLinks.clear()
    world.tieManager.reset()
    unbreededLinkCount = 0
    world.links.clearDirected()
  }

  def checkBreededCompatibility(unbreeded: Boolean): Boolean = {
    val it = world.links.iterator
    !it.hasNext || unbreeded == (it.next().asInstanceOf[Link].getBreed eq world.links)
  }

  def createLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Link = {
    val link = linkFactory(world, src, dest, linkBreed)
    bless(link)
    link
  }

  private def bless(link: Link) {
    allLinks.addLink(link)
    if (link.getBreed eq world.links)
      unbreededLinkCount += 1
    else
      breededLinks.getOrElseUpdate(link.getBreed, new LinkMap()).addLink(link)
  }

  def cleanupLink(link: Link) {
    // keep tie bookkeeping up to date
    link.untie()
    allLinks.removeLink(link)
    if (link.getBreed eq world.links) {
      unbreededLinkCount -= 1
      // were we the last link?
      if (unbreededLinkCount == 0)
        world.links.clearDirected()
    } else {
      breededLinks(link.getBreed).removeLink(link)
    }
  }

  def cleanupTurtle(turtle: Turtle) {
    links(turtle, world.links).foreach(_.die())
    allLinks.removeTurtle(turtle)
    breededLinks.valuesIterator.foreach(_.removeTurtle(turtle))
  }

  private def linkMapsForBreed(linkBreed: AgentSet): LinkMap =
    if (linkBreed eq world.links) allLinks else breededLinks.getOrElseUpdate(linkBreed, new LinkMap())

  def outNeighbors(src: Turtle, linkBreed: AgentSet): Array[Turtle] =
    otherEnds(src, outLinks(src, linkBreed), linkBreed)

  def inNeighbors(target: Turtle, linkBreed: AgentSet): Array[Turtle] =
    otherEnds(target, inLinks(target, linkBreed), linkBreed)

  def neighbors(turtle: Turtle, linkBreed: AgentSet): Array[Turtle] =
    otherEnds(turtle, links(turtle, linkBreed), linkBreed)

  def getLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Option[Link] = {
    if (src == null || dest == null)
      return None
    var link = world.links.getAgent(new DummyLink(world, src, dest, linkBreed))
    if (link == null && linkBreed.isUndirected)
      link = world.links.getAgent(new DummyLink(world, dest, src, linkBreed))
    Option(link).asInstanceOf[Option[Link]]
  }

  def linksWith(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link] =
    links(src, linkBreed).filter(l => otherEnd(src, l) == dest)

  def linksTo(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link] =
    outLinks(src, linkBreed).filter(l => otherEnd(src, l) == dest)

  def outLinks(src: Turtle, linkBreed: AgentSet): Array[Link] = {
    val linkMaps = linkMapsForBreed(linkBreed)
    linkMaps.links(src, includeUndirected = true, includeOutgoing = true)
  }

  def inLinks(target: Turtle, linkBreed: AgentSet): Array[Link] = {
    val linkMaps = linkMapsForBreed(linkBreed)
    linkMaps.links(target, includeUndirected = true, includeIncoming = true)
  }

  def links(turtle: Turtle, linkBreed: AgentSet): Array[Link] = {
    val linkMaps = linkMapsForBreed(linkBreed)
    linkMaps.links(turtle, includeUndirected = true, includeOutgoing = true, includeIncoming = true)
  }

  def otherEnd(turtle: Turtle, link: Link): Turtle =
    if (link.end1 == turtle) link.end2 else link.end1

  def otherEnds(turtle: Turtle, links: Array[Link], linkBreed: AgentSet): Array[Turtle] = {
    val result = new Array[Turtle](links.length)
    var i = 0
    while (i < links.length) {
      result(i) = otherEnd(turtle, links(i))
      i += 1
    }
    if ((linkBreed eq world.links) && (breededLinks.size > 1))
      result.distinct
    else
      result
  }
}
