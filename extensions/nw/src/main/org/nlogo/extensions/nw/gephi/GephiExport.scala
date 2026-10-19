package org.nlogo.extensions.nw.gephi

import java.io.{ File, StringWriter }

import org.nlogo.agent.{ AgentSet, Link, Turtle, World }
import org.nlogo.api
import org.nlogo.api._
import org.nlogo.core.Breed

import org.gephi.graph.api.{ Column, GraphController }
import org.gephi.io.exporter.api.ExportController
import org.gephi.io.exporter.plugin.{ ExporterCSV, ExporterGraphML }
import org.gephi.io.exporter.spi.{ CharacterExporter, Exporter }
import org.gephi.project.api.ProjectController
import org.gephi.utils.longtask.spi.LongTask
import org.gephi.utils.progress.ProgressTicket
import org.openide.util.Lookup

import org.nlogo.extensions.nw.GraphContext
import org.nlogo.extensions.nw.NetworkExtensionUtil._

object GephiExport {
  val exportController = GephiUtils.withNWLoaderContext {Lookup.getDefault.lookup(classOf[ExportController])}

  def save(context: GraphContext, world: World, file: File, extension: String): Unit = GephiUtils.withNWLoaderContext {
    save(context, world, file, exportController.getExporter(extension))
  }

  def save(context: GraphContext, world: World, file: File): Unit = GephiUtils.withNWLoaderContext {
    save(context, world, file, exportController.getFileExporter(file))
  }

  def save(context: GraphContext, world: World, file: File, exporter: Exporter): Unit = GephiUtils.withNWLoaderContext {
    prepareExport(exporter, file.toString)
    buildWorkspace(context, world)
    exportController.exportFile(file, exporter)
  }

  def saveToString(context: GraphContext, world: World, extension: String): String = GephiUtils.withNWLoaderContext {
    val exporter = exportController.getExporter(extension)
    prepareExport(exporter, s"the given $extension string")
    buildWorkspace(context, world)
    exporter match {
      case ce: CharacterExporter =>
        val writer = new StringWriter
        exportController.exportWriter(writer, ce)
        writer.toString
      case _ =>
        throw new ExtensionException(s"The $extension format cannot be exported to a string.")
    }
  }

  private def prepareExport(exporter: Exporter, sourceName: String): Unit = {
    if (exporter == null) {
      throw new ExtensionException("Unable to find exporter for " + sourceName)
    } else if (exporter.isInstanceOf[ExporterCSV]) {
      throw new ExtensionException("Exporting CSV files is not supported.")
    } else if (exporter.isInstanceOf[ExporterGraphML]) {
      throw new ExtensionException("You must use nw:save-graphml to save graphml files.")
    }

    // Some exporters expect a progress ticker to be passed in :( BCH 5/8/2015
    exporter match {
      case lt: LongTask => lt.setProgressTicket(new ProgressTicket {
          def finish(): Unit = ()

          def finish(finishMessage: String): Unit = ()

          def getDisplayName: String = ""

          def progress(): Unit = ()

          def progress(workunit: Int): Unit = ()

          def progress(message: String): Unit = ()

          def progress(message: String, workunit: Int): Unit = ()

          def switchToIndeterminate(): Unit = ()

          def setDisplayName(newDisplayName: String): Unit = ()

          def start(): Unit = ()

          def start(workunits: Int): Unit = ()

          def switchToDeterminate(workunits: Int): Unit = ()
        } )
      case _ => // ignore
    }
  }

