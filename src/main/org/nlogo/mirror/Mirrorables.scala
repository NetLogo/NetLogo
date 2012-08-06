// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api.AgentVariableNumbers._
import org.nlogo.api
import org.nlogo.plot

import collection.JavaConverters._

object Mirrorables {

  case object Patch extends Kind
  case object Turtle extends Kind
  case object Link extends Kind
  case object Observer extends Kind
  case object World extends Kind
  case object Plot extends Kind
  case object PlotPen extends Kind
  case object InterfaceGlobals extends Kind

  implicit def agentKindToMirrorKind(agentKind: api.AgentKind) = agentKind match {
    case api.AgentKind.Observer => Observer
    case api.AgentKind.Turtle   => Turtle
    case api.AgentKind.Patch    => Patch
    case api.AgentKind.Link     => Link
  }

  // so we don't fill up memory with duplicate AgentKey objects
  val keyCache = collection.mutable.WeakHashMap[api.Agent, AgentKey]()
  def getAgentKey(agent: api.Agent) = keyCache.getOrElseUpdate(agent, AgentKey(agent.kind, agent.id))
  abstract class MirrorableAgent[T <: api.Agent](agent: T) extends Mirrorable {
    override def getVariable(index: Int) = variables.getOrElse(index, agent.getVariable(index))
    override def agentKey = getAgentKey(agent)
  }

  object MirrorableTurtle {
    val tvLineThickness =
      api.AgentVariables.getImplicitTurtleVariables(api.Version.is3D).size
    val lastIndex = tvLineThickness
  }
  class MirrorableTurtle(turtle: api.Turtle) extends MirrorableAgent(turtle) {
    import MirrorableTurtle._
    override def kind = Turtle
    override def nbVariables = lastIndex + 1
    override val variables = Map(
      VAR_BREED -> turtle.getBreed.printName,
      tvLineThickness -> Double.box(turtle.lineThickness))
  }

  class MirrorablePatch(patch: api.Patch) extends MirrorableAgent(patch) {
    override def kind = Patch
    override def nbVariables =
      api.AgentVariables.getImplicitPatchVariables(api.Version.is3D).size
    override val variables = Map(
      VAR_PXCOR -> Int.box(patch.pxcor),
      VAR_PYCOR -> Int.box(patch.pycor))
  }

  object MirrorableLink {
    private val n = api.AgentVariables.getImplicitLinkVariables.size
    val Seq(
      lvSize,
      lvHeading,
      lvMidpointX,
      lvMidpointY,
      _*) = Stream.from(n)
    val lastIndex = lvMidpointY
  }
  class MirrorableLink(link: api.Link) extends MirrorableAgent(link) {
    import MirrorableLink._
    override def kind = Link
    override def nbVariables = lastIndex + 1
    override val variables = Map(
      VAR_END1 -> Long.box(link.end1.id),
      VAR_END2 -> Long.box(link.end2.id),
      VAR_LBREED -> link.getBreed.printName,
      lvSize -> Double.box(link.size),
      lvHeading -> Double.box(link.heading),
      lvMidpointX -> Double.box(link.midpointX),
      lvMidpointY -> Double.box(link.midpointY))
  }

  object MirrorableObserver {
    val ovTargetAgent = 0
    val lastIndex = ovTargetAgent
  }
  class MirrorableObserver(observer: api.Observer) extends MirrorableAgent(observer) {
    import MirrorableObserver._
    override def kind = Link
    override def nbVariables = lastIndex + 1
    override val variables = Map(
      ovTargetAgent -> Option(observer.targetAgent).map(getAgentKey).map(key => (Serializer.agentKindToInt(key.kind), key.id)))
  }

