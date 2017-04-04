// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, Color, CompilerServices, ImporterUser,
  LogoException, MersenneTwisterFast, RandomSeedGenerator,
  Timer, TrailDrawerInterface, ValueConstraint, Version, WorldDimensionException }

import org.nlogo.core.{ AgentKind, Breed, Dialect, Nobody, Program,
  Shape, ShapeList, ShapeListTracker, WorldDimensions }

import java.lang.{ Double => JDouble, Integer => JInteger }

import java.util.{ ArrayList => JArrayList, Arrays,
  HashMap => JHashMap, Iterator => JIterator, List => JList,
  Map => JMap }

import java.util.concurrent.CopyOnWriteArrayList

import org.nlogo.agent.Importer.{ ErrorHandler => ImporterErrorHandler, StringReader => ImporterStringReader }

import scala.collection.{ Iterator => SIterator }

object World {
  val Zero = JDouble.valueOf(0.0)
  val One  = JDouble.valueOf(1.0)

  val NegativeOneInt = JInteger.valueOf(-1)

  trait VariableWatcher {
    /**
     * Called when the watched variable is set.
     * @param agent The agent for which the variable was set
     * @param variableName The name of the variable as an upper case string
     * @param value The new value of the variable
     */
    def update(agent: Agent, variableName: String, value: Object): Unit
  }

  trait InRadiusOrCone {
    def inRadius(agent: Agent, sourceSet: AgentSet, radius: Double, wrap: Boolean): JList[Agent]
    def inCone(turtle: Turtle, sourceSet: AgentSet, radius: Double, angle: Double, wrap: Boolean): JList[Agent]

  }
}

import World._

trait WorldKernel {
  def program: Program
  def observer: Observer
  def observers: AgentSet
  def patches: IndexedAgentSet
  def turtles: TreeAgentSet
  def links: TreeAgentSet
  private[agent] def topology: Topology
  private[agent] def breeds: JMap[String, AgentSet]
  private[agent] def linkBreeds: JMap[String, AgentSet]
  def clearAll(): Unit = {}
}

trait CoreWorld
  extends org.nlogo.api.WorldWithWorldRenderable
  with WorldKernel
  with DimensionManagement
  with WatcherManagement {

    // anything that affects the outcome of the model should happen on the
    // main RNG
    val mainRNG: MersenneTwisterFast = new MersenneTwisterFast()

    // anything that doesn't and can happen non-deterministically (for example monitor updates)
    // should happen on the auxillary rng. JobOwners should know which RNG they use.
    val auxRNG: MersenneTwisterFast = new MersenneTwisterFast()

    /// random seed generator
    def generateSeed = RandomSeedGenerator.generateSeed()

    val tieManager: TieManager

    val tickCounter: TickCounter = new TickCounter()

    val timer: Timer = new Timer()

    private[agent] def breeds: JMap[String, AgentSet]
    private[agent] def linkBreeds: JMap[String, AgentSet]

    def linkManager: LinkManager
    // Patches are indexed in row-major order. See `getPatchAt`.
    // This is also true in 3D (with the x-coordinate corresponding to a stride
    // of 1, the y-coordinate with a stride of world-width, and the z-coordinate
    // with a stride of world-width * world-height)
    private[agent] var _patches: IndexedAgentSet = null
    def patches: IndexedAgentSet = _patches

    protected var _links: TreeAgentSet = null;
    def links: TreeAgentSet = _links

    private[agent] var topology: Topology = _

    def getLinkVariablesArraySize(breed: AgentSet): Int

    def getBreedSingular(breed: AgentSet): String
    def getLinkBreedSingular(breed: AgentSet): String

    abstract override def clearAll(): Unit = {
      super.clearAll()
      tickCounter.clear()
    }

    def ticks: Double = tickCounter.ticks

    def allStoredValues: scala.collection.Iterator[Object] = AllStoredValues.apply(this)
}

trait World extends CoreWorld with GrossWorldState with AgentManagement {
  def inRadiusOrCone: World.InRadiusOrCone
  def clearDrawing(): Unit
  def protractor: Protractor
  def diffuse(param: Double, vn: Int): Unit
  def diffuse4(param: Double, vn: Int): Unit
  def stamp(agent: Agent, erase: Boolean): Unit
  def changeTopology(xWrapping: Boolean, yWrapping: Boolean): Unit
  def exportWorld(writer: java.io.PrintWriter, full: Boolean): Unit
  @throws(classOf[java.io.IOException])
  def importWorld(errorHandler: ImporterErrorHandler, importerUser: ImporterUser,
    stringReader: org.nlogo.agent.Importer.StringReader,
    reader: java.io.BufferedReader): Unit
  def notifyWatchers(agent: Agent, vn: Int, value: Object): Unit
  def sprout(patch: Patch, breed: AgentSet): Turtle
  def copy(): World
}