  private def buildWorkspace(context: GraphContext, world: World): Unit = {
    implicit val implicitWorld = world;

    val program = world.program
    val projectController = Lookup.getDefault.lookup(classOf[ProjectController])
    projectController.newProject()
    val gephiWorkspace = projectController.getCurrentWorkspace
    val graphModel = Lookup.getDefault.lookup(classOf[GraphController]).getGraphModel
    val graph = (context.links.exists(_.isDirectedLink), context.links.exists(!_.isDirectedLink)) match {
      case (true, false) => graphModel.getDirectedGraph
      case (false, true) => graphModel.getUndirectedGraph
      case _             => graphModel.getGraph
    }

    val nodeAttributes = graphModel.getNodeTable

    val turtlesOwnAttributes: Map[String, Column] = program.turtlesOwn.map { name =>
      val kind = getBestType(world.turtles.asIterable[Turtle].map(t => t.getTurtleOrLinkVariable(name)))
      name -> Option(nodeAttributes.getColumn(name)).getOrElse(nodeAttributes.addColumn(name, kind))
    }.toMap

    val breedsOwnAttributes: Map[AgentSet, Map[String, Column]] = program.breeds.collect {
      case (breedName, breed: Breed) =>
        world.getBreed(breedName) -> breed.owns.map { name =>
          val kind = getBestType(world.getBreed(breedName).asIterable[Turtle].map(t => t.getBreedVariable(name)))
          name -> Option(nodeAttributes.getColumn(name)).getOrElse(nodeAttributes.addColumn(name, kind))
        }.toMap
    }.toMap

    val nodes = context.nodes.unsorted.map { turtle =>
      val node = graphModel.factory.newNode(turtle.toString.split(" ").mkString("-"))
      turtlesOwnAttributes.foreach { case (name, col) =>
        node.setAttribute(col, coerce(turtle.getTurtleOrLinkVariable(name), col.getTypeClass))
      }
      if (turtle.getBreed != world.turtles) {
        breedsOwnAttributes(turtle.getBreed).foreach { case (name, col) =>
          node.setAttribute(col, coerce(turtle.getBreedVariable(name), col.getTypeClass))
        }
      }
      val color = api.Color.getColor(turtle.color())
      node.setColor(color)
      node.setAlpha(color.getAlpha / 255f)
      graph.addNode(node)
      turtle -> node
    }.toMap

    val edgeAttributes = graphModel.getEdgeTable
    val linksOwnAttributes: Map[String, Column] = program.linksOwn.map { name =>
      val kind = getBestType(world.links.asIterable[Link].map(l => l.getTurtleOrLinkVariable(name)))
      name -> Option(edgeAttributes.getColumn(name)).getOrElse(edgeAttributes.addColumn(name, kind))
    }.toMap

    val linkBreedsOwnAttributes: Map[AgentSet, Map[String, Column]] = program.linkBreeds.collect {
      case (breedName, breed: Breed) =>
        val breedSet = world.getLinkBreed(breedName.toUpperCase)
        breedSet -> breed.owns.map { name =>
          lazy val kind = getBestType(breedSet.asIterable[Link].map(_.getLinkBreedVariable(name)))
          name -> Option(edgeAttributes.getColumn(name)).getOrElse(edgeAttributes.addColumn(name, kind))
        }.toMap
    }.toMap

    context.links.foreach { link =>
      val edge = graphModel.factory.newEdge(nodes(link.end1), nodes(link.end2), 1, link.isDirectedLink)
      linksOwnAttributes.foreach { case (name, col) =>
        edge.setAttribute(col, coerce(link.getTurtleOrLinkVariable(name), col.getTypeClass))
      }
      if (link.getBreed != world.links) {
        linkBreedsOwnAttributes(link.getBreed).foreach { case (name, col) =>
          edge.setAttribute(col, coerce(link.getLinkBreedVariable(name), col.getTypeClass))
        }
      }
      val color = api.Color.getColor(link.color())
      edge.setColor(color)
      edge.setAlpha(color.getAlpha / 255f)
      graph.addEdge(edge)
    }
    gephiWorkspace.add(graphModel)
  }

  private type JDouble     = java.lang.Double
  private type JBoolean    = java.lang.Boolean
  private val DoubleClass  = classOf[java.lang.Double]
  private val FloatClass   = classOf[java.lang.Float]
  private val BooleanClass = classOf[java.lang.Boolean]
  private val StringClass  = classOf[java.lang.String]

  private def getBestType(values: Iterable[AnyRef]): Class[?] = {
    if (values.forall(_.isInstanceOf[Number])){
      DoubleClass
    } else if (values.forall(_.isInstanceOf[JBoolean])) {
      BooleanClass
    } else {
      StringClass
    }
  }

  private def coerce(value: AnyRef, kind: Class[?]): AnyRef =
    (value, kind) match {
      case (x: Number, DoubleClass)    => x.doubleValue: JDouble
      case (x: Number, FloatClass)     => x.doubleValue: JDouble
      case (b: JBoolean, BooleanClass) => b
      // For Strings, we want to keep the escaping but ditch the surrounding quotes. BCH 1/26/2015
      case (s: String, StringClass)    => Dump.logoObject(s, readable = true, exporting = false).drop(1).dropRight(1)
      case (s: AnyRef, StringClass)    => Dump.logoObject(s, readable = true, exporting = false)
      case (o: AnyRef, attributeType)  =>
        throw new ExtensionException(s"Could not coerce ${Dump.logoObject(o, readable = true, exporting = false)} to $attributeType")
    }

}