  object MirrorableWorld {
    val Seq(
      wvPatchesWithLabels,
      wvTurtleShapeList,
      wvLinkShapeList,
      wvPatchSize,
      wvWorldWidth,
      wvWorldHeight,
      wvMinPxcor,
      wvMinPycor,
      wvMaxPxcor,
      wvMaxPycor,
      wvWrappingAllowedInX,
      wvWrappingAllowedInY,
      wvPatchesAllBlack,
      wvTurtleBreeds,
      wvLinkBreeds,
      wvUnbreededLinksAreDirected,
      wvTrailDrawing,
      _*) = Stream.from(0)
  }
  class MirrorableWorld(world: api.World) extends Mirrorable {
    import MirrorableWorld._
    override def kind = World
    override def agentKey = AgentKey(kind, 0) // dummy id for the one and unique world
    override val variables = Map(
      wvPatchesWithLabels -> Int.box(world.patchesWithLabels),
      wvTurtleShapeList -> world.turtleShapeList, // probably not good enough to just pass the shapelists like that...
      wvLinkShapeList -> world.linkShapeList,
      wvPatchSize -> Double.box(world.patchSize),
      wvWorldWidth -> Int.box(world.worldWidth),
      wvWorldHeight -> Int.box(world.worldHeight),
      wvMinPxcor -> Int.box(world.minPxcor),
      wvMinPycor -> Int.box(world.minPycor),
      wvMaxPxcor -> Int.box(world.maxPxcor),
      wvMaxPycor -> Int.box(world.maxPycor),
      wvWrappingAllowedInX -> Boolean.box(world.wrappingAllowedInX),
      wvWrappingAllowedInY -> Boolean.box(world.wrappingAllowedInY),
      wvPatchesAllBlack -> Boolean.box(world.patchesAllBlack),
      wvTurtleBreeds -> world.program.breeds.map { case (breedName, breed) => breedName -> breed.isDirected },
      wvLinkBreeds -> world.program.linkBreeds.map { case (breedName, breed) => breedName -> breed.isDirected },
      wvUnbreededLinksAreDirected -> Boolean.box(world.links.isDirected),
      wvTrailDrawing ->
        (if (world.trailDrawer.isDirty) {
          val outputStream = new java.io.ByteArrayOutputStream
          val img = world.trailDrawer.getDrawing.asInstanceOf[java.awt.image.BufferedImage]
          javax.imageio.ImageIO.write(img, "png", outputStream)
          Some(outputStream.toByteArray())
        } else None))
  }

  object MirrorablePlot {
    val Seq(
      pvXMin,
      pvXMax,
      pvYMin,
      pvYMax,
      pvLegendIsOpen,
      _*) = Stream.from(0)
  }

  class MirrorablePlot(val p: plot.Plot, val plots: List[plot.Plot]) extends Mirrorable {
    import MirrorablePlot._
    override def kind = Plot
    override def agentKey = AgentKey(kind, plots.indexOf(p))
    override val variables = Map(
      pvXMin -> Double.box(p.xMin),
      pvXMax -> Double.box(p.xMax),
      pvYMin -> Double.box(p.yMin),
      pvYMax -> Double.box(p.yMax),
      pvLegendIsOpen -> Boolean.box(p.legendIsOpen))
  }

  object MirrorablePlotPen {
    val Seq( // init vals for indices by pattern matching over range of getters
      ppvName,
      ppvIsDown,
      ppvMode,
      ppvInterval,
      ppvColor,
      ppvX,
      ppvPoints,
      _*) = Stream.from(0)
  }
  class MirrorablePlotPen(val pen: plot.PlotPen, val plots: List[plot.Plot]) extends Mirrorable {
    import MirrorablePlotPen._
    override def kind = PlotPen
    override def agentKey = {
      // we combine the plot id and the pen id (which are both
      // originally Ints) into a single Long:
      val plotId: Long = plots.indexOf(pen.plot)
      val penId: Long = pen.plot.pens.indexOf(pen)
      AgentKey(kind, (plotId << 32) | penId)
    }
    override val variables = Map(
      ppvName -> pen.name,
      ppvIsDown -> Boolean.box(pen.isDown),
      ppvMode -> Int.box(pen.mode),
      ppvInterval -> Double.box(pen.interval),
      ppvColor -> org.nlogo.api.Color.argbToColor(pen.color),
      ppvX -> Double.box(pen.x),
      ppvPoints -> pen.points.toList)
  }

  class MirrorableInterfaceGlobals(world: api.World) extends Mirrorable {
    def kind = InterfaceGlobals
    def agentKey = AgentKey(kind, 0)
    val variables =
      world.program.interfaceGlobals
        .zipWithIndex
        .map { case (name, i) => i -> (name, world.observer.getVariable(i)) }
        .toMap
  }

  def allMirrorables(world: api.World, plots: List[plot.Plot]): Iterable[Mirrorable] = {
    import collection.JavaConverters._
    val turtles = world.turtles.agents.asScala.map(t => new MirrorableTurtle(t.asInstanceOf[api.Turtle]))
    val patches = world.patches.agents.asScala.map(p => new MirrorablePatch(p.asInstanceOf[api.Patch]))
    val links = world.links.agents.asScala.map(l => new MirrorableLink(l.asInstanceOf[api.Link]))
    val worldIterable = Iterable(new MirrorableWorld(world))
    val observerIterable = Iterable(new MirrorableObserver(world.observer))
    // val interfaceGlobals = Iterable(new MirrorableInterfaceGlobals(world))
    // val plotMirrorables = for { p <- plots } yield new MirrorablePlot(p, plots)
    // val plotPens = for { p <- plots; pp <- p.pens } yield new MirrorablePlotPen(pp, plots)
    // (worldIterable ++ observerIterable ++ interfaceGlobals ++ turtles ++ patches ++ links ++ plotMirrorables ++ plotPens)
    (worldIterable ++ observerIterable ++ turtles ++ patches ++ links)
  }

}
