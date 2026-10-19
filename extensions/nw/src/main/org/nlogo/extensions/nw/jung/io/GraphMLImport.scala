package org.nlogo.extensions.nw.jung.io

import java.io.{BufferedReader, FileReader, Reader}
import java.util.Locale

import edu.uci.ics.jung
import edu.uci.ics.jung.graph.util.EdgeType
import edu.uci.ics.jung.io.graphml.Metadata.MetadataType
import edu.uci.ics.jung.io.graphml.{AbstractMetadata, EdgeMetadata, GraphMLReader2, GraphMetadata, Key, NodeMetadata}
import org.nlogo.agent.{Agent, AgentSet, Link, Turtle, World}
import org.nlogo.api.{AgentException, ExtensionException, MersenneTwisterFast}
import org.nlogo.extensions.nw.NetworkExtensionUtil.{createTurtle, using}
import org.nlogo.extensions.nw.jung.{createLink, sparseGraphFactory, transformer}

import scala.jdk.CollectionConverters.{ CollectionHasAsScala, ListHasAsScala, MapHasAsScala, SetHasAsScala }

object GraphMLImport {

  trait GraphElement {
    val metadata: AbstractMetadata
    def id: String
  }

  case class Vertex(metadata: NodeMetadata) extends GraphElement {
    def id = metadata.getId
  }

  case class Edge(metadata: EdgeMetadata) extends GraphElement {
    def id = metadata.getId
  }

  object Attribute {
    def apply(name: String, attributeType: String, value: String) =
      new Attribute(
        name.toUpperCase(Locale.ENGLISH),
        attributeType.toUpperCase(Locale.ENGLISH),
        Option(value).getOrElse(""))
  }
  class Attribute private (
    val name: String,
    val attributeType: String,
    val value: String) {
    def valueObject: AnyRef =
      try {
        attributeType match {
          case "BOOLEAN" => Boolean.box(value.toBoolean)
          case "INT"     => Double.box(value.toDouble)
          case "LONG"    => Double.box(value.toDouble)
          case "FLOAT"   => Double.box(value.toDouble)
          case "DOUBLE"  => Double.box(value.toDouble)
          case "STRING"  => value
          case _ =>
            // trial and errors for unknown types
            try Double.box(value.toDouble)
            catch {
              case _: Exception =>
                try Boolean.box(value.toBoolean)
                catch { case _: Exception => value } // string as a final resort
            }
        }
      } catch {
        // If anything fails, we return the value as a string.
        // TODO: use a proper XML parser eventually! NP 2013-02-15
        case e: Exception => value
      }
  }

  def attributes(e: GraphElement, keys: Seq[Key]): Seq[Attribute] = {
    val properties = e.metadata.getProperties.asScala
    val as = Attribute("ID", "STRING", e.id) +:
      (for {
        key <- keys
        value <- properties.get(key.getId).orElse(Option(key.defaultValue))
        attributeName = Option(key.getAttributeName).getOrElse(key.getId)
        attributeType = Option(key.getAttributeType).getOrElse("")
      } yield Attribute(attributeName, attributeType, value))
    as.sortBy(_.name != "BREED") // BREED first
  }

  // private def setBreed(agent: Agent, breed: String) {
  //   agent match {
  //     case t: Turtle =>
  //       for (breedAgentSet <- Option(agent.world.getBreed(breed)))
  //         t.setTurtleOrLinkVariable("BREED", breedAgentSet)
  //     case l: Link =>
  //       for (breedAgentSet <- Option(agent.world.getLinkBreed(breed)))
  //         l.setTurtleOrLinkVariable("BREED", breedAgentSet)
  //   }
  // }

