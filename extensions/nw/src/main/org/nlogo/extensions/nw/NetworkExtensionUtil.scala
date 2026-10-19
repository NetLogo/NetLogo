// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw

import org.nlogo.agent.TreeAgentSet
import org.nlogo.api.{Agent, ExtensionException}
import org.nlogo.core.{ AgentKind, I18N, Syntax, Token }
import org.nlogo.{agent, api, nvm}
import org.nlogo.api.MersenneTwisterFast
import scala.language.implicitConversions
import scala.reflect.Selectable.reflectiveSelectable
import java.io.{ ByteArrayInputStream, InputStreamReader, Reader }
import java.nio.charset.StandardCharsets
import java.util.Locale

object NetworkExtensionUtil {

  // Wraps an in-memory string in a Reader for feeding to the file importers. We deliberately do NOT use StringReader:
  // its `ready()` always returns true, even at EOF, whereas the Gephi importers loop on `reader.ready()` and rely on it
  // going false to stop. Backing the reader with a ByteArrayInputStream gives FileReader-like `ready()` semantics
  // (false at EOF), so the importers terminate. -Jeremy B July 2026
  def readerForString(data: String): Reader =
    new InputStreamReader(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)

  val supportedStringFormats = Seq("dl", "gdf", "gexf", "gml", "graphml", "matrix", "vna")

  def normalizeNetworkFormat(rawFormat: String): String =
    rawFormat.trim.toLowerCase(Locale.ENGLISH).stripPrefix(".")

  def unsupportedFormatMessage(rawFormat: String): String =
    s"'$rawFormat' is not a supported network format. Valid formats are: dl, gdf, gexf, gml, graphml, matrix, and vna."

  implicit def functionToTransformer[I, O](f: Function1[I, O]): org.apache.commons.collections15.Transformer[I,O] =
    new org.apache.commons.collections15.Transformer[I, O] {
      override def transform(i: I) = f(i)
    }

  implicit def AgentToRichAgent(agent: Agent): org.nlogo.extensions.nw.NetworkExtensionUtil.RichAgent = new RichAgent(agent)
  class RichAgent(agent: Agent) {
    def requireAlive =
      if (agent.id != -1) // is alive
        agent
      else throw new ExtensionException(
        I18N.errors.get("org.nlogo.$common.thatAgentIsDead"))
  }

  implicit def LinkToRichLink(link: org.nlogo.agent.Link)(implicit world: agent.World): org.nlogo.extensions.nw.NetworkExtensionUtil.RichLink =
    new RichLink(link, world)

  class RichLink(link: org.nlogo.agent.Link, world: agent.World) {
    def getBreedOrLinkVariable(variable: String) =
      try {
        world.program.linksOwn.indexOf(variable) match {
          case -1 => link.getLinkBreedVariable(variable)
          case i  => link.getLinkVariable(i)
        }
      } catch {
        case e: Exception => throw new ExtensionException(e)
      }
  }

  implicit def AgentSetToRichAgentSet(agentSet: api.AgentSet)(implicit world: org.nlogo.agent.World): org.nlogo.extensions.nw.NetworkExtensionUtil.RichAgentSet =
    new RichAgentSet(agentSet.asInstanceOf[agent.AgentSet], world)

  class RichAgentSet(agentSet: agent.AgentSet, val world: org.nlogo.agent.World) {
    assert(agentSet != null)
    def isLinkBreed = (agentSet eq world.links) || world.isLinkBreed(agentSet)
    def isTurtleBreed = (agentSet eq world.turtles) || world.isBreed(agentSet)

    def isLinkSet = agentSet.kind == AgentKind.Link
    def isTurtleSet = agentSet.kind == AgentKind.Turtle
    def requireTurtleSet =
      if (isTurtleSet) agentSet
      else throw new ExtensionException("Expected input to be a turtleset")
    def requireLinkSet =
      if (isLinkSet) agentSet
      else throw new ExtensionException("Expected input to be a linkset")