// A note on wrapping: normally whether x and y coordinates wrap is a
// product of the topology.  But we also have the old "-nowrap" primitives
// that don't wrap regardless of what the topology is.  So that's why many
// methods like distance() and towards() take a boolean argument "wrap";
// it's true for the normal prims, false for the nowrap prims. - ST 5/24/06
class World2D extends World with CompilationManagement {

  val protractor: Protractor = new Protractor(this)

  val linkManager: LinkManager =
    new LinkManagerImpl(this,
      new LinkFactory[World]() {
        def apply(world: World, src: Turtle, dest: Turtle, breed: AgentSet): Link = {
          new Link(world, src, dest, breed)
        }
      })
  val tieManager: TieManager = new TieManager(this, linkManager)

  protected val dimensionVariableNames =
    Seq("MIN-PXCOR", "MAX-PXCOR", "MIN-PYCOR", "MAX-PYCOR", "WORLD-WIDTH", "WORLD-HEIGHT")

  val inRadiusOrCone: InRadiusOrCone = new InRadiusOrCone(this)

  /// observer/turtles/patches
  changeTopology(true, true)

  // create patches in the constructor, it's necessary in case
  // the first model we load is 1x1 since when we do create patches
  // in the model loader we only do the reallocation if the dimensions
  // are different than the stored dimensions.  This doesn't come up
  // often because almost always load the default model first, and there
  // aren't many 1x1 models. ev 2/5/07
  createPatches(_minPxcor, _maxPxcor, _minPycor, _maxPycor);
  setUpShapes(true);

  protected def createObserver(): Observer =
    new Observer(this)

  def changeTopology(xWrapping: Boolean, yWrapping: Boolean): Unit = {
    topology = Topology.getTopology(this, xWrapping, yWrapping)
    if (_patches != null) { // is null during initialization
      val it = _patches.iterator
      while (it.hasNext) {
        it.next().asInstanceOf[Patch].topologyChanged()
      }
    }
  }

  /// export world

  def exportWorld(writer: java.io.PrintWriter, full: Boolean): Unit =
    new Exporter(this, writer).exportWorld(full)

  @throws(classOf[java.io.IOException])
  def importWorld(errorHandler: ImporterErrorHandler, importerUser: ImporterUser,
                          stringReader: ImporterStringReader, reader: java.io.BufferedReader): Unit =
    new Importer(errorHandler, this, importerUser, stringReader).importWorld(reader)

  /// equality

