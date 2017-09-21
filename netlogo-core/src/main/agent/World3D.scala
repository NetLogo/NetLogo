// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ AgentKind, Program, WorldDimensions }
import org.nlogo.api.{ AgentException, Color, ImporterUser, NetLogoThreeDDialect,
  WorldDimensionException, WorldDimensions3D }

import java.lang.{ Double => JDouble }

import java.util.Arrays

import World._

class World3D extends World
  with org.nlogo.api.World3D
  with CompilationManagement {

  val drawing: Drawing3D = new Drawing3D(this)

  override val protractor: Protractor3D = new Protractor3D(this)

  def protractor3D: org.nlogo.api.Protractor3D = protractor

  private var _patchScratch3d: Array[Array[Array[Double]]] = _

  override val linkManager = new LinkManagerImpl(this,
    { (world: World3D, src: Turtle, dest: Turtle, breed: AgentSet) =>
      val l = new Link3D(world, src, dest, breed)
      l.setId(newLinkId())
      l
    })

  protected val _links: TreeAgentSet = new TreeAgentSet(AgentKind.Link, "LINKS")

  override val tieManager: TieManager3D = new TieManager3D(_links, linkManager, protractor)
  val inRadiusOrCone = new InRadiusOrCone3D(this)

  _mayHavePartiallyTransparentObjects = false

  protected val dimensionVariableNames =
    Seq("MIN-PXCOR", "MAX-PXCOR", "MIN-PYCOR", "MAX-PYCOR", "MIN-PZCOR", "MAX-PZCOR", "WORLD-WIDTH", "WORLD-HEIGHT", "WORLD-DEPTH")

  var _worldDepth: Int = _
  var _maxPzcor: Int = _
  var _minPzcor: Int = _

  def worldDepth: Int = _worldDepth
  def minPzcor: Int = _minPzcor
  def maxPzcor: Int = _maxPzcor

  var _worldDepthBoxed: JDouble = JDouble.valueOf(_worldDepth)
  var _maxPzcorBoxed: JDouble = JDouble.valueOf(_maxPzcor)
  var _minPzcorBoxed: JDouble = JDouble.valueOf(_minPzcor)

  def worldDepthBoxed = _worldDepthBoxed
  def maxPzcorBoxed   = _maxPzcorBoxed
  def minPzcorBoxed   = _minPzcorBoxed

  _topology = new Torus3D(this)
  createPatches(_minPxcor, _maxPxcor, _minPycor, _maxPycor, _minPzcor, _maxPzcor)

  override protected def createObserver(): Observer = new Observer3D(this)

  def changeTopology(xWrapping: Boolean, yWrapping: Boolean): Unit = {
    _topology = new Torus3D(this)
  }

  def changeTopology(xWrapping: Boolean, yWrapping: Boolean, zWrapping: Boolean): Unit = {
    _topology = new Torus3D(this)
  }

  def shortestPathZ(z1: Double, z2: Double): Double =
    topology.asInstanceOf[Topology3D].shortestPathZ(z1, z2)

  val wrappingAllowedInZ = true

  def wrappedObserverZ(z: Double): Double =
    topology.asInstanceOf[Topology3D].wrapZ(z - followOffsetZ)

  def followOffsetZ: Double =
    observer.asInstanceOf[Observer3D].followOffsetZ()

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse(param: Double, vn: Int): Unit =
    topology.diffuse(param, vn)

  def diffuse4(param: Double, vn: Int): Unit = {
    throw new UnsupportedOperationException()
  }

  def wrapZ(z: Double): Double =
    Topology.wrap(z, _minPzcor - 0.5, _maxPzcor + 0.5)

  def wrapAndRoundZ(_z: Double): Int = {
    // floor() is slow so we don't use it
    val z = topology.asInstanceOf[Topology3D].wrapZ(_z)

    if (z > 0) {
      (z + 0.5).toInt
    } else {
      val intPart = z.toInt
      val fractPart = intPart - z
      if (fractPart > 0.5) intPart - 1 else intPart
    }
  }

  // this procedure is the same as calling getPatchAt when the topology is a torus
  // meaning it will override the Topology's wrapping rules and
  def getPatchAtWrap(x: Double, y: Double, z: Double): Patch = {
    val wrappedX = Topology.wrap(x, _minPxcor - 0.5, _maxPxcor + 0.5)
    val wrappedY = Topology.wrap(y, _minPycor - 0.5, _maxPycor + 0.5)
    val wrappedZ = Topology.wrap(z, _minPzcor - 0.5, _maxPzcor + 0.5)

    def roundBase(base: Double): Int =
      if (base > 0) {
        (base + 0.5).toInt
      } else {
        val intPart = base.toInt
        val fractPart = intPart - base
        if (fractPart > 0.5) intPart - 1 else intPart
      }

    val xc = roundBase(wrappedX)
    val yc = roundBase(wrappedY)
    val zc = roundBase(wrappedZ)
    val patchid =
      ((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
        (_worldWidth * (_maxPycor - yc))
        + xc - _minPxcor)


    _patches.getByIndex(patchid).asInstanceOf[Patch]
  }

  def validPatchCoordinates(xc: Int, yc: Int, zc: Int): Boolean =
    xc >= _minPxcor && xc <= _maxPxcor &&
    yc >= _minPycor && yc <= _maxPycor &&
    zc >= _minPzcor && zc <= _maxPzcor

  def fastGetPatchAt(xc: Int, yc: Int, zc: Int): Patch =
    _patches.getByIndex(((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
      (_worldWidth * (_maxPycor - yc))
      + xc - _minPxcor)).asInstanceOf[Patch]

  override def fastGetPatchAt(xc: Int, yc: Int): Patch =
    fastGetPatchAt(xc, yc, 0)

  override def createPatches(dim: WorldDimensions): Unit = {
    val d = dim.asInstanceOf[WorldDimensions3D]
    createPatches(dim.minPxcor, dim.maxPxcor,
                  dim.minPycor, dim.maxPycor,
                  d.minPzcor, d.maxPzcor)
  }

  override def createPatches(minPxcor: Int, maxPxcor: Int,
                            minPycor: Int, maxPycor: Int): Unit = {
    createPatches(minPxcor, maxPxcor, minPycor, maxPycor, 0, 0)
  }

  override def newProgram: Program =
    Program.fromDialect(NetLogoThreeDDialect)

  override def newProgram(interfaceGlobals: Seq[String]): Program =
    newProgram.copy(interfaceGlobals = interfaceGlobals)

  // 3D world copying isn't yet supported
  def copy(): World = null

  def createPatches(
    minPxcor: Int, maxPxcor: Int,
    minPycor: Int, maxPycor: Int,
    minPzcor: Int, maxPzcor: Int): Unit = {

    _patchScratch = null
    _patchScratch3d = null

    _minPxcor = minPxcor
    _maxPxcor = maxPxcor
    _minPycor = minPycor
    _maxPycor = maxPycor
    _minPzcor = minPzcor
    _maxPzcor = maxPzcor
    _worldWidth = _maxPxcor - _minPxcor + 1
    _worldHeight = _maxPycor - _minPycor + 1
    _worldDepth = _maxPzcor - _minPzcor + 1

    rootsTable = new RootsTable(_worldWidth, _worldHeight)

    _worldWidthBoxed = JDouble.valueOf(_worldWidth)
    _worldHeightBoxed = JDouble.valueOf(_worldHeight)
    _worldDepthBoxed = JDouble.valueOf(_worldDepth)
    _minPxcorBoxed = JDouble.valueOf(_minPxcor)
    _minPycorBoxed = JDouble.valueOf(_minPycor)
    _minPzcorBoxed = JDouble.valueOf(_minPzcor)
    _maxPxcorBoxed = JDouble.valueOf(_maxPxcor)
    _maxPycorBoxed = JDouble.valueOf(_maxPycor)
    _maxPzcorBoxed = JDouble.valueOf(_maxPzcor)


    breeds.clear()

    program.breeds.foreach {
      case (name, breed) =>
        val agentset = new TreeAgentSet(AgentKind.Turtle, breed.name)
        breeds.put(name.toUpperCase, agentset)
    }

    _turtles.clear() // so a SimpleChangeEvent is published
    _links.clear() // so a SimpleChangeEvent is published
    _links.directed = Directedness.Undetermined

    var x = _minPxcor
    var y = _maxPycor
    var z = _maxPzcor
    val patchArray = new Array[Agent](_worldWidth * _worldHeight * _worldDepth)
    _patchColors = new Array[Int](_worldWidth * _worldHeight * _worldDepth)
    Arrays.fill(_patchColors, Color.getARGBbyPremodulatedColorNumber(0.0))
    _patchColorsDirty = true

    val numVariables = program.patchesOwn.size

    observer.resetPerspective()

    var i = 0
    while (i < _worldWidth * _worldHeight * _worldDepth) {
      val patch = new Patch3D(this, i, x, y, z, numVariables)
      x += 1
      if (x == (_maxPxcor + 1)) {
        x = _minPxcor
        y -= 1
        if (y == (_minPycor - 1)) {
          y = _maxPycor
          z -= 1
        }
      }
      patchArray(i) = patch
      i += 1
    }
    _patches = new ArrayAgentSet(AgentKind.Patch, "patches", patchArray)
    _patchesWithLabels = 0
    _patchesAllBlack = true
    _mayHavePartiallyTransparentObjects = false
  }

  /// export world

  def exportWorld(writer: java.io.PrintWriter, full: Boolean): Unit = {
    new Exporter3D(this, writer).exportWorld(full)
  }

  @throws(classOf[java.io.IOException])
  def importWorld(errorHandler: org.nlogo.agent.ImporterJ.ErrorHandler,
    importerUser: ImporterUser,
    stringReader: org.nlogo.agent.ImporterJ.StringReader,
    reader: java.io.BufferedReader): Unit =
    new Importer3D(errorHandler, this, importerUser, stringReader).importWorld(reader)

  // used by Importer and Parser
  def getOrCreateTurtle(id: Long): Turtle = {
    val turtle = getTurtle(id)
    if (turtle == null) {
      new Turtle3D(this, id)
    } else {
      turtle
    }
  }

  def getPatchScratch3d: Array[Array[Array[Double]]] = {
    if (_patchScratch3d == null) {
      _patchScratch3d = Array.ofDim[Double](_worldWidth, _worldHeight, _worldDepth)
    }
    _patchScratch3d
  }

  // these methods are primarily for behaviorspace
  // to vary the size of the world without
  // knowing quite so much about the world.
  // ev 2/20/06
  override def getDimensions: WorldDimensions3D = {
    new WorldDimensions3D(_minPxcor, _maxPxcor, _minPycor, _maxPycor, _minPzcor, _maxPzcor, 12.0, true, true, true)
  }

  override def dimensionsAdjustedForPatchSize(patchSize: Double): WorldDimensions =
    getDimensions.copyThreeD(patchSize = patchSize)

  @throws(classOf[WorldDimensionException])
  override def setDimensionVariable(variableName: String, value: Int, d: WorldDimensions): WorldDimensions = {
    val wd = d.asInstanceOf[WorldDimensions3D]
    variableName.toUpperCase match {
      case "MIN-PZCOR" => wd.copyThreeD(minPzcor = value)
      case "MAX-PZCOR" => wd.copyThreeD(maxPzcor = value)
      case "WORLD-DEPTH" =>
        val newMin = growMin(wd.minPzcor, wd.maxPzcor, value, wd.minPzcor)
        val newMax = growMax(wd.minPzcor, wd.maxPzcor, value, wd.maxPzcor)
        wd.copyThreeD(maxPzcor = newMax, minPzcor = newMin)
      case other =>
        val newWd = super.setDimensionVariable(variableName, value, d)
        new WorldDimensions3D(
          newWd.minPxcor, newWd.maxPxcor,
          newWd.minPycor, newWd.maxPycor,
          wd.minPzcor,    wd.maxPzcor,
          12.0, true, true, true)
    }
  }

  override def equalDimensions(d: WorldDimensions): Boolean =
    d.minPxcor == _minPxcor &&
      d.maxPxcor == _maxPxcor &&
      d.minPycor == _minPycor &&
      d.maxPycor == _maxPycor &&
      d.asInstanceOf[WorldDimensions3D].minPzcor == _minPzcor &&
      d.asInstanceOf[WorldDimensions3D].maxPzcor == _maxPzcor

  @throws(classOf[AgentException])
  override def getPatchAt(x: Double, y: Double): Patch3D =
    getPatchAt(x, y, 0)

  @throws(classOf[AgentException])
  override def getPatchAt(x: Double, y: Double, z: Double): Patch3D = {
    val xc = wrapAndRoundX(x)
    val yc = wrapAndRoundY(y)
    val zc = wrapAndRoundZ(z)

    val id = ((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
        (_worldWidth * (_maxPycor - yc))
        + xc - _minPxcor)

    _patches.getByIndex(id).asInstanceOf[Patch3D]
  }

  override def createTurtle(breed: AgentSet): Turtle =
    new Turtle3D(this, breed, Zero, Zero, Zero)

  // c must be in 0-13 range
  // h can be out of range
  override def createTurtle(breed: AgentSet, c: Int, h: Int): Turtle = {
    val baby = new Turtle3D(this, breed, Zero, Zero, Zero)
    baby.colorDoubleUnchecked(JDouble.valueOf(5 + 10 * c))
    baby.heading(h)
    baby
  }

  override def getDrawing: AnyRef = drawing

  // we don't ever send pixels in 3D because
  // 3D drawing is vector based. ev 5/30/06
  override def sendPixels: Boolean = false

  private[nlogo] def drawLine(
    x0: Double, y0: Double,
    x1: Double, y1: Double,
    color: AnyRef, size: Double, mode: String) {
    drawing.drawLine(x0, y0, 0, x1, y1, 0, size, color)
  }

  private[nlogo] def drawLine(
    x0: Double, y0: Double, z0: Double,
    x1: Double, y1: Double, z1: Double,
    color: AnyRef, size: Double): Unit = {
      drawing.drawLine(x0, y0, z0, x1, y1, z1, size, color)
  }

  override def clearAll(): Unit = {
    super.clearAll()
    drawing.clear()
  }

  def clearDrawing(): Unit = {
    drawing.clear()
  }

  def stamp(agent: Agent, erase: Boolean): Unit = {
    if (!erase) {
      drawing.stamp(agent)
    }
  }

  def sprout(patch: Patch, breed: AgentSet): Turtle = {
    val p3d = patch.asInstanceOf[Patch3D]
    new Turtle3D(this, breed, p3d.pxcor, p3d.pycor, p3d.pzcor)
  }
}
