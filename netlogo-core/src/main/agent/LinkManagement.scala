// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ AgentKind, Program, Shape, ShapeListTracker }

import java.util.{ HashMap => JHashMap, Map => JMap }
import scala.collection.JavaConverters._

import World.NegativeOneInt

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

  var linkBreeds: JMap[String, TreeAgentSet] = new JHashMap[String, TreeAgentSet]()
  def linkShapeList = linkShapes.shapeList

  def linksOwnIndexOf(name: String): Int = program.linksOwn.indexOf(name)
  def linksOwnNameAt(index: Int): String = program.linksOwn(index)
  def getLinkBreed(breedName: String): AgentSet = linkBreeds.get(breedName)
  def isLinkBreed(breed: AgentSet): Boolean =
    program.linkBreeds.isDefinedAt(breed.printName)
  def linkBreedOwns(breed: AgentSet, name: String): Boolean =
    breed != links && linkBreedsOwnIndexOf(breed, name) != -1
  def linkBreedsOwnNameAt(breed: AgentSet, index: Int): String =
    program.linkBreeds(breed.printName).owns(index - program.linksOwn.size)
  def linkBreedsOwnIndexOf(breed: AgentSet, name: String): Int = {
    if (breed == null) throw new IllegalArgumentException("invalid breed")
    breedsOwnCache.getOrDefault(breed.printName + "~" + name, NegativeOneInt).intValue
  }
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
  def compareLinkBreeds(breed1: AgentSet, breed2: AgentSet): Int =
    linkBreeds.values.iterator.asScala.collectFirst {
      case b if b eq breed1 => -1
      case b if b eq breed2 => 1
    }.getOrElse {
      throw new IllegalStateException("neither of the breeds exist, that's bad");
    }
}
