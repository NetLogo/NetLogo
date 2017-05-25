// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import scala.collection.JavaConverters._
import scala.collection.mutable
import java.util.IdentityHashMap

import scala.collection.mutable.ArrayBuffer

// It would be nice to move this to the api package, but it would take a lot of refactoring to make
// all of the argument types and return types be the api types. - ST 4/11/13

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

trait LinkManager {

  def reset()

  def cleanupTurtle(turtle: Turtle)
  def cleanupLink(link: Link)

  def checkBreededCompatibility(unbreeded: Boolean): Boolean

  def createLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Link

  def addLink(link: Link)
  def removeLink(link: Link)

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
  def neighbors(target: Turtle, linkSet: AgentSet): Array[Turtle]

  def outLinks(src: Turtle, linkBreed: AgentSet): Array[Link]
  def inLinks(target: Turtle, linkBreed: AgentSet): Array[Link]
  def links(target: Turtle, linkSet: AgentSet): Array[Link]

  def dummyLink(end1: Turtle, end2: Turtle, breed: AgentSet): DummyLink
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


class LinkManagerImpl[W <: World](world: W, linkFactory: LinkFactory[W]) extends LinkManager {

  private var unbreededLinkCount = 0

  def reset() {
    world.links.agents.forEach { case l: Link =>
      l.end1.removeLink(l)
      l.end2.removeLink(l)
    }
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
    addLink(link)
    link
  }

  def addLink(link: Link) = {
    if (link.getBreed eq world.links) {
      if (world.links.directed == Directedness.Undetermined) {
        world.links.setDirected(false)
      }
      unbreededLinkCount += 1
    }
    link.end1.addLink(link)
    link.end2.addLink(link)
  }

  def removeLink(link: Link) = {
    link.end1.removeLink(link)
    link.end2.removeLink(link)
    if (link.getBreed eq world.links) {
      unbreededLinkCount -= 1
      // were we the last link?
      if (unbreededLinkCount == 0)
        world.links.clearDirected()
    }
  }

  override def cleanupLink(link: Link): Unit = {
    link.untie()
    removeLink(link)
  }

  def cleanupTurtle(turtle: Turtle) = turtle.links.foreach(_.die())

  def outNeighbors(src: Turtle, linkBreed: AgentSet): Array[Turtle] =
    otherEnds(src, outLinks(src, linkBreed), linkBreed)

  def inNeighbors(target: Turtle, linkBreed: AgentSet): Array[Turtle] =
    otherEnds(target, inLinks(target, linkBreed), linkBreed)

  def neighbors(turtle: Turtle, linkSet: AgentSet): Array[Turtle] =
    otherEnds(turtle, links(turtle, linkSet), linkSet)

  def getLink(src: Turtle, dest: Turtle, linkBreed: AgentSet): Option[Link] = {
    if (src == null || dest == null)
      return None
    var link = world.links.getAgent(new DummyLink(world, src, dest, linkBreed))
    if (link == null && linkBreed.isUndirected)
      link = world.links.getAgent(new DummyLink(world, dest, src, linkBreed))
    Option(link).asInstanceOf[Option[Link]]
  }

  def linksWith(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link] =
    links(src, linkBreed).filter(_.otherEnd(src) == dest)
  def linksTo(src: Turtle, dest: Turtle, linkBreed: AgentSet): Array[Link] =
    outLinks(src, linkBreed).filter(_.otherEnd(src) == dest)

  def outLinks(src: Turtle, linkBreed: AgentSet): Array[Link] =
    src.selectLinks(true, false, linkBreed)

  def inLinks(target: Turtle, linkBreed: AgentSet): Array[Link] =
    target.selectLinks(false, true, linkBreed)

  def links(turtle: Turtle, linkSet: AgentSet): Array[Link] =
    turtle.selectLinks(true, true, linkSet)

  def otherEnds(turtle: Turtle, links: Array[Link], linkBreed: AgentSet): Array[Turtle] = {
    // Using an explicit while loop was found to have a significant performance
    // improvement over using `map` in the benchmarks.
    // -BCH 10/26/2016
    val result = new Array[Turtle](links.length)
    var i = 0
    while (i < links.length) {
      result(i) = links(i).otherEnd(turtle)
      i += 1
    }
    if ((linkBreed eq world.links) && (world.linkBreeds.size > 1)) result.distinct else result
  }

  def dummyLink(end1: Turtle, end2: Turtle, breed: AgentSet): DummyLink = {
    new DummyLink(world, end1, end2, breed)
  }
}
