// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jung.io

import java.io.{ BufferedReader, FileNotFoundException, Reader, StringWriter, Writer }

import org.nlogo.agent.AgentSet
import org.nlogo.agent.Link
import org.nlogo.agent.Turtle
import org.nlogo.agent.World
import org.nlogo.api.ExtensionException
import org.nlogo.extensions.nw.jung.DummyGraph
import org.nlogo.extensions.nw.jung.factoryFor
import org.nlogo.extensions.nw.NetworkExtensionUtil.using
import org.nlogo.api.MersenneTwisterFast

import edu.uci.ics.jung
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations

object Matrix {

  def save(graph: jung.graph.Graph[Turtle, Link], filename: String): Unit =
    try {
      using(new java.io.FileWriter(filename)) { writeMatrix(graph, _) }
    } catch {
      case e: Exception => throw new ExtensionException(e)
    }

  def saveToString(graph: jung.graph.Graph[Turtle, Link]): String =
    try {
      val writer = new StringWriter
      writeMatrix(graph, writer)
      writer.toString
    } catch {
      case e: Exception => throw new ExtensionException(e)
    }

  private def writeMatrix(graph: jung.graph.Graph[Turtle, Link], writer: Writer): Unit = {
    /* This is almost a line for line copy of jung.io.MatrixFile.save, the major
     * difference being that it explicitly uses the US locale to make sure entries
     * use the dot decimal separator (see issue #69) */
    val matrix = GraphMatrixOperations.graphToSparseMatrix(graph, null) // TODO: provide weights
    for (i <- 0 until matrix.rows) {
      for (j <- 0 until matrix.columns) {
        val w = matrix.getQuick(i, j)
        writer.write("%4.2f".formatLocal(java.util.Locale.US, w))
        if (j < matrix.columns - 1) writer.write(" ")
      }
      writer.write("\n")
    }
  }

  def load(filename: String, turtleBreed: AgentSet, linkBreed: AgentSet, world: World, rng: MersenneTwisterFast): Iterator[Turtle] =
    load(matrixFile => matrixFile.load(filename), turtleBreed, linkBreed, world, rng)

  def load(reader: Reader, turtleBreed: AgentSet, linkBreed: AgentSet, world: World, rng: MersenneTwisterFast): Iterator[Turtle] = {
    val buffered = reader match {
      case b: BufferedReader => b
      case r                 => new BufferedReader(r)
    }
    load(matrixFile => matrixFile.load(buffered), turtleBreed, linkBreed, world, rng)
  }

  private def load(readGraph: jung.io.MatrixFile[DummyGraph.Vertex, DummyGraph.Edge] => jung.graph.Graph[DummyGraph.Vertex, DummyGraph.Edge],
                   turtleBreed: AgentSet, linkBreed: AgentSet, world: World, rng: MersenneTwisterFast): Iterator[Turtle] = {
    val matrixFile = new jung.io.MatrixFile(
      null, // TODO: provide weight key (null means 1) (issue #19)
      factoryFor(linkBreed), DummyGraph.vertexFactory, DummyGraph.edgeFactory)
    val graph = try {
      readGraph(matrixFile)
    } catch {
      case e: Exception => e.getCause match {
        case fileNotFound: FileNotFoundException =>
          throw new ExtensionException(fileNotFound)
        case _ =>
          throw new ExtensionException(e)
      }
    }
    DummyGraph.importToNetLogo(graph, world, turtleBreed, linkBreed, rng)
  }
}
