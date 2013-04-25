// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.{ api, util }
import Mirrorables._
import Mirroring.State
import collection.JavaConverters._
import api.AgentVariableNumbers._

class FakeWorld(state: State) extends api.World {

  import MirrorableWorld._

  private val (worldVars, observerVars, patchStates, turtleStates, linkStates) = {
    // each group will be a seq of (agentId, vars):
    val groups = state.toSeq.groupBy(_._1.kind).map {
      case (kind, states) => kind -> states.map {
        case (AgentKey(_, agentId), vars) => agentId -> vars
      }
    }
    (groups(World).head._2, // world should always be there
      groups(Observer).head._2, // observer should always be there
      groups(Patch), // patches should always be there
      groups.getOrElse(Turtle, Seq()), // there might be no turtles
      groups.getOrElse(Link, Seq())) // there might be no links
  }

  def newRenderer: api.RendererInterface = {
    val renderer =
      util.Femto.get(
        classOf[api.RendererInterface],
        "org.nlogo.render.Renderer", Array(this))
    renderer.resetCache(patchSize)
    renderer
  }


  def patchColors: Array[Int] =
    patches.agentSeq
      .map(patch => api.Color.getRGBInt(patch.pcolor))
      .toArray

  class FakeAgentSet[A <: api.Agent](val kind: api.AgentKind, val agentSeq: Seq[A], val isDirected: Boolean = false)
    extends api.AgentSet {
    override val isUndirected: Boolean = !isDirected
    override def isEmpty = agentSeq.isEmpty
    override def count = agentSeq.size
    override def equalAgentSets(other: api.AgentSet) = unsupported
    override val agents = (agentSeq: Iterable[api.Agent]).asJava
    override def printName = unsupported
    override def contains(a: api.Agent) = unsupported
    override def removableAgents = unsupported
  }
  trait FakeAgent extends api.Agent {
    val vars: Seq[AnyRef]
    override def variables = unsupported
    def world = unsupported
    def classDisplayName = unsupported
    def alpha = unsupported
    def getVariable(vn: Int) = unsupported
    def setVariable(vn: Int, value: AnyRef) = unsupported
  }

  class FakeTurtle(agentId: Long, val vars: Seq[AnyRef])
    extends api.Turtle with FakeAgent {
    import MirrorableTurtle._
    override def kind = api.AgentKind.Turtle
    override def id = agentId
    override def xcor = vars(VAR_XCOR).asInstanceOf[Double]
    override def ycor = vars(VAR_YCOR).asInstanceOf[Double]
    override def heading = vars(VAR_HEADING).asInstanceOf[Double]
    override def hidden = vars(VAR_HIDDEN).asInstanceOf[Boolean]
    override def lineThickness = vars(tvLineThickness).asInstanceOf[Double]
    override def color: AnyRef = vars(VAR_COLOR)
    override def labelString = org.nlogo.api.Dump.logoObject(vars(VAR_LABEL))
    override def hasLabel = labelString.nonEmpty
    override def labelColor = vars(VAR_LABELCOLOR)
    override def getBreed = turtleBreeds.getOrElse(vars(VAR_BREED).asInstanceOf[String], turtles)
    override def size = vars(VAR_SIZE).asInstanceOf[Double]
    override def shape = vars(VAR_SHAPE).asInstanceOf[String]
    override def getBreedIndex = unsupported
    override def getPatchHere = unsupported
    override def jump(distance: Double) = unsupported
    override def heading(d: Double) = unsupported
  }

  override val turtles =
    new FakeAgentSet(api.AgentKind.Turtle, turtleStates.map {
      case (id, vars) => new FakeTurtle(id, vars)
    }.sortBy(_.id))

  class FakePatch(agentId: Long, val vars: Seq[AnyRef])
    extends api.Patch with FakeAgent {
    override def kind = api.AgentKind.Patch
    override def id = agentId
    override def pxcor = vars(VAR_PXCOR).asInstanceOf[Int]
    override def pycor = vars(VAR_PYCOR).asInstanceOf[Int]
    override def pcolor: AnyRef = vars(VAR_PCOLOR)
    override def labelString = org.nlogo.api.Dump.logoObject(vars(VAR_PLABEL))
    override def hasLabel = labelString.nonEmpty
    override def labelColor = vars(VAR_PLABELCOLOR)
    override def getPatchAtOffsets(dx: Double, dy: Double) = unsupported
    override def size = 1
  }

  override val patches =
    new FakeAgentSet(api.AgentKind.Patch, patchStates.map {
      case (id, vars) => new FakePatch(id, vars)
    }.sortBy(_.id))

