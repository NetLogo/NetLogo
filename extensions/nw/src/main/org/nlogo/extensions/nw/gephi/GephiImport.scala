package org.nlogo.extensions.nw.gephi

import java.awt.Color
import java.io.{ File, FileReader, IOException, Reader }
import java.lang.{ Boolean => JBoolean, Double  => JDouble }
import java.util.Collection

import scala.collection.Map
import scala.jdk.CollectionConverters.{ CollectionHasAsScala, IterableHasAsScala, MapHasAsScala }
import scala.language.postfixOps
import scala.util.control.Exception.allCatch

import org.nlogo.agent.{ Agent, AgentSet, Turtle, World }
import org.nlogo.api._
import org.nlogo.core.LogoList

import org.gephi.graph.api.types.TimeMap
import org.gephi.io.importer.api.{ EdgeDirection, EdgeDirectionDefault, EdgeDraft, ElementDraft, ImportController, NodeDraft }
import org.gephi.io.importer.plugin.file.{ ImporterGDF, ImporterGraphML }
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetCSV
import org.gephi.io.importer.spi.FileImporter
import org.openide.util.Lookup

import org.nlogo.extensions.nw.NetworkExtensionUtil._

object GephiImport {
  val importController = GephiUtils.withNWLoaderContext {
    Lookup.getDefault.lookup(classOf[ImportController])
  }

  def load(file: File, world: World,
           defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet,
           initTurtles: IterableOnce[Turtle] => Unit): Unit = GephiUtils.withNWLoaderContext {
    load(file, world, defaultTurtleBreed, defaultLinkBreed, initTurtles, importController.getFileImporter(file))
  }

  def load(file: File, world: World,
           defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet,
           initTurtles: IterableOnce[Turtle] => Unit,
           extension: String): Unit = GephiUtils.withNWLoaderContext {
    load(file, world, defaultTurtleBreed, defaultLinkBreed, initTurtles, importController.getFileImporter(extension))
  }

  def load(file: File, world: World,
           defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet,
           initTurtles: IterableOnce[Turtle] => Unit,
           importer: FileImporter): Unit = GephiUtils.withNWLoaderContext {
    if (!file.exists) {
      throw new ExtensionException("The file " + file + " cannot be found.")
    }
    load(new FileReader(file), file.toString, world, defaultTurtleBreed, defaultLinkBreed, initTurtles, importer)
  }

  def loadString(data: String, extension: String, world: World,
                 defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet,
                 initTurtles: IterableOnce[Turtle] => Unit): Unit = GephiUtils.withNWLoaderContext {
    load(readerForString(data), s"the given $extension string",
      world, defaultTurtleBreed, defaultLinkBreed, initTurtles, importController.getFileImporter(extension))
  }

  def load(reader: => Reader, sourceName: String, world: World,
           defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet,
           initTurtles: IterableOnce[Turtle] => Unit,
           importer: FileImporter): Unit = GephiUtils.withNWLoaderContext {
    val isGdfImport = importer match {
      case null =>
        throw new ExtensionException("Unable to find importer for " + sourceName)

      case _: ImporterSpreadsheetCSV =>
        throw new ExtensionException("Importing CSV files is not supported.")

      case _: ImporterGraphML =>
        throw new ExtensionException("You must use nw:load-graphml to load graphml files.")

      case _: ImporterGDF =>
        true

      case _ =>
        false
    }

    val turtleBreeds = world.breeds.asScala
    val linkBreeds = world.linkBreeds.asScala

    val container = try {
      using(reader)(r => importController.importFile(r, importer))
    } catch {
      case e: IOException =>
        throw new ExtensionException(e)
    }

    val unloader = container.getUnloader

    val defaultDirected = unloader.getEdgeDefault == EdgeDirectionDefault.DIRECTED

    val nodes: Iterable[NodeDraft] = unloader.getNodes.asScala
    val edges: Iterable[EdgeDraft] = unloader.getEdges.asScala

    val nodeToTurtle: Map[NodeDraft, Turtle] = nodes zip nodes.map {
      node => {
        val attrs = getAttributes(node) ++
        pair("LABEL", node.getLabel) ++
        pair("LABEL-COLOR", node.getLabelColor) ++
        pair("COLOR", node.getColor)

        // Note that node's have a getSize. This does not correspond to the `size` attribute in files so should not be
        // used. BCH 1/21/2015

        val breed = getBreed(attrs, turtleBreeds).getOrElse(defaultTurtleBreed)
        val turtle = createTurtle(world, breed, world.mainRNG)
        (attrs - "BREED") foreach (setAttribute(world, turtle)).tupled
        turtle
      }
    } toMap

    val badEdges: Iterable[EdgeDraft] = edges.map { edge =>
      val source = nodeToTurtle(edge.getSource)
      val target = nodeToTurtle(edge.getTarget)
      // There are three gephi edge types: directed, undirected, and mutual. Mutual is pretty much just indicating that
      // and edge goes both ways/there are two edges in either direction, so we treat it as either. BCH 1/22/2015
      val attrs = getAttributes(edge) ++
        pair("LABEL", edge.getLabel) ++
        pair("LABEL-COLOR", edge.getLabelColor) ++
        pair("COLOR", edge.getColor) ++
        pair("WEIGHT", edge.getWeight.toDouble: JDouble)

      val breed = getBreed(attrs, linkBreeds).getOrElse(defaultLinkBreed)

      val gephiDirected   = edge.getDirection == EdgeDirection.DIRECTED
      val gephiUndirected = edge.getDirection == EdgeDirection.UNDIRECTED
      val gephiUnset      = edge.getDirection == null

      val bad = if (breed.isDirected == breed.isUndirected) {
        // This happens when the directedness of the default breed hasn't been set yet
        if (gephiUnset) {
          breed.setDirected(defaultDirected)
        } else {
          breed.setDirected(gephiDirected)
        }
        false
      } else {
        // We have to special case on the GDF import as it defaults to undirected edges on
        // import in the latest gephi version.  Those will conflict, but it's not "wrong",
        // that's just how they come out.  https://github.com/gephi/gephi/issues/906
        // -Jeremy B July 2022
        ((breed.isDirected && gephiUndirected && !isGdfImport) || (breed.isUndirected && gephiDirected))
      }

      val links = List(world.linkManager.createLink(source, target, breed)) ++ {
        if (breed.isDirected && gephiUnset)
          Some(world.linkManager.createLink(target, source, breed))
        else
          None
      }

      links foreach { l => (attrs - "BREED") foreach (setAttribute(world, l)).tupled }

      if (bad) {
        Some(edge)
      } else {
        None
      }
    }.collect({ case Some(e: EdgeDraft) => e })

    initTurtles(nodeToTurtle.values)

    if (badEdges.nonEmpty) {
      val edgesList = badEdges.map(e => e.getSource.getId + "->" + e.getTarget.getId).mkString(", ")
      val errorMsg =
        "The following edges had a directedness different than their assigned breed. They have been given " +
        "the directedness of their breed. If you wish to ignore this error, wrap this command in a CAREFULLY:"
      throw new ExtensionException(errorMsg + " " + edgesList)
    }
  }