    def requireTurtleBreed =
      if (isTurtleBreed) agentSet.asInstanceOf[TreeAgentSet]
      else throw new ExtensionException("Expected input to be a turtle breed")
    def requireLinkBreed =
      if (isLinkBreed) agentSet.asInstanceOf[TreeAgentSet]
      else throw new ExtensionException(
        I18N.errors.get("org.nlogo.prim.etc.$common.expectedLastInputToBeLinkBreed"))
    def requireDirectedLinkBreed =
      if (isLinkBreed && agentSet.isDirected) agentSet.asInstanceOf[TreeAgentSet]
      else throw new ExtensionException(
        "Expected input to be a directed link breed")
    def requireUndirectedLinkBreed =
      if (isLinkBreed && !agentSet.isDirected) agentSet.asInstanceOf[TreeAgentSet]
      else throw new ExtensionException(
        "Expected input to be an undirected link breed")

    private class AgentSetIterable[T <: Agent]
      extends Iterable[T] {
      protected def newIt = agentSet.iterator
      override def iterator: Iterator[T] = {
        val it = newIt
        new Iterator[T] {
          def hasNext = it.hasNext
          def next() = it.next().asInstanceOf[T]
        }
      }
    }
    def asIterable[T <: Agent]: Iterable[T] = new AgentSetIterable

    private class AgentSetShufflerable[T <: Agent](rng: MersenneTwisterFast)
      extends AgentSetIterable[T] {
      override def newIt = agentSet.shufflerator(rng)
    }
    /** A Shufflerable is to a Shufflerator as an Iterable is to an Iterator */
    def asShufflerable[T <: Agent](rng: MersenneTwisterFast): Iterable[T] =
      new AgentSetShufflerable(rng)

  }

  trait TurtleAskingCommand extends api.Command with nvm.CustomAssembled {
    // the command itself is turtle or observer. inside the block is turtle code.
    // Issue #126 provides a good use case for this to be executed in turtle contexts.
    override def getSyntax =
      Syntax.commandSyntax(agentClassString = "OT--", blockAgentClassString = Some("-T--"))

    def askTurtles(context: api.Context)(turtles: IterableOnce[agent.Turtle]) = {
      val agents = turtles.iterator.toArray
      val extContext = context.asInstanceOf[nvm.ExtensionContext]
      val nvmContext = extContext.nvmContext
      agents.foreach(extContext.workspace.joinForeverButtons)
      val agentSet = agent.AgentSet.fromArray(AgentKind.Turtle, agents)
      nvmContext.runExclusiveJob(agentSet, nvmContext.ip + 1)
    }
    def assemble(a: nvm.AssemblerAssistant): Unit = {
      a.block()
      a.done()
    }

  }

  trait TurtleCreatingCommand extends TurtleAskingCommand {
    def createTurtles(args: Array[api.Argument], context: api.Context): IterableOnce[agent.Turtle]
    override def perform(args: Array[api.Argument], context: api.Context) =
      askTurtles(context)(createTurtles(args, context))

    // helper function to validate a minimum number of "things" (could be nodes, rows, columns, etc.)
    // and throw an appropriate exception if the value is below that minimum
    protected def getIntValueWithMinimum(arg: api.Argument, minimum: Int, things: String = "nodes") = {
      val nb = arg.getIntValue
      if (nb < minimum)
        throw new ExtensionException(
          "The number of " + things + " in the generated network must be at least " + minimum + ".")
      nb
    }

  }

  def createTurtle(world: agent.World, turtleBreed: agent.AgentSet, rng: MersenneTwisterFast) =
    world.createTurtle(
      turtleBreed,
      rng.nextInt(14), // color
      rng.nextInt(360)) // heading

  def using[A <: { def close(): Unit }, B](closeable: A)(body: A => B): B =
    try body(closeable) finally closeable.close()

  def canonocilizeVar(variable: AnyRef) = variable match {
    case s: String => s.toUpperCase(Locale.ENGLISH)
    case t: Token  => t.text.toString.toUpperCase(Locale.ENGLISH)
    case _ => throw new IllegalStateException
  }
}