  class FakeLink(agentId: Long, val vars: Seq[AnyRef]) extends api.Link with FakeAgent {
    import MirrorableLink._
    override def id = agentId
    override def kind = api.AgentKind.Link
    override def getBreedIndex: Int = unsupported
    override def labelColor: AnyRef = vars(VAR_LLABELCOLOR)
    override def labelString: String = org.nlogo.api.Dump.logoObject(vars(VAR_LLABEL))
    override def color: AnyRef = vars(VAR_LCOLOR)
    override def hasLabel: Boolean = labelString.nonEmpty
    override def lineThickness: Double = vars(VAR_THICKNESS).asInstanceOf[Double]
    override def hidden: Boolean = vars(VAR_LHIDDEN).asInstanceOf[Boolean]
    override def linkDestinationSize: Double = end2.size
    override def heading: Double = vars(lvHeading).asInstanceOf[Double]
    override def isDirectedLink: Boolean = getBreed.isDirected
    override def x1: Double = end1.xcor
    override def y1: Double = end1.ycor
    override def x2: Double = shortestPathX(end1.xcor, end2.xcor)
    override def y2: Double = shortestPathY(end1.ycor, end2.ycor)
    override def midpointX: Double = vars(lvMidpointX).asInstanceOf[Double]
    override def midpointY: Double = vars(lvMidpointY).asInstanceOf[Double]
    override def getBreed: api.AgentSet = linkBreeds.getOrElse(vars(VAR_LBREED).asInstanceOf[String], links)
    override lazy val end1 = turtles.agentSeq.find(_.id == vars(VAR_END1).asInstanceOf[Long]).get
    override lazy val end2 = turtles.agentSeq.find(_.id == vars(VAR_END2).asInstanceOf[Long]).get
    override def size: Double = vars(lvSize).asInstanceOf[Double]
    override def shape = vars(VAR_LSHAPE).asInstanceOf[String]
    override def toString = "link " + end1.id + " " + end2.id // TODO: get breed name in there
  }

  override val links = {
    val agentSeq = linkStates
      .map { case (id, vars) => new FakeLink(id, vars) }
      .sortBy(_.id)
    new FakeAgentSet(api.AgentKind.Link, agentSeq, worldVar[Boolean](wvUnbreededLinksAreDirected)) {
      override val agents =
        (agentSeq.sortBy(l => (l.end1.id, l.end2.id)): Iterable[api.Agent]).asJava
    }
  }

  class FakeObserver(val vars: Seq[AnyRef], val nbInterfaceGlobals: Int) extends api.Observer with FakeAgent {
    import MirrorableObserver._
    def targetAgent: api.Agent = {
      vars(ovTargetAgent(nbInterfaceGlobals)).asInstanceOf[Option[(Int, Long)]].flatMap {
        case (agentKind, id) =>
        Serializer.agentKindFromInt(agentKind) match {
          case Turtle => turtles.agentSeq.find(_.id == id)
          case Link   => links.agentSeq.find(_.id == id)
          case Patch  => patches.agentSeq.find(_.id == id)
        }
    }.orNull
    }
    def kind = api.AgentKind.Observer
    def id = 0
    def size = 0
    def perspective: api.Perspective = unsupported
    def oxcor: Double = unsupported
    def oycor: Double = unsupported
    def setPerspective(p: api.Perspective, a: api.Agent) = unsupported
  }

  private def worldVar[T](i: Int) = worldVars(i).asInstanceOf[T]

  def ticks = worldVar[Double](wvTicks)
  def patchesWithLabels = worldVar[Int](wvPatchesWithLabels)
  def turtleShapeList = worldVar[api.ShapeList](wvTurtleShapeList)
  def linkShapeList = worldVar[api.ShapeList](wvLinkShapeList)
  def patchSize = worldVar[Double](wvPatchSize)
  def worldWidth = worldVar[Int](wvWorldWidth)
  def worldHeight = worldVar[Int](wvWorldHeight)
  def minPxcor = worldVar[Int](wvMinPxcor)
  def minPycor = worldVar[Int](wvMinPycor)
  def maxPxcor = worldVar[Int](wvMaxPxcor)
  def maxPycor = worldVar[Int](wvMaxPycor)
  def wrappingAllowedInX = worldVar[Boolean](wvWrappingAllowedInX)
  def wrappingAllowedInY = worldVar[Boolean](wvWrappingAllowedInY)
  def patchesAllBlack = worldVar[Boolean](wvPatchesAllBlack)