  private def convertColor(c: Color): LogoList = {
    val l = LogoList(
      c.getRed.toDouble: JDouble
    , c.getGreen.toDouble: JDouble
    , c.getBlue.toDouble: JDouble)
    if (c.getAlpha != 255) l.lput(c.getAlpha.toDouble: JDouble)
    l
  }

  private def pair(key: String, value: AnyRef): Option[(String, AnyRef)] =
    Option(value) map (v => key -> convertAttribute(key, v))

  private def getAttributes(el: ElementDraft): scala.collection.immutable.Map[String, AnyRef] =
    el.getColumns.asScala.map { c =>
      val name     = c.getId.toUpperCase
      val rawValue = el.getValue(c.getId)
      val value    = convertAttribute(name, rawValue)
      (name, value)
    }.filter({ case (_, v) => v != null }).toMap

  private def getBreed(attributes: Map[String, AnyRef], breeds: Map[String, AgentSet]): Option[AgentSet] = {
    attributes
      .get("BREED")
      .collect({ case s: String => s.toUpperCase })
      .flatMap( s => breeds.get(s) )
  }

  private val doubleBuiltins = Set("XCOR", "YCOR", "HEADING", "PEN-SIZE", "THICKNESS", "SIZE")
  private val booleanBuiltins = Set("HIDDEN")
  private val colorBuiltins = Set("COLOR", "LABEL-COLOR")

  private def convertAttribute(name: String, o: Any): AnyRef = {
    if (doubleBuiltins.contains(name)) o match {
      case x: Number => x.doubleValue: JDouble
      case s: String  => allCatch.opt(s.toDouble: JDouble).getOrElse(0.0: JDouble)
      case _ => "" // purposely invalid so it won't set. Might want to throw error instead. BCH 1/25/2015

    } else if (booleanBuiltins.contains(name)) o match {
      case x: Number  => x.doubleValue != 0: JBoolean
      case b: JBoolean => b
      case s: String   => allCatch.opt(s.toBoolean: JBoolean).getOrElse(false: JBoolean)
      case _ => "" // purposely invalid so it won't set. Might want to throw error instead. BCH 1/25/2015

    } else if (colorBuiltins.contains(name)) o match {
      case c: Color => convertColor(c)
      case x: Number => x.doubleValue: JDouble
      case c: Collection[?] => LogoList.fromIterator(c.asScala.map(convertAttribute).iterator)
      case s: String  => allCatch.opt(s.toDouble: JDouble).getOrElse("")
      case _ => "" // purposely invalid so it won't set. Might want to throw error instead. BCH 1/25/2015

    } else {
      convertAttribute(o)
    }
  }

  private def convertAttribute(o: Any): AnyRef = o match {
    case c: Color => convertColor(c)
    case n: Number => n.doubleValue: JDouble
    case b: JBoolean => b
    case ll: LogoList => ll
    case c: Collection[?] => LogoList.fromIterator(c.asScala.map(x => convertAttribute(x)).iterator)
    // There may be a better handling of dynamic values, but this seems good enough for now. BCH 1/21/2015
    case d: TimeMap[?, ?] => LogoList.fromIterator(d.toValuesArray.map(x => convertAttribute(x)).iterator)
    case a: Array[?] => LogoList.fromIterator(a.map(convertAttribute).iterator)
    // Gephi attributes are strongly typed. Many formats, however, are not, and neither is NetLogo. Thus, when we use
    // String as a kind of AnyRef. This is a bad solution, but better than alternatives. BCH 1/25/2015
    case null => ""
    case x => x.toString
  }

  private def setAttribute(world: World, agent: Agent)(name: String, value: AnyRef): Unit = {
    val i = world.indexOfVariable(agent, name)
    if (i != -1) {
      try {
        agent.setVariable(i, value)
      } catch {
        case e: AgentException => /*Invalid variable or value, so skip*/
      }
    }
  }
}
