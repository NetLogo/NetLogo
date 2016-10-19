// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import scala.collection.JavaConverters._
import scala.collection.mutable
import java.util.IdentityHashMap

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

  def findLinkedFrom(src: Turtle, breed: AgentSet): Seq[Turtle]
  def findLinkedTo(target: Turtle, breed: AgentSet): Seq[Turtle]
  def findLinkedWith(target: Turtle, breed: AgentSet): Seq[Turtle]

  def findLinksFrom(src: Turtle, breed: AgentSet): Seq[Link]
  def findLinksTo(target: Turtle, breed: AgentSet): Seq[Link]
  def findLinksWith(target: Turtle, breed: AgentSet): Seq[Link]

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
// To support efficient lookups when only one endpoint is known, LinkManager contains two
// LinkedHashMaps, srcMap and destMap.  These maps are technically redundant, in the sense that they
// don't contain any unique information of their own.  srcMap supports efficiently answering the
// question, "what outgoing links exist from a turtle?", and destMap, the same but for incoming.
// Undirected links count as both ongoing and incoming.
//
// LinkManager is responsible for keeping srcMap and destMap in sync with the contents of
// World.links.
//

import collection.mutable.Buffer

class LinkMap extends mutable.LinkedHashMap[Turtle, List[Link]] {
  override def default(key: Turtle) = List.empty[Link]
}

case class LinkMaps(undirected: LinkMap = new LinkMap,
                    outgoing:   LinkMap = new LinkMap,
                    incoming:   LinkMap = new LinkMap) {

  def addLink(link: Link) = {
    if (link.isDirectedLink) {
      outgoing(link.end1) = link +: outgoing(link.end1)
      incoming(link.end2) = link +: incoming(link.end2)
    } else {
      undirected(link.end1) = link +: undirected(link.end1)
      undirected(link.end2) = link +: undirected(link.end2)
    }
  }

  def removeLink(link: Link) = {
    if (link.isDirectedLink) {
      outgoing(link.end1) = outgoing(link.end1).filterNot(_ == link)
      incoming(link.end2) = incoming(link.end2).filterNot(_ == link)
    } else {
      undirected(link.end1) = undirected(link.end1).filterNot(_ == link)
      undirected(link.end2) = undirected(link.end2).filterNot(_ == link)
    }
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

  private val allLinks: LinkMaps = LinkMaps()
  // We use an IdentityHashMap here as breedsets are ALWAYS canonicalized via
  // the world. To be fair, lots of other code depends on that (anywhere you
  // see an `eq` check on an agentset).
  // Note this should never be iterated over where order matters! The iteration
  // order of the breeds is non-deterministic here and will result in
  // non-reproducible runs.
  // -- BCH 10/18/2016
  private val breededLinks = new IdentityHashMap[AgentSet, LinkMaps].asScala

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

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link = {
    val link = linkFactory(world, src, dest, breed)
    bless(link)
    link
  }

  private def bless(link: Link) {
    val end1 = link.end1
    val end2 = link.end2

    allLinks.addLink(link)
    if (link.getBreed eq world.links)
      unbreededLinkCount += 1
    else
      breededLinks.getOrElseUpdate(link.getBreed, LinkMaps()).addLink(link)
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
    allLinks.undirected(turtle).foreach(_.die())
    allLinks.outgoing(turtle).foreach(_.die())
    allLinks.incoming(turtle).foreach(_.die())
    allLinks.removeTurtle(turtle)
    breededLinks.valuesIterator.foreach(_.removeTurtle(turtle))
  }

  private def linkMapsForBreed(breed: AgentSet): LinkMaps =
    if (breed eq world.links) allLinks else breededLinks.getOrElseUpdate(breed, LinkMaps())

  def findLinkedFrom(src: Turtle, breed: AgentSet): Seq[Turtle] =
    neighbors(src, findLinksFrom(src, breed), breed)

  def findLinkedTo(target: Turtle, breed: AgentSet): Seq[Turtle] =
    neighbors(target, findLinksTo(target, breed), breed)

  def findLinkedWith(turtle: Turtle, breed: AgentSet): Seq[Turtle] =
    neighbors(turtle, findLinksWith(turtle, breed), breed)

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
        world.getLinkBreeds.asScala.foreach { breedNamePair =>
          link = world.links.getAgent(new DummyLink(world, src, dest, breedNamePair._2)).asInstanceOf[Link]
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

  def findLinksFrom(src: Turtle, breed: AgentSet): Seq[Link] = {
    val linkMaps = linkMapsForBreed(breed)
    linkMaps.outgoing(src) ::: linkMaps.undirected(src)
  }

  def findLinksTo(target: Turtle, breed: AgentSet): Seq[Link] = {
    val linkMaps = linkMapsForBreed(breed)
    linkMaps.incoming(target) ::: linkMaps.undirected(target)
  }

  def findLinksWith(turtle: Turtle, breed: AgentSet): Seq[Link] = {
    val linkMaps = linkMapsForBreed(breed)
    linkMaps.undirected(turtle) ::: linkMaps.outgoing(turtle) ::: linkMaps.incoming(turtle)
  }

  def otherEnd(turtle: Turtle, link: Link): Turtle =
    if (link.end1 == turtle) link.end2 else link.end1

  def neighbors(turtle: Turtle, links: Seq[Link], breed: AgentSet): Seq[Turtle] = {
    val result = links.map(otherEnd(turtle, _))
    if ((breed eq world.links) && (breededLinks.size > 1))
      result.distinct
    else
      result
  }
}