  private def setAgentVariable(agent: Agent, attribute: Attribute): Unit = {
    if (attribute.name != "WHO") // don' try to set WHO
      try {
        val program = agent.world.program
        agent match {
          case l: Link =>
            attribute.name match {
              case "BREED" => // Breed is set separately
              case v if program.linksOwn.indexOf(v) != -1 =>
                agent.setTurtleOrLinkVariable(v, attribute.valueObject)
              case v =>
                agent.setLinkBreedVariable(v, attribute.valueObject)
            }
          case t: Turtle =>
            attribute.name match {
              case "BREED" => // Breed is set separately
              case v if program.turtlesOwn.indexOf(v) != -1 =>
                agent.setTurtleOrLinkVariable(v, attribute.valueObject)
              case v =>
                agent.setBreedVariable(v, attribute.valueObject)
            }
          case _ =>
            throw new IllegalStateException
        }
      } catch {
        case e: AgentException => // Variable just does not exist - move on
        case e: Exception      => throw new ExtensionException(e)
      }
  }

  private def createAgents[E <: GraphElement, A <: Agent](elements: Iterable[E],
                                                          keys: Seq[Key],
                                                          defaultBreed: AgentSet,
                                                          breeds: String => AgentSet)
                                                         (create: (E, AgentSet) => A): Map[E, A] =
    elements.map { elem =>
      val attrs = attributes(elem, keys)
      val breed = attrs.find(_.name == "BREED").map{
        _.valueObject.toString.toUpperCase(Locale.ENGLISH)
      }.flatMap(name => Option(breeds(name))).getOrElse(defaultBreed)
      val agent = create(elem, breed)
      attrs.foreach { setAgentVariable(agent, _) }
      elem -> agent
    }.toMap

  def load(fileName: String, world: World, rng: MersenneTwisterFast): Iterator[Turtle] =
    load(new BufferedReader(new FileReader(fileName)), world, rng, world.turtles, world.links)

  def load(reader: => Reader, world: World, rng: MersenneTwisterFast,
           defaultTurtleBreed: AgentSet, defaultLinkBreed: AgentSet): Iterator[Turtle] = {
    try {
      val graphFactory = sparseGraphFactory[Vertex, Edge]
      val graphTransformer = transformer { (_: GraphMetadata) => graphFactory.create }

      using {
        new GraphMLReader2[jung.graph.Graph[Vertex, Edge], Vertex, Edge](
          reader,
          graphTransformer,
          transformer(Vertex.apply),
          transformer(Edge.apply),
          transformer(_ => Edge(null)))
      } { graphReader =>
        val graph = graphReader.readGraph()

        val keyMap: Map[MetadataType, Seq[Key]] =
          graphReader
            .getGraphMLDocument.getKeyMap.entrySet()
            .asScala.map(entry => entry.getKey -> entry.getValue.asScala.toSeq).toMap

        // The vertices have non-deterministic order, so we need to sort them by id
        // so that the turtles are created in the same order every time. Otherwise
        // we end up with an isomorphic, but non-identical network (up to turtle id)
        // - BCH 8/7/2018
        val vertices = graph.getVertices.asScala.toList.sortBy(_.id)

        val turtles: Map[Vertex, Turtle] =
          createAgents(vertices, keyMap(MetadataType.NODE), defaultTurtleBreed, world.getBreed) {
            (_, breed) => createTurtle(world, breed, rng)
          }

        createAgents(graph.getEdges.asScala, keyMap(MetadataType.EDGE), defaultLinkBreed, world.getLinkBreed) {
          (e: Edge, breed) =>
            createLink(turtles, graph.getEndpoints(e), graph.getDefaultEdgeType == EdgeType.DIRECTED, breed, world)
        }

        // The `turtles` map also has non-deterministic order (likely since the keys
        // are being hashed by reference address rather than a deterministic hash
        // function). We need to return the turtles in a deterministic order, however,
        // so initialization code always runs in the same order. We do this by mapping
        // `vertices`, which is ordered, so that we don't have to do another sort. We
        // do this lazily to avoid it completely if there is no initialization code.
        // - BCH 8/7/2018
        vertices.iterator.map(turtles)
      }
    } catch {
      case e: Exception => throw new ExtensionException(e)
    }
  }
}
