package org.nlogo.extensions.nw.prim

import java.io.File

import org.nlogo.agent.World
import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.nvm.ExtensionContext

import org.nlogo.extensions.nw.GraphContextProvider
import org.nlogo.extensions.nw.NetworkExtensionUtil._
import org.nlogo.extensions.nw.gephi.{ GephiExport, GephiImport, GephiUtils }
import org.nlogo.extensions.nw.jung.io.{ GraphMLExport, GraphMLImport, Matrix }

class Load extends TurtleAskingCommand {
  override def getSyntax = commandSyntax(
    right = List(StringType, TurtlesetType, LinksetType, CommandBlockType | OptionalType),
    blockAgentClassString = Some("-T--"))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    implicit val world = context.world.asInstanceOf[World]
    val ws = context.asInstanceOf[ExtensionContext].workspace
    val turtleBreed = args(1).getAgentSet.requireTurtleBreed
    val linkBreed = args(2).getAgentSet.requireLinkBreed
    val file = new File(ws.fileManager.attachPrefix(args(0).getString))
    GephiImport.load(file, world, turtleBreed, linkBreed, askTurtles(context))
  }
}

class LoadFromString extends TurtleAskingCommand {
  override def getSyntax = commandSyntax(
    right = List(StringType, StringType, TurtlesetType, LinksetType, CommandBlockType | OptionalType),
    blockAgentClassString = Some("-T--"))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    implicit val world = context.world.asInstanceOf[World]
    val rawFormat   = args(0).getString
    val data        = args(1).getString
    val turtleBreed = args(2).getAgentSet.requireTurtleBreed
    val linkBreed   = args(3).getAgentSet.requireLinkBreed
    val rng         = context.getRNG
    normalizeNetworkFormat(rawFormat) match {
      case "graphml" =>
        askTurtles(context)(GraphMLImport.load(readerForString(data), world, rng, turtleBreed, linkBreed))
      case "matrix" =>
        askTurtles(context)(Matrix.load(readerForString(data), turtleBreed, linkBreed, world, rng))
      case format @ ("dl" | "gdf" | "gexf" | "gml" | "vna") =>
        GephiImport.loadString(data, "." + format, world, turtleBreed, linkBreed, askTurtles(context))
      case _ =>
        throw new api.ExtensionException(unsupportedFormatMessage(rawFormat))
    }
  }
}

class LoadFileType(extension: String) extends TurtleAskingCommand {
  override def getSyntax = commandSyntax(right = List(StringType, TurtlesetType, LinksetType, CommandBlockType | OptionalType), blockAgentClassString = Some("-T--"))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    implicit val world = context.world.asInstanceOf[World]
    val ws = context.asInstanceOf[ExtensionContext].workspace
    val turtleBreed = args(1).getAgentSet.requireTurtleBreed
    val linkBreed = args(2).getAgentSet.requireLinkBreed
    val file = new File(ws.fileManager.attachPrefix(args(0).getString))
    GephiImport.load(file, world, turtleBreed, linkBreed, askTurtles(context), extension)
  }
}

class LoadFileTypeDefaultBreeds(extension: String) extends TurtleAskingCommand {
  override def getSyntax = commandSyntax(right = List(StringType, CommandBlockType | OptionalType), blockAgentClassString = Some("-T--"))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    val world = context.world.asInstanceOf[World]
    val ws = context.asInstanceOf[ExtensionContext].workspace
    val file = new File(ws.fileManager.attachPrefix(args(0).getString))
    GephiImport.load(file, world, world.turtles, world.links, askTurtles(context), extension)
  }
}

class Save(gcp: GraphContextProvider) extends api.Command {
  override def getSyntax = commandSyntax(right = List(StringType))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    val world = context.getAgent.world.asInstanceOf[World]
    val workspace = context.asInstanceOf[ExtensionContext].workspace
    val fm = workspace.fileManager
    val file = new File(fm.attachPrefix(args(0).getString))
    GephiExport.save(gcp.getGraphContext(world), world, file)
  }
}

class SaveToString(gcp: GraphContextProvider) extends api.Reporter {
  override def getSyntax = reporterSyntax(right = List(StringType), ret = StringType)
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = GephiUtils.withNWLoaderContext {
    val world        = context.getAgent.world.asInstanceOf[World]
    val rawFormat    = args(0).getString
    val graphContext = gcp.getGraphContext(world)
    normalizeNetworkFormat(rawFormat) match {
      case "graphml" =>
        GraphMLExport.saveToString(graphContext)
      case "matrix" =>
        Matrix.saveToString(graphContext.asJungGraph)
      case format @ ("dl" | "gdf" | "gexf" | "gml" | "vna") =>
        GephiExport.saveToString(graphContext, world, "." + format)
      case _ =>
        throw new api.ExtensionException(unsupportedFormatMessage(rawFormat))
    }
  }
}

class SaveFileType(gcp: GraphContextProvider, extension: String) extends api.Command {
  override def getSyntax = commandSyntax(right = List(StringType))
  override def perform(args: Array[api.Argument], context: api.Context) = GephiUtils.withNWLoaderContext {
    val world = context.getAgent.world.asInstanceOf[World]
    val workspace = context.asInstanceOf[ExtensionContext].workspace
    val fm = workspace.fileManager
    val file = new File(fm.attachPrefix(args(0).getString))
    GephiExport.save(gcp.getGraphContext(world), world, file, extension)
  }
}
