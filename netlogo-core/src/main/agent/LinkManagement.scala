// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ AgentKind, Program, Shape, ShapeList, ShapeListTracker }

import java.lang.{ Double => JDouble }

import java.util.{ HashMap => JHashMap, Map => JMap }

import World.{ NegativeOneInt, Zero }

trait LinkManagement extends WorldKernel {
  def program: Program
  def linkManager: LinkManager
  def turtles: TreeAgentSet
  protected def breedsOwnCache: JHashMap[String, Integer]

  val linkShapes = new ShapeListTracker(AgentKind.Link)
  val linkBreedShapes = new BreedShapes("LINKS", linkShapes)

  // we assign an unique ID to links, like turtles, except that
  // it's not visible to anyone and it can't affect the outcome of
  // the model. I added it because it greatly complicates hubnet
  // view mirroring to have the only unique identifier be a
  // 3 element list. ev 5/1/08
  private var _nextLinkIndex: Long = 0

  private[agent] var linkBreeds: JMap[String, TreeAgentSet] = new JHashMap[String, TreeAgentSet]()
  def linkShapeList = linkShapes.shapeList

  def linksOwnIndexOf(name: String): Int = program.linksOwn.indexOf(name)
  def linksOwnNameAt(index: Int): String = program.linksOwn(index)
  def linkBreedAgents: JMap[String, TreeAgentSet] = linkBreeds
  def getLinkBreed(breedName: String): AgentSet = linkBreeds.get(breedName)
  def isLinkBreed(breed: AgentSet): Boolean =
    program.linkBreeds.isDefinedAt(breed.printName)
  def linkBreedOwns(breed: AgentSet, name: String): Boolean =
    breed != links && linkBreedsOwnIndexOf(breed, name) != -1
  def linkBreedsOwnNameAt(breed: AgentSet, index: Int): String =
    program.linkBreeds(breed.printName).owns(index - program.linksOwn.size)
  def linkBreedsOwnIndexOf(breed: AgentSet, name: String): Int =
    breedsOwnCache.getOrDefault(breed.printName + "~" + name, NegativeOneInt).intValue
  def getLinkBreedSingular(breed: AgentSet): String =
    if (breed == links)
      "LINK"
    else
      program.linkBreeds.get(breed.printName).map(_.singular).getOrElse("LINK")

  def newLinkId(): Long = {
    val r = _nextLinkIndex
    _nextLinkIndex += 1
    r
  }

  // null indicates failure
  def checkLinkShapeName(name: String): String = {
    val lowName = name.toLowerCase();
    if (linkShapeList.exists(lowName)) lowName
    else                               null
  }

  def getLinkShape(name: String): Shape = {
    linkShapeList.shape(name)
  }

  abstract override def clearAll() {
    super.clearAll()
    clearLinks()
  }

  def clearLinks(): Unit = {
    linkManager.reset()
    if (program.linkBreeds.nonEmpty) {
      val breedIterator = linkBreeds.values.iterator
      while (breedIterator.hasNext) {
        breedIterator.next().asInstanceOf[TreeAgentSet].clear()
      }
    }
    val iter = links.iterator
    while (iter.hasNext) {
      iter.next().asInstanceOf[Link]._id = -1
    }
    links.clear()
    _nextLinkIndex = 0
  }

  def getLink(end1: Object, end2: Object, breed: AgentSet): Link = {
    linkManager.getLink(
      turtles.getAgent(end1).asInstanceOf[Turtle],
      turtles.getAgent(end2).asInstanceOf[Turtle], breed).orNull
  }

  def getVariablesArraySize(link: org.nlogo.api.Link, breed: org.nlogo.api.AgentSet): Int = {
    if (breed == links) {
      program.linksOwn.size
    } else {
      val breedOwns = program.linkBreeds(breed.printName).owns
      program.linksOwn.size + breedOwns.size
    }
  }

  def getLinkVariablesArraySize(breed: AgentSet): Int = {
    if (breed == links) {
      program.linksOwn.size
    } else {
      val breedOwns = program.linkBreeds(breed.printName).owns
      program.linksOwn.size + breedOwns.size
    }
  }

  // assumes caller has already checked to see if the breeds are equal
  def compareLinkBreeds(breed1: AgentSet, breed2: AgentSet): Int = {
    val iter = linkBreeds.values.iterator
    while (iter.hasNext) {
      val next = iter.next()
      if (next == breed1) {
        return -1
      } else {
        return 1
      }
    }

    throw new IllegalStateException("neither of the breeds exist, that's bad");
  }
}