  private[nlogo] def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: Object, size: Double, mode: String): Unit = {
    trailDrawer.drawLine(x0, y0, x1, y1, color, size, mode)
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse(param: Double, vn: Int): Unit =
    topology.diffuse(param, vn)

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse4(param: Double, vn: Int): Unit =
    topology.diffuse4(param, vn)

  def getDimensions: WorldDimensions =
    new WorldDimensions(_minPxcor, _maxPxcor, _minPycor, _maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY)

  // used by Importer and Parser
  def getOrCreateTurtle(id: Long): Turtle = {
    val turtle = getTurtle(id).asInstanceOf[Turtle]
    if (turtle == null) {
      val newTurtle = new Turtle2D(this, id)
      nextTurtleIndex(StrictMath.max(nextTurtleIndex, id + 1))
      newTurtle
    } else {
      turtle
    }
  }

  def createTurtle(breed: AgentSet): Turtle =
    new Turtle2D(this, breed, Zero, Zero)

  // c must be in 0-13 range
  // h can be out of range
  def createTurtle(breed: AgentSet, c: Int, h: Int): Turtle = {
    val baby = new Turtle2D(this, breed, Zero, Zero)
    baby.colorDoubleUnchecked(JDouble.valueOf(5 + 10 * c))
    baby.heading(h)
    baby
  }

  @throws(classOf[AgentException])
  def getPatchAt(x: Double, y: Double): Patch = {
    val xc = roundX(x)
    val yc = roundY(y)
    val id = ((_worldWidth * (_maxPycor - yc)) + xc - _minPxcor)
    getPatch(id)
  }

  // this procedure is the same as calling getPatchAt when the topology is a torus
  // meaning it will override the Topology's wrapping rules and
  def getPatchAtWrap(x: Double, y: Double): Patch = {
    val wrappedX = Topology.wrap(x, _minPxcor - 0.5, _maxPxcor + 0.5);
    val wrappedY = Topology.wrap(y, _minPycor - 0.5, _maxPycor + 0.5);
    val xc =
      if (wrappedX > 0) {
        (wrappedX + 0.5).toInt
      } else {
        val intPart = wrappedX.toInt
        val fractPart = intPart - wrappedX
        if (fractPart > 0.5) intPart - 1 else intPart
      }
    val yc =
      if (wrappedY > 0) {
        (wrappedY + 0.5).toInt
      } else {
        val intPart = wrappedY.toInt
        val fractPart = intPart - wrappedY
        if (fractPart > 0.5) intPart - 1 else intPart
      }
    val patchid = ((_worldWidth * (_maxPycor - yc)) + xc - _minPxcor)
    getPatch(patchid)
  }

  def fastGetPatchAt(xc: Int, yc: Int): Patch =
    getPatch(_worldWidth * (_maxPycor - yc) + xc - _minPxcor)

  def copy(): World = {
    val newWorld = new World2D()
    newWorld.tickCounter.ticks = tickCounter.ticks
    newWorld.program(program)
    copyDimensions(newWorld)
    copyAgents(newWorld, newWorld)
    copyGrossState(newWorld)
    newWorld
  }

  def createPatches(minPxcor: Int, maxPxcor: Int,
    minPycor: Int, maxPycor: Int): Unit = {

    _patchScratch = null

    _minPxcor = minPxcor
    _maxPxcor = maxPxcor
    _minPycor = minPycor
    _maxPycor = maxPycor
    _worldWidth = maxPxcor - minPxcor + 1
    _worldHeight = maxPycor - minPycor + 1
    _worldWidthBoxed = JDouble.valueOf(_worldWidth)
    _worldHeightBoxed = JDouble.valueOf(_worldHeight)
    _minPxcorBoxed = JDouble.valueOf(_minPxcor)
    _minPycorBoxed = JDouble.valueOf(_minPycor)
    _maxPxcorBoxed = JDouble.valueOf(_maxPxcor)
    _maxPycorBoxed = JDouble.valueOf(_maxPycor)

    rootsTable = new RootsTable(_worldWidth, _worldHeight)

    recreateAllBreeds()

    if (_turtles != null) {
      _turtles.clear() // so a SimpleChangeEvent is published
    }
    _turtles = new TreeAgentSet(AgentKind.Turtle, "TURTLES")
    if (_links != null) {
      _links.clear() // so a SimpleChangeEvent is published
    }
    _links = new TreeAgentSet(AgentKind.Link, "LINKS")

    val patchArray = new Array[Agent](_worldWidth * _worldHeight)
    _patchColors = new Array[Int](_worldWidth * _worldHeight)
    Arrays.fill(_patchColors, Color.getARGBbyPremodulatedColorNumber(0.0))
    _patchColorsDirty = true

    val numVariables = program.patchesOwn.size

    observer.resetPerspective()

    var i = 0
    var x = minPxcor
    var y = maxPycor
    while (i < _worldWidth * _worldHeight) {
      val patch = new Patch(this, i, x, y, numVariables)
      x += 1
      if (x > maxPxcor) {
        x = minPxcor
        y -= 1
      }
      patchArray(i) = patch
      i += 1
    }
    _patches = new ArrayAgentSet(AgentKind.Patch, "patches", patchArray)
    _patchesWithLabels = 0
    _patchesAllBlack = true
    _mayHavePartiallyTransparentObjects = false
  }

  override def clearAll(): Unit = {
    super.clearAll()
  }

  // in a 2D world the drawing lives in the
  // renderer so the workspace takes care of it.
  def clearDrawing(): Unit = { }

  def stamp(agent: Agent, erase: Boolean): Unit = {
    trailDrawer.stamp(agent, erase)
  }

  def sprout(patch: Patch, breed: AgentSet): Turtle = {
    new Turtle2D(this, breed, patch.pxcor, patch.pycor)
  }
}
