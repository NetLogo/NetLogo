package org.nlogo.extensions.samplescala

import org.nlogo.{ agent, api, core, nvm }
import core.Syntax._
import api.ScalaConversions._  // implicits
import org.nlogo.core.AgentKind

class SampleScalaExtension extends api.DefaultClassManager {
  def load(manager: api.PrimitiveManager): Unit = {
    manager.addPrimitive("first-n-integers", IntegerList)
    manager.addPrimitive("my-list", MyList)
    manager.addPrimitive("create-red-turtles", CreateRedTurtles)
  }
}

object IntegerList extends api.Reporter {
  override def getSyntax =
    reporterSyntax(right = List(NumberType), ret = ListType)
  def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val n = try args(0).getIntValue
    catch {
      case e: api.LogoException =>
        throw new api.ExtensionException(e.getMessage)
    }
    if (n < 0)
      throw new api.ExtensionException("input must be positive")
    (0 until n).toLogoList
  }
}

object MyList extends api.Reporter {
  override def getSyntax =
    reporterSyntax(right = List(WildcardType | RepeatableType), ret = ListType, defaultOption = Some(3))
  def report(args: Array[api.Argument], context: api.Context) =
    args.map(_.get).toLogoList
}

object CreateRedTurtles extends api.Command with nvm.CustomAssembled {
  override def getSyntax =
    commandSyntax(right = List(NumberType, CommandBlockType | OptionalType),
      agentClassString = "O---",
      blockAgentClassString = Some("-T--"))

  // only box this once
  private val red = Double.box(15)

  def perform(args: Array[api.Argument], context: api.Context): Unit = {
    // the api package have what we need, so we'll often
    // be dropping down to the agent and nvm packages
    val n = args(0).getIntValue
    val world = context.getAgent.world.asInstanceOf[agent.World]
    val eContext = context.asInstanceOf[nvm.ExtensionContext]
    val nvmContext = eContext.nvmContext
    val agents =
      new agent.AgentSetBuilder(AgentKind.Turtle, n)
    for(_ <- 0 until n) {
      val turtle = world.createTurtle(world.turtles)
      turtle.colorDoubleUnchecked(red)
      agents.add(turtle)
      eContext.workspace.joinForeverButtons(turtle)
    }
    // if the optional command block wasn't supplied, then there's not
    // really any point in calling this, but it won't bomb, either
    nvmContext.runExclusiveJob(agents.build(), nvmContext.ip + 1)
    // prim._extern will take care of leaving nvm.Context ip in the right place
  }

  def assemble(a: nvm.AssemblerAssistant): Unit = {
    a.block()
    a.done()
  }
}