  def program = {
    def makeBreedMap(breedsVar: Int) =
      worldVar[collection.immutable.ListMap[String, Boolean]](breedsVar).map {
        case (breedName, isDirected) => breedName -> api.Breed(breedName, "", Seq(), isDirected)
      }
    api.Program.empty.copy(
      breeds = makeBreedMap(wvTurtleBreeds),
      linkBreeds = makeBreedMap(wvLinkBreeds))
  }

  private def makeBreeds[A <: FakeAgent](
    breeds: Iterable[api.Breed],
    allAgents: FakeAgentSet[A],
    breedVariableIndex: Int): Map[String, FakeAgentSet[_]] =
    breeds.map { breed =>
      val agentSeq = allAgents.agentSeq.filter(_.vars(breedVariableIndex) == breed.name)
      breed.name -> new FakeAgentSet[A](allAgents.kind, agentSeq, breed.isDirected)
    }.toMap

  private val turtleBreeds = makeBreeds(program.breeds.values, turtles, VAR_BREED)
  private val linkBreeds = makeBreeds(program.linkBreeds.values, links, VAR_LBREED)

  override def getBreed(name: String) = turtleBreeds(name)
  override def getLinkBreed(name: String) = linkBreeds(name)

  def getPatch(i: Int): api.Patch = patches.agentSeq(i)

  override lazy val observer: api.Observer =
    new FakeObserver(observerVars, worldVar[Int](wvNbInterfaceGlobals))

  def wrap(pos: Double, min: Double, max: Double): Double =
    // This is basically copied from org.nlogo.agent.Topology to avoid a dependency to the latter.
    // Maybe the Topology.wrap function should be made accessible from the api package. NP 2012-07-24 
    if (pos >= max)
      (min + ((pos - max) % (max - min)))
    else if (pos < min) {
      val result = max - ((min - pos) % (max - min))
      if (result < max) result else min
    } else pos

  // More ugly repetition to get around the Topology dependency:
  def shortestPathX(x1: Double, x2: Double) =
    if (wrappingAllowedInX) {
      val xprime = if (x1 > x2) x2 + worldWidth else x2 - worldWidth
      if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1)) xprime else x2
    } else x2
  def shortestPathY(y1: Double, y2: Double) =
    if (wrappingAllowedInY) {
      val yprime = if (y1 > y2) y2 + worldHeight else y2 - worldHeight
      if (StrictMath.abs(y2 - y1) > StrictMath.abs(yprime - y1)) yprime else y2
    } else y2

  def trailDrawer: api.TrailDrawerInterface = newRenderer.trailDrawer

  def getPatchAt(x: Double, y: Double): api.Patch = unsupported
  def fastGetPatchAt(x: Int, y: Int): api.Patch = unsupported
  def followOffsetX: Double = unsupported
  def followOffsetY: Double = unsupported
  def wrapX(x: Double): Double = unsupported
  def wrapY(y: Double): Double = unsupported
  def getDrawing: AnyRef = unsupported
  def sendPixels: Boolean = unsupported
  def markDrawingClean() = unsupported
  def protractor: api.Protractor = unsupported
  def wrappedObserverX(x: Double): Double = unsupported
  def wrappedObserverY(y: Double): Double = unsupported
  def patchColorsDirty: Boolean = unsupported
  def markPatchColorsDirty() = unsupported
  def markPatchColorsClean() = unsupported
  def getVariablesArraySize(link: api.Link, breed: api.AgentSet): Int = unsupported
  def getVariablesArraySize(turtle: api.Turtle, breed: api.AgentSet): Int = unsupported
  def linksOwnNameAt(i: Int): String = unsupported
  def turtlesOwnNameAt(i: Int): String = unsupported
  def breedsOwnNameAt(breed: api.AgentSet, i: Int): String = unsupported
  def allStoredValues: Iterator[AnyRef] = unsupported
  def mayHavePartiallyTransparentObjects: Boolean = unsupported
  def timer: api.Timer = unsupported
  def setObserverVariableByName(variableName: String, value: AnyRef): Unit = unsupported
  def observerOwnsIndexOf(name: String): Int = unsupported
  def auxRNG: util.MersenneTwisterFast = unsupported
  def mainRNG: util.MersenneTwisterFast = unsupported
  def equalDimensions(d: api.WorldDimensions): Boolean = unsupported
  def setDimensionVariable(variableName: String, value: Int, d: api.WorldDimensions): api.WorldDimensions = unsupported
  def isDimensionVariable(variableName: String): Boolean = unsupported
  def getDimensions: api.WorldDimensions = unsupported
  def realloc(): Unit = unsupported
  def clearGlobals(): Unit = unsupported

  private def unsupported = throw new UnsupportedOperationException
}
