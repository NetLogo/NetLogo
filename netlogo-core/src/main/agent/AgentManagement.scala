// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  java.{ lang, util },
    lang.{ Double => JDouble },
    util.{ HashMap => JHashMap, Map => JMap }

import
  org.nlogo.{ core, api },
    core.{ AgentKind, Nobody, Program,
      ShapeListTracker, WorldDimensions },
    api.{ AgentException, AgentVariableNumbers, Color }

trait AgentManagement
  extends TurtleManagement
  with LinkManagement
  with ObserverManagement
  with WorldKernel { this: CoreWorld with GrossWorldState =>

  def program: Program
  def patches: IndexedAgentSet
  def turtles: TreeAgentSet
  def links: TreeAgentSet

  val noTurtles: AgentSet = AgentSet.emptyTurtleSet
  val noPatches: AgentSet = AgentSet.emptyPatchSet
  val noLinks:   AgentSet = AgentSet.emptyLinkSet

  def patchChangedColorAt(patchId: Int, color: Double): Unit

  def patchesOwnNameAt(index: Int): String = program.patchesOwn(index)
  def patchesOwnIndexOf(name: String): Int = program.patchesOwn.indexOf(name)
  def observerOwnsNameAt(index: Int): String = observer.variableName(index)
  def observerOwnsIndexOf(name: String): Int = observer.variableIndex(name)

  protected var breedsOwnCache: JHashMap[String, Integer] = new JHashMap[String, Integer]()

  def createPatches(minPx: Int, maxPx: Int, minPy: Int, maxPy: Int)
  @throws(classOf[AgentException])
  def getPatchAt(x: Double, y: Double): Patch
  def fastGetPatchAt(xc: Int, yc: Int): Patch
  def getOrCreateTurtle(id: Long): Turtle

  def createPatches(dim: WorldDimensions): Unit = {
    createPatches(dim.minPxcor, dim.maxPxcor, dim.minPycor, dim.maxPycor)
  }

  def getVariablesArraySize(patch: Patch): Int = program.patchesOwn.size

  def indexOfVariable(agentKind: AgentKind, name: String): Int = {
    if (agentKind == AgentKind.Observer)
      observerOwnsIndexOf(name)
    else if (agentKind == AgentKind.Turtle)
      turtlesOwnIndexOf(name)
    else if (agentKind == AgentKind.Link)
      linksOwnIndexOf(name)
    else
      patchesOwnIndexOf(name)
  }

  def getPatch(id: Int): Patch =
    _patches.getByIndex(id).asInstanceOf[Patch]

  def agentSetOfKind(agentKind: AgentKind): AgentSet = {
    agentKind match {
      case AgentKind.Turtle => _turtles
      case AgentKind.Patch => _patches
      case AgentKind.Observer => observers
      case AgentKind.Link => _links
    }
  }

  abstract override def clearAll(): Unit = {
    super.clearAll()
    clearPatches()
    clearGlobals()
  }

  def getOrCreateLink(end1: JDouble, end2: JDouble, breed: AgentSet): Link =
    getOrCreateLink(
      getOrCreateTurtle(end1.longValue),
      getOrCreateTurtle(end2.longValue), breed)

  def getOrCreateLink(end1: Turtle, end2: Turtle, breed: AgentSet): Link = {
    val link = getLink(end1.agentKey, end2.agentKey, breed)
    if (link == null) linkManager.createLink(end1, end2, breed)
    else              link
  }

  def getOrCreateDummyLink(end1: Object, end2: Object, breed: AgentSet): Link = {
    val linkOption =
      if (end1 == Nobody || end2 == Nobody) None
      else Option(getLink(
        end1.asInstanceOf[Turtle].agentKey,
        end2.asInstanceOf[Turtle].agentKey, breed))

    linkOption.getOrElse(
      linkManager.dummyLink(end1, end2, breed) )
  }

  def indexOfVariable(agent: Agent, name: String): Int = {
    agent match {
      case observer: Observer => observerOwnsIndexOf(name)
      case turtle: Turtle =>
        val breed = turtle.getBreed
        if (breed == _turtles) turtlesOwnIndexOf(name)
        else {
          val breedIndexOf = breedsOwnIndexOf(breed, name)
          if (breedIndexOf != -1) breedIndexOf
          else                    turtlesOwnIndexOf(name)
        }
      case link: Link =>
        val breed = link.getBreed
        if (breed == _links) linksOwnIndexOf(name)
        else {
          val breedIndexOf = linkBreedsOwnIndexOf(breed, name)
          if (breedIndexOf != -1) breedIndexOf
          else                    linksOwnIndexOf(name)
        }
      case _ => patchesOwnIndexOf(name)
    }
  }

  def clearPatches(): Unit = {
    val iter = patches.iterator
    while(iter.hasNext) {
      val patch = iter.next().asInstanceOf[Patch]
      patch.pcolorDoubleUnchecked(Color.BoxedBlack)
      patch.label("")
      patch.labelColor(Color.BoxedWhite)
      patch.clearProgramVariables()
    }
    patchesAllBlack(true)
  }

  def setUpShapes(clearOld: Boolean): Unit = {
    turtleBreedShapes.setUpBreedShapes(clearOld, breeds)
    linkBreedShapes.setUpBreedShapes(clearOld, linkBreeds)
  }

  // copyAgents is a "deep copy" of the world intended to capture the relevant features
  // of world for displaying it to the user.
  def copyAgents(other: AgentManagement, newWorld: World): Unit = {
    import scala.collection.JavaConverters._

    // begin by copying breeds
    breeds.asScala.foreach {
      case (name, set) =>
        other.breeds.put(name, new TreeAgentSet(AgentKind.Turtle, set.printName))
    }
    linkBreeds.asScala.foreach {
      case (name, set) =>
        val agentset = new TreeAgentSet(AgentKind.Link, set.printName)
        other.linkBreeds.put(name, agentset)
        agentset.setDirected(set.isDirected)
    }

    // then, copy patches
    val patchIter = patches.iterator
    val newPatchArray = new Array[Agent](worldHeight * worldWidth)
    var i: Int = 0
    while (patchIter.hasNext) {
      val patch = patchIter.next.asInstanceOf[Patch]
      val newPatch = new Patch(newWorld, patch.id.toInt, patch.pxcor, patch.pycor, patch.variables.length)
      System.arraycopy(patch.variables, 0, newPatch.variables, 0, patch.variables.length)
      newPatchArray(i) = newPatch
      i += 1
    }
    newWorld._patches = new ArrayAgentSet(AgentKind.Patch, "patches", newPatchArray)

    def copyBreedShapes(
      breedSets:      JMap[String, TreeAgentSet],
      newShapes:      World => ShapeListTracker,
      shapeTracker:   ShapeListTracker,
      newBreedShapes: World => BreedShapes,
      breedShapes:    BreedShapes): Unit = {
      newShapes(newWorld).addAll(shapeTracker.shapeList.shapes)
      breedSets.asScala.foreach {
        case (name, agentset) if breedShapes.breedHasShape(agentset) =>
          newBreedShapes(newWorld).setBreedShape(agentset, breedShapes.breedShape(agentset))
        case _ =>
      }
    }
    copyBreedShapes(breeds,     _.turtleShapes, turtleShapes, _.turtleBreedShapes, turtleBreedShapes)
    copyBreedShapes(linkBreeds, _.linkShapes,   linkShapes,   _.linkBreedShapes,   linkBreedShapes)

    // then we recreate all the agents, setting their breed to the breed in the new world.
    // this also causes the agent to be added to its breed.
    val turtleIter = turtles.iterator
    while (turtleIter.hasNext) {
      val turtle = turtleIter.next().asInstanceOf[Turtle2D]
      val newTurtle = new Turtle2D(newWorld, turtle.id)
      System.arraycopy(turtle.variables, 0, newTurtle.variables, 0, 7)
      System.arraycopy(turtle.variables, 9, newTurtle.variables, 9, 4)
      newTurtle.xcor = turtle.xcor
      newTurtle.ycor = turtle.ycor
      newTurtle.heading = turtle.heading
      if (turtle.getBreed != turtles) {
        val newBreed = other.breeds.get(turtle.getBreed.printName)
        newTurtle.setBreed(newBreed)
        newTurtle.shape(turtle.shape)
      }
      val newPatchHere = newWorld.getPatch(turtle.getPatchHere.id.toInt)
      newPatchHere.addTurtle(newTurtle)
    }

    val linkIter = links.iterator
    while (linkIter.hasNext) {
      val link = linkIter.next().asInstanceOf[Link]
      // find corresponding turtles
      val end1 = newWorld.getTurtle(link.end1.id)
      val end2 = newWorld.getTurtle(link.end2.id)
      val newBreed =
        if (link.getBreed == links) other.links
        else                        linkBreeds.get(link.getBreed.printName)
      val newLink = other.linkManager.createLink(end1, end2, newBreed)
      System.arraycopy(
        link.variables,    AgentVariableNumbers.VAR_LCOLOR,
        newLink.variables, AgentVariableNumbers.VAR_LCOLOR,
        AgentVariableNumbers.VAR_LBREED - AgentVariableNumbers.VAR_LCOLOR)
      System.arraycopy(
        link.variables,    AgentVariableNumbers.VAR_THICKNESS,
        newLink.variables, AgentVariableNumbers.VAR_THICKNESS,
        AgentVariableNumbers.VAR_TIEMODE - AgentVariableNumbers.VAR_LBREED)
    }
  }
}
